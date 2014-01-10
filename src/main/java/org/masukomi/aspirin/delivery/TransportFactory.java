package org.masukomi.aspirin.delivery;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

public interface TransportFactory {
    public Transport getTransport(Session session, URLName remoteServer) throws NoSuchProviderException;
}
