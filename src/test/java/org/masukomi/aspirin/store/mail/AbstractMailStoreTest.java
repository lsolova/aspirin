package org.masukomi.aspirin.store.mail;

import static org.hamcrest.CoreMatchers.*;
import org.junit.Before;
import org.junit.Test;
import org.masukomi.aspirin.TestMailFactory;
import org.masukomi.aspirin.mail.MimeMessageWrapper;

import javax.mail.internet.MimeMessage;
import java.util.List;

import static org.junit.Assert.*;

public abstract class AbstractMailStoreTest {

    protected MailStore mailStore;
    protected MimeMessageWrapper mimeMessage;
    protected MimeMessageWrapper mimeMessage2;

    @Test
    public void set() throws Exception {
        mailStore.set(mimeMessage);
        assertThat(mailStore.get(mimeMessage.getMailId()), equalTo((MimeMessage) mimeMessage));
    }

    @Test
    public void remove() throws Exception {
        mailStore.set(mimeMessage);
        mailStore.remove(mimeMessage.getMailId());
        assertNull(mailStore.get(mimeMessage.getMailId()));
    }

    @Test
    public void getMailIds() throws Exception {
        String msgid1 = mimeMessage.getMailId();
        mailStore.set(mimeMessage);
        String msgid2 = mimeMessage2.getMailId();
        mailStore.set(mimeMessage2);
        List<String> mailIds = mailStore.getMailIds();
        assertTrue(mailIds.contains(msgid1) && mailIds.contains(msgid2));
        mailStore.remove(msgid1);
        List<String> mailIds2 = mailStore.getMailIds();
        assertTrue(!mailIds2.contains(msgid1) && mailIds2.contains(msgid2));
    }

    @Before
    public void commonInitializeBefore() throws Exception {
        TestMailFactory testMailFactory = new TestMailFactory();
        mimeMessage = testMailFactory.createMessage("aspirin@masukomi.org", "aspirin@masukomi.org", "Testing email 1", "Lorem ipsum dolor sit amet." );
        mimeMessage2 = testMailFactory.createMessage("aspirin2@masukomi.org", "aspirin2@masukomi.org", "Testing email 2", "Lorem ipsum dolor sit amet." );
        initializeBefore();
    }

    public abstract void initializeBefore() throws Exception;

}
