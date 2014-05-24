package org.masukomi.aspirin;

import org.masukomi.aspirin.config.Configuration;
import org.masukomi.aspirin.listener.AspirinListener;
import org.masukomi.aspirin.mail.MimeMessageWrapper;
import org.masukomi.aspirin.store.mail.MailStore;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * This is the facade class of the Aspirin package. You should to use this class to manage email sending.
 * 
 * <h2>How it works?</h2>
 * 
 * <p>All email is represented by two main object:</p>
 * 
 * <p>A {@link org.masukomi.aspirin.mail.MimeMessageWrapper}, which contains the RAW content of an email, so it could be
 * very large. The original {@link javax.mail.internet.MimeMessage} object is extended to handle additional headers
 * easier, this is the MimeMessageWrapper.
 *
 * Messages are stored in a {@link MailStore} (there is two different implementation in Aspirin - one for simple
 * in-memory usage {@link org.masukomi.aspirin.store.mail.SimpleMailStore} and one for heavy usage
 * {@link org.masukomi.aspirin.store.mail.FileMailStore}, this stores all message objects on filesystem.) If no one of
 * these default stores is enough for you, you can implement a new one based on the MailStore interface.</p>
 * 
 * <p>A {@link org.masukomi.aspirin.store.queue.QueueInfo}, which represents an email-recipient pair, so one message
 * object could be associated to more QueueInfo objects. This is an inside object, which contains all control
 * informations of a mail item. In Aspirin package there is a {@link org.masukomi.aspirin.store.queue.QueueStore} for
 * in-memory use {@link org.masukomi.aspirin.store.queue.SimpleQueueStore}, this is the default implementation to store
 * QueueInfo objects. An other QueueStore example is implemented, but requires an additional SQL JDBC package (like the
 * SQLite example based on <a href="http://sqljet.com">SQLJet</a>.</p>
 * 
 * <p><b>Hint:</b> If you need a Quality-of-Service (QoS) mail sending, use
 * {@link org.masukomi.aspirin.store.mail.FileMailStore} and {@link org.masukomi.aspirin.store.queue.SqliteQueueStore},
 * they could preserve emails in queue between runs or restore after a crash.</p>
 * 
 * @author Laszlo Solova
 *
 */
public class Aspirin {
	
	/**
	 * Name of ID header placed in MimeMessageWrapper object. If it is not defined already, then we generate a new one.
	 */
	public static final String HEADER_MAIL_ID = "X-Aspirin-MailID";
	
	/**
	 * Name of expiration time header placed in MimeMessageWrapper object. Default expiration time is -1, unlimited.
     * Expiration time is an epoch timestamp in milliseconds.
	 */
	public static final String HEADER_EXPIRY = "X-Aspirin-Expiry";

	/**
	 * Add MimeMessage to deliver it.
	 * @param msg MimeMessageWrapper to deliver.
	 * @throws MessagingException If delivery add failed.
	 */
	public static void add(MimeMessage msg) throws MessagingException {
		AspirinInternal.add(msg,-1);
	}
	
	/**
	 * Add MimeMessage to delivery.
	 * @param msg MimeMessage
	 * @param expiry Expiration of this email in milliseconds from now.
	 * @throws MessagingException If delivery add failed.
	 */
	public static void add(MimeMessage msg, long expiry) throws MessagingException {
		AspirinInternal.add(msg, expiry);
	}
	
	/**
	 * Add mail delivery status listener.
	 * @param listener AspirinListener object
	 */
	public static void addListener(AspirinListener listener) {
		AspirinInternal.addListener(listener);
	}
	
	/**
	 * It creates a new MimeMessage with standard Aspirin ID header.
	 * 
	 * @return new MimeMessage object
	 * @throws javax.mail.MessagingException if something went wrong
	 */
	public static MimeMessageWrapper createNewMimeMessage() throws MessagingException {
		return AspirinInternal.createNewMimeMessage();
	}
	
	/**
	 * You can get configuration object, which could be changed to set up new 
	 * values. Please use this method to set up your Aspirin instance. Of 
	 * course default values are enough to simple mail sending.
	 * 
	 * @return Configuration object of Aspirin
	 */
	public static Configuration getConfiguration() {
		return AspirinInternal.getConfiguration();
	}
	
	/**
	 * Remove an email from delivery.
	 * @param mailid Unique Aspirin ID of this email.
	 * @throws MessagingException If removing failed.
	 */
	public static void remove(String mailid) throws MessagingException {
		AspirinInternal.remove(mailid);
	}
	
	/**
	 * Remove delivery status listener.
	 * @param listener AspirinListener
	 */
	public static void removeListener(AspirinListener listener) {
		AspirinInternal.removeListener(listener);
	}
	
	/**
	 * Call on shutting down your system. All aspirin processes will be 
	 * shutdown as recommended.
	 */
	public static void shutdown() {
		AspirinInternal.shutdown();
	}

}
