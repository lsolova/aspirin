package org.masukomi.aspirin.store.mail;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.masukomi.aspirin.Aspirin;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Random;

public abstract class AbstractMailStoreTest {

    protected MailStore mailStore;
    protected MimeMessage mimeMessage;
    protected Random rand = new Random();

    @Test
    public void set() throws Exception {
        String msgid1 = "set"+rand.nextInt(9999);
        mailStore.set(msgid1,mimeMessage);
        Assert.assertEquals(mimeMessage, mailStore.get(msgid1));
    }

    @Test
    public void remove() throws Exception {
        String msgid1 = "remove"+rand.nextInt(9999);
        mailStore.set(msgid1,mimeMessage);
        mailStore.remove(msgid1);
        Assert.assertNull(mailStore.get(msgid1));
    }

    @Test
    public void getMailIds() throws Exception {
        String msgid1 = "mailid1"+rand.nextInt(9999);
        mailStore.set(msgid1,mimeMessage);
        String msgid2 = "mailid2"+rand.nextInt(9999);
        mailStore.set(msgid2,mimeMessage);
        List<String> mailIds = mailStore.getMailIds();
        Assert.assertTrue(mailIds.contains(msgid1) && mailIds.contains(msgid2));
        mailStore.remove(msgid1);
        List<String> mailIds2 = mailStore.getMailIds();
        Assert.assertTrue(!mailIds2.contains(msgid1) && mailIds2.contains(msgid2));
    }

    @Before
    public void commonInitializeBefore() throws Exception {
        mimeMessage = Aspirin.createNewMimeMessage();
        mimeMessage.addFrom(InternetAddress.parse("aspirin@masukomi.org"));
        mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse("aspirin@masukomi.org"));
        mimeMessage.setSubject("Testing email "+rand.nextInt(9999));
        mimeMessage.setText("Lorem ipsum dolor sit amet.");
        initializeBefore();
    }

    public abstract void initializeBefore();

}
