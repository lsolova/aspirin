package org.masukomi.aspirin.store.mail;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.masukomi.aspirin.AspirinInternal;
import org.masukomi.aspirin.TestMailFactory;

import javax.mail.internet.MimeMessage;
import java.util.List;

public abstract class AbstractMailStoreTest {

    protected MailStore mailStore;
    protected MimeMessage mimeMessage;
    protected MimeMessage mimeMessage2;

    @Test
    public void set() throws Exception {
        String msgid1 = AspirinInternal.getMailID(mimeMessage);
        mailStore.set(mimeMessage);
        Assert.assertEquals(mimeMessage, mailStore.get(msgid1));
    }

    @Test
    public void remove() throws Exception {
        String msgid1 = AspirinInternal.getMailID(mimeMessage);
        mailStore.set(mimeMessage);
        mailStore.remove(msgid1);
        Assert.assertNull(mailStore.get(msgid1));
    }

    @Test
    public void getMailIds() throws Exception {
        String msgid1 = AspirinInternal.getMailID(mimeMessage);
        mailStore.set(mimeMessage);
        String msgid2 = AspirinInternal.getMailID(mimeMessage2);
        mailStore.set(mimeMessage2);
        List<String> mailIds = mailStore.getMailIds();
        Assert.assertTrue(mailIds.contains(msgid1) && mailIds.contains(msgid2));
        mailStore.remove(msgid1);
        List<String> mailIds2 = mailStore.getMailIds();
        Assert.assertTrue(!mailIds2.contains(msgid1) && mailIds2.contains(msgid2));
    }

    @Before
    public void commonInitializeBefore() throws Exception {
        TestMailFactory testMailFactory = new TestMailFactory();
        mimeMessage = testMailFactory.createMessage("aspirin@masukomi.org", "aspirin@masukomi.org", "Testing email 1", "Lorem ipsum dolor sit amet." );
        mimeMessage2 = testMailFactory.createMessage("aspirin2@masukomi.org", "aspirin2@masukomi.org", "Testing email 2", "Lorem ipsum dolor sit amet." );
        initializeBefore();
    }

    public abstract void initializeBefore();

}
