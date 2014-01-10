package org.masukomi.aspirin;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class TestMailFactory {

    public MimeMessage createMessage(String from, String to, String subject, String text) throws Exception {
        MimeMessage mimeMessage = Aspirin.createNewMimeMessage();
        mimeMessage.addFrom(InternetAddress.parse(from));
        mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        mimeMessage.setSubject(subject);
        mimeMessage.setText(text);
        return mimeMessage;
    }
}
