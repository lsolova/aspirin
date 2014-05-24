package org.masukomi.aspirin;

import org.masukomi.aspirin.config.Configuration;
import org.masukomi.aspirin.delivery.DeliveryManager;
import org.masukomi.aspirin.listener.AspirinListener;
import org.masukomi.aspirin.listener.ListenerManager;
import org.masukomi.aspirin.mail.MimeMessageWrapper;
import org.slf4j.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Inside factory and part provider class.
 * 
 * @author Laszlo Solova
 *
 */
public class AspirinInternal {
	

	/** This session is used to generate new MimeMessage objects. */
	private static volatile Session defaultSession = null;
	
	/** Configuration object of Aspirin. */
	private static Configuration configuration = Configuration.getInstance();
	/** AspirinListener management object. Create on first request. */
	private static volatile ListenerManager listenerManager = null;
	/** Delivery and QoS service management. Create on first request. */
	private static volatile DeliveryManager deliveryManager = new DeliveryManager();
	
	/**
	 * You can get configuration object, which could be changed to set up new 
	 * values. Please use this method to set up your Aspirin instance. Of 
	 * course default values are enough to simple mail sending.
	 * 
	 * @return Configuration object of Aspirin
	 */
	public static Configuration getConfiguration() {
		return configuration;
	}
	
	/**
	 * Add MimeMessage to deliver it.
	 * @param msg MimeMessage to deliver.
	 * @throws MessagingException If delivery add failed.
	 */
	protected static void add(MimeMessage msg) throws MessagingException {
		if( !deliveryManager.isAlive() )
			deliveryManager.start();
		deliveryManager.add(convert(msg));
	}
	
	/**
	 * Add MimeMessage to delivery.
	 * @param msg MimeMessage
	 * @param expiry Expiration of this email in milliseconds from now.
	 * @throws MessagingException If delivery add failed.
	 */
	public static void add(MimeMessage msg, long expiry) throws MessagingException {
        MimeMessageWrapper wrappedMsg = convert(msg);
        if( 0 < expiry )
            wrappedMsg.setExpiry(expiry);
		add(wrappedMsg);
	}
	
	/**
	 * Add mail delivery status listener.
	 * @param listener AspirinListener object
	 */
	public static void addListener(AspirinListener listener) {
		if( listenerManager == null )
			listenerManager = new ListenerManager();
		listenerManager.add(listener);
	}
	
	/**
	 * Remove an email from delivery.
	 * @param mailid Unique Aspirin ID of this email.
	 * @throws MessagingException If removing failed.
	 */
	public static void remove(String mailid) throws MessagingException {
		deliveryManager.remove(mailid);
	}
	
	/**
	 * Remove delivery status listener.
	 * @param listener AspirinListener
	 */
	public static void removeListener(AspirinListener listener) {
		if( listenerManager != null )
			listenerManager.remove(listener);
	}
	
	/**
	 * It creates a new MimeMessage with standard Aspirin ID header.
	 * 
	 * @return new MimeMessage object
     * @throws javax.mail.MessagingException if message creation failed
	 * 
	 */
	public static MimeMessageWrapper createNewMimeMessage() throws MessagingException {
		if( defaultSession == null )
			defaultSession = Session.getDefaultInstance(System.getProperties());
		return new MimeMessageWrapper(defaultSession);
	}
	
	public static Collection<InternetAddress> extractRecipients(MimeMessage message) throws MessagingException {
		Collection<InternetAddress> recipients = new ArrayList<>();
		
		Address[] addresses;
		Message.RecipientType[] types = new Message.RecipientType[]{
				RecipientType.TO,
				RecipientType.CC,
				RecipientType.BCC
		};
		for( Message.RecipientType recType : types )
		{
			addresses = message.getRecipients(recType);
			if (addresses != null)
			{
				for (Address addr : addresses)
				{
					try {
						recipients.add((InternetAddress)addr);
					} catch (Exception e) {
						getLogger().warn("Recipient parsing failed.", e);
					}
				}
			}
		}
		return recipients;
	}
	
	/**
	 * Decode mail ID from MimeMessage. If no such header was defined, then we 
	 * get MimeMessage's toString() method result back.
	 * 
	 * @param message MimeMessage, which ID needs.
	 * @return An unique mail id associated to this MimeMessage.
     *
     * @deprecated Use MimeMessageWrapper.getMailId() method instead.
	 */
    @Deprecated
	public static String getMailID(MimeMessage message) {
		if( message instanceof MimeMessageWrapper ) {
            return ((MimeMessageWrapper)message).getMailId();
        }
		return message.toString();
	}

    public static Logger getLogger() {
		if( configuration == null ) { return null; }
		return configuration.getLogger();
	}
	
	public static DeliveryManager getDeliveryManager() {
		return deliveryManager;
	}
	
	public static ListenerManager getListenerManager() {
		return listenerManager;
	}
	
	public static void shutdown() {
		deliveryManager.shutdown();
	}

    private static MimeMessageWrapper convert(MimeMessage mimeMessage) throws MessagingException{
        if( mimeMessage instanceof MimeMessageWrapper ) {
            return MimeMessageWrapper.class.cast(mimeMessage);
        }
        return new MimeMessageWrapper(mimeMessage);
    }

}
