package org.masukomi.aspirin.delivery;

import org.junit.Assert;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;
import org.masukomi.aspirin.TestMailFactory;
import org.masukomi.aspirin.mail.MimeMessageWrapper;
import org.masukomi.aspirin.store.queue.DeliveryState;
import org.masukomi.aspirin.store.queue.QueueInfo;

import javax.mail.Message;
import javax.mail.URLName;
import java.util.ArrayList;
import java.util.List;

public class SendMessageTest {

    public static final String RECIPIENT_ADDRESS = "aspirin2@masukomi.org";
    public static final String FROM_ADDRESS = "aspirin@masukomi.org";
    public static final String MAIL_SUBJECT = "Test mail 1";
    public static final String MAIL_TEXT = "Lorem ipsum dolor sit amet.";

    @Test
    public void sendMessage() throws Exception {
        DeliveryContext deliveryContext = new DeliveryContext();

        List<URLName> urlNameList = new ArrayList<>();
        urlNameList.add(new URLName("smtp://masukomi.org"));
        deliveryContext.addContextVariable("targetservers", urlNameList);

        MimeMessageWrapper message = new TestMailFactory().createMessage(FROM_ADDRESS, RECIPIENT_ADDRESS, MAIL_SUBJECT, MAIL_TEXT);
        deliveryContext.setMessage(message);

        QueueInfo qi = new QueueInfo();
        qi.setMailid(message.getMailId());
        qi.setRecipient(message.getRecipients(Message.RecipientType.TO)[0].toString());
        qi.setState(DeliveryState.QUEUED);
        deliveryContext.setQueueInfo(qi);

        MockTransportFactory mockTransportFactory = new MockTransportFactory();
        SendMessage sendMessage = new SendMessage().setTransportFactory(mockTransportFactory);
        sendMessage.handle(deliveryContext);

        Mailbox mb = Mailbox.get(RECIPIENT_ADDRESS);
        Assert.assertEquals(1,mb.getNewMessageCount());
        Message m = mb.get(0);
        Assert.assertEquals(FROM_ADDRESS,m.getFrom()[0].toString());
        Assert.assertEquals(MAIL_SUBJECT,m.getSubject());
        Assert.assertEquals(MAIL_TEXT,m.getContent());
    }
}
