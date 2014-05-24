package org.masukomi.aspirin.mail;

import org.masukomi.aspirin.Aspirin;
import org.masukomi.aspirin.AspirinInternal;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a wrapper class for {@link javax.mail.internet.MimeMessage} to add support of additional headers.
 */
public class MimeMessageWrapper extends MimeMessage {

    /** This counter is used to generate unique message ids. */
    private static Integer idCounter = 0;
    private static final Object idCounterLock = new Object();

    /**
     * Formatter to set expiry header. Please, use this formatter to create or
     * change a current header.
     */
    private static final SimpleDateFormat EXPIRY_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public MimeMessageWrapper(Session session) throws MessagingException {
        super(session);
        getMailId();
    }

    public MimeMessageWrapper(Session session, InputStream is) throws MessagingException {
        super(session, is);
        getMailId();
    }

    public MimeMessageWrapper(MimeMessage mimeMessage) throws MessagingException {
        super(mimeMessage);
        getMailId();
    }

    public String getMailId() {
        String mailIdHeader = null;
        try {
        mailIdHeader = getExtraHeader(Aspirin.HEADER_MAIL_ID);
        if( mailIdHeader == null ) {
            mailIdHeader = createNewMailId();
            String headerMailId = Aspirin.HEADER_MAIL_ID;
            setExtraHeader(headerMailId, mailIdHeader);
        }

        } catch (MessagingException msge) {
            AspirinInternal.getLogger().warn(Aspirin.HEADER_MAIL_ID+" header resolving failed.",msge);
        }
        return mailIdHeader;
    }

    private String getExtraHeader(String headerName) throws MessagingException {
        String[] headers = getHeader(headerName);
        if( headers != null && 0 < headers.length )
            return headers[0];
        return null;
    }

    private void setExtraHeader(String headerName, String headerValue) throws MessagingException {
        setHeader(headerName, headerValue);
    }

    private static String createNewMailId() {
        long nowTime = System.currentTimeMillis()/1000;
        String newId = Long.toHexString(nowTime);
        int currentCounter;
        synchronized (idCounterLock) {
            currentCounter = idCounter++;
        }
        newId += "."+Integer.toHexString(currentCounter);
        return newId;
    }

    public long getExpiry() {
        long result = -1;
        try {
            String header = getExtraHeader(Aspirin.HEADER_EXPIRY);
            if( header != null ) {
                result = EXPIRY_FORMAT.parse(header).getTime()-System.currentTimeMillis();
                if( result < 0 )
                    result = 0;
            }
        } catch (Exception e) {
            AspirinInternal.getLogger().warn("Expiry header reading failed in "+this.toString(),e);
        }
        return result;
    }
    public void setExpiry(long expiry) {
        try {
            setExtraHeader(Aspirin.HEADER_EXPIRY, EXPIRY_FORMAT.format(new Date(System.currentTimeMillis() + expiry)));
        } catch (MessagingException msge) {
            AspirinInternal.getLogger().warn(Aspirin.HEADER_EXPIRY + " header setting failed.", msge);
        }
    }

    @Override
    public String toString() {
        return getMailId();
    }
}
