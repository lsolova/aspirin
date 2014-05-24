package org.masukomi.aspirin.store.mail;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Random;

import static org.junit.Assert.assertNotNull;

public class FileMailStoreTest extends AbstractMailStoreTest {

    protected Random rand = new Random();

    @Test
    public void init() throws Exception {
        Path rootDir = Paths.get(getClass().getResource(".").toURI()).resolve("testRootDir" + rand.nextInt(9999));
        mailStore = new FileMailStore(rootDir);
        mailStore.set(mimeMessage);
        mailStore = new FileMailStore(rootDir);
        mailStore.init();
        assertNotNull(mailStore.get(mimeMessage.getMailId()));
    }

    @Override
    public void initializeBefore() throws Exception {
        Path rootDir = Paths.get(getClass().getResource(".").toURI()).resolve("testRootDir" + rand.nextInt(9999));
        mailStore = new FileMailStore(rootDir);
    }

    @After
    public void cleanupAfter() throws Exception {
        Path rootDir = ((FileMailStore) mailStore).getRootDir();
        removeTestDirectory(rootDir);
    }

    private void removeTestDirectory(Path rootDir) throws IOException {
        if( Files.exists(rootDir) ) {
            Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
