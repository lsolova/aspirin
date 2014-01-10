package org.masukomi.aspirin.delivery;

import org.jvnet.mock_javamail.MockTransport;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

public class MockTransportFactory implements TransportFactory {
    @Override
    public Transport getTransport(Session session, URLName remoteServer) throws NoSuchProviderException {
        return new MockTransport(session,remoteServer);
    }
}
