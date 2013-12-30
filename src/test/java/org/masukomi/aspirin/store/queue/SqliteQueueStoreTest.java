package org.masukomi.aspirin.store.queue;

import org.junit.After;
import org.masukomi.aspirin.Aspirin;

import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SqliteQueueStoreTest extends AbstractQueueStoreTest {

    private Path sqliteQueueStorePath;

    @Override
    public void initializeBefore() throws Exception {
        sqliteQueueStorePath = Paths.get(".","testsqlite"+rand.nextInt(9999)+".db");
        Aspirin.getConfiguration().setProperty(SqliteQueueStore.PARAM_STORE_SQLITE_DB,sqliteQueueStorePath.toString());
        queueStore = new SqliteQueueStore();
    }

    @After
    public void cleanupAfter() throws Exception {
        queueStore = null;
        System.gc();
        try {
            Files.delete(sqliteQueueStorePath);
        } catch (FileSystemException fse) {
            Thread.sleep(1000);
            Files.delete(sqliteQueueStorePath);
        }
    }
}
