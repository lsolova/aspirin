package org.masukomi.aspirin.store.queue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.Random;

public abstract class AbstractQueueStoreTest {

    protected QueueStore queueStore;
    protected Random rand = new Random();

    @Test
    public void add() throws Exception {
        String mailId1 = "add"+rand.nextInt(9999);
        queueStore.add(mailId1,-1L, Arrays.asList(InternetAddress.parse("aspirin1@masukomi.org,aspirin2@masukomi.org")));
        Assert.assertEquals(1,queueStore.size());
    }

    @Test
    public void isCompleted() throws Exception {
        QueueInfo qi = prepareSentMail();
        Assert.assertTrue(queueStore.isCompleted(qi.getMailid()));
    }

    @Test
    public void clean() throws Exception {
        prepareSentMail();
        queueStore.clean();
        Assert.assertEquals(1,queueStore.size());
    }

    private QueueInfo prepareSentMail() throws MessagingException {
        String mailId1 = "next1"+rand.nextInt(9999);
        queueStore.add(mailId1,-1L, Arrays.asList(InternetAddress.parse("aspirin11@masukomi.org")));
        String mailId2 = "next2"+rand.nextInt(9999);
        queueStore.add(mailId2,-1L, Arrays.asList(InternetAddress.parse("aspirin21@masukomi.org,aspirin22@masukomi.org")));
        QueueInfo qi = queueStore.next();
        qi.setState(DeliveryState.SENT);
        queueStore.setSendingResult(qi);
        return qi;
    }

    @Test
    public void next() throws Exception {
        String mailId1 = "next1"+rand.nextInt(9999);
        queueStore.add(mailId1,-1L, Arrays.asList(InternetAddress.parse("aspirin11@masukomi.org,aspirin12@masukomi.org")));
        String mailId2 = "next2"+rand.nextInt(9999);
        queueStore.add(mailId2,-1L, Arrays.asList(InternetAddress.parse("aspirin21@masukomi.org,aspirin22@masukomi.org")));
        QueueInfo qi = queueStore.next();
        Assert.assertEquals(mailId1,qi.getMailid());
    }

    @Before
    public abstract void initializeBefore() throws Exception;
}
