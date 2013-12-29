package org.masukomi.aspirin.store.mail;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Test;
import org.masukomi.aspirin.AspirinInternal;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileMailStoreTest extends AbstractMailStoreTest {

    @Test
    public void init() throws Exception {
        Path rootDir = Paths.get("./testRootDir"+rand.nextInt(9999));
        try {
            FileMailStore fileMailStore = new FileMailStore();
            fileMailStore.setRootDir(rootDir.toFile());
            String mailid = AspirinInternal.getMailID(mimeMessage);
            fileMailStore.set(mailid, mimeMessage);
            fileMailStore = new FileMailStore();
            fileMailStore.setRootDir(rootDir.toFile());
            fileMailStore.init();
            Assert.assertNotNull(fileMailStore.get(mailid));
        } finally {
            removeTestDirectory(rootDir);
        }
    }

    @Override
    public void initializeBefore() {
        Path rootDir = Paths.get("./testRootDir"+rand.nextInt(9999));
        mailStore = new FileMailStore();
        ((FileMailStore)mailStore).setRootDir(rootDir.toFile());
    }

    @After
    public void cleanupAfter() throws Exception {
        Path rootDir = ((FileMailStore) mailStore).getRootDir().toPath();
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
