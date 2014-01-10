package org.masukomi.aspirin.store.mail;

import org.masukomi.aspirin.AspirinException;
import org.masukomi.aspirin.AspirinInternal;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This store implementation is designed to reduce memory usage of MimeMessage instances. All MimeMessage instance are
 * stored in files and in weak references too. So garbage collector can remove all large MimeMessage object from memory
 * if necessary.
 *
 */
public class FileMailStore implements MailStore {
	
	private Path rootDir;
	private int subDirCount = 3;
	private Random rand = new Random();
	private Map<String, WeakReference<MimeMessage>> messageMap = new ConcurrentHashMap<>();
	private Map<String, Path> messagePathMap = new ConcurrentHashMap<>();

    public FileMailStore(Path rootDir) {
        this.rootDir = rootDir;
    }

    @Override
	public MimeMessage get(String mailid) {
		WeakReference<MimeMessage> msgRef = messageMap.get(mailid);
		MimeMessage msg = null;
		if( msgRef != null )
		{
			msg = msgRef.get();
			if( msg == null )
			{
				try(InputStream msgis = Files.newInputStream(messagePathMap.get(mailid), StandardOpenOption.READ)) {
                    msg = new MimeMessage(Session.getDefaultInstance(System.getProperties()), msgis);
					messageMap.put(mailid, new WeakReference<>(msg));
				} catch (IOException e) {
					AspirinInternal.getLogger().error(getClass().getSimpleName()+" No file representation found for name "+mailid,e);
				} catch (MessagingException e) {
					AspirinInternal.getLogger().error(getClass().getSimpleName()+" There is a messaging exception with name "+mailid,e);
				}
			}
		}
		return msg;
	}
	
	@Override
	public List<String> getMailIds() {
		return new ArrayList<>(messageMap.keySet());
	}
	
	@Override
	public void init() {
        if( !Files.exists(rootDir) )
            return;
        try {
            Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path msgFile, BasicFileAttributes attrs) throws IOException {
                    try(InputStream msgis = Files.newInputStream(msgFile, StandardOpenOption.READ)) {
                        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(System.getProperties()), msgis);
                        String mailid = AspirinInternal.getMailID(msg);
                        messageMap.put(mailid, new WeakReference<>(msg));
                        messagePathMap.put(mailid, msgFile);
                    } catch (IOException e) {
                        AspirinInternal.getLogger().error(getClass().getSimpleName()+" No file representation found with name "+msgFile,e);
                    } catch (MessagingException e) {
                        AspirinInternal.getLogger().error(getClass().getSimpleName()+" There is a messaging exception in file "+msgFile,e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            AspirinInternal.getLogger().error(getClass().getSimpleName()+" Store initialization failed.",e);
        }
	}

	@Override
	public void remove(String mailid) {
        try {
		    messageMap.remove(mailid);
            Files.delete(messagePathMap.remove(mailid));
        } catch (IOException ioe) {
            AspirinInternal.getLogger().error(getClass().getSimpleName()+" File deleting failed: "+mailid,ioe);
        }
    }
	
	@Override
	public void set(MimeMessage msg) {
        createFolderStructure();
        String mailid = AspirinInternal.getMailID(msg);
        saveContentAndMetadata(mailid, msg, generateFilePath(mailid));
    }

    private void saveContentAndMetadata(String mailid, MimeMessage msg, Path filepath) {
        try(OutputStream fos = Files.newOutputStream(filepath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            msg.writeTo(fos);
            messageMap.put(mailid, new WeakReference<>(msg));
            messagePathMap.put(mailid, filepath);
        } catch (FileNotFoundException e) {
            AspirinInternal.getLogger().error(getClass().getSimpleName()+" No file representation found for name "+mailid,e);
        } catch (IOException e) {
            AspirinInternal.getLogger().error(getClass().getSimpleName()+" Could not write file for name "+mailid,e);
        } catch (MessagingException e) {
            AspirinInternal.getLogger().error(getClass().getSimpleName()+" There is a messaging exception with name "+mailid,e);
        }
    }

    private Path generateFilePath(String mailid) {
        return rootDir.resolve(String.valueOf(rand.nextInt(subDirCount))).resolve(mailid + ".msg");
    }

    private void createFolderStructure() {
        for( int dirCount = 0; dirCount < subDirCount; dirCount++ ) {
            Path dir = rootDir.resolve(String.valueOf(dirCount));
            if( !Files.exists(dir) ) {
                try {
                    Files.createDirectories(dir);
                } catch (IOException e) {
                    throw new AspirinException("Mail store directory structure creation failed.",e);
                }
            }
        }
    }
	
	public Path getRootDir() {
		return rootDir;
	}
	public int getSubDirCount() {
		return subDirCount;
	}

    public void setSubDirCount(int subDirCount) {
        this.subDirCount = subDirCount;
    }
}
