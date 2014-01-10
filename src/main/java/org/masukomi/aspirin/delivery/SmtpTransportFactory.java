package org.masukomi.aspirin.delivery;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

public class SmtpTransportFactory implements TransportFactory {
    public Transport getTransport(Session session, URLName outgoingMailServer) throws NoSuchProviderException {
        return session.getTransport(outgoingMailServer);
    }
}
