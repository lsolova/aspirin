package org.masukomi.aspirin;

import org.masukomi.aspirin.mail.MimeMessageWrapper;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;

public class TestMailFactory {

    public MimeMessageWrapper createMessage(String from, String to, String subject, String text) throws Exception {
        MimeMessageWrapper mimeMessage = Aspirin.createNewMimeMessage();
        mimeMessage.addFrom(InternetAddress.parse(from));
        mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        mimeMessage.setSubject(subject);
        mimeMessage.setText(text);
        return mimeMessage;
    }
}
