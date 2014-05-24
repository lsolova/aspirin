package org.masukomi.aspirin.store.queue;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.masukomi.aspirin.Aspirin;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class SqliteQueueStoreTest extends AbstractQueueStoreTest {

    public static final String MAILID = "ASD_MAILID";
    private Path sqliteQueueStorePath;

    @Override
    public void initializeBefore() throws Exception {
        sqliteQueueStorePath = Paths.get(".","testsqlite"+rand.nextInt(9999)+".db");
        Aspirin.getConfiguration().setProperty(SqliteQueueStore.PARAM_STORE_SQLITE_DB,sqliteQueueStorePath.toString());
        queueStore = new SqliteQueueStore(sqliteQueueStorePath.toAbsolutePath().toString());
        ((SqliteQueueStore)queueStore).truncate();
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

    @Test
    public void add() throws Exception {
        addItemToStore();
        assertThat(queueStore.size(), equalTo(1));
    }

    private void addItemToStore() throws MessagingException {
        List<InternetAddress> addressList = Arrays.asList(new InternetAddress("recipient@example.com"));
        queueStore.add(MAILID,Long.MAX_VALUE, addressList);
    }

    @Test
    public void isCompleted() throws Exception {
        addItemToStore();
        assertThat(queueStore.isCompleted(MAILID),equalTo(false));
    }

}
