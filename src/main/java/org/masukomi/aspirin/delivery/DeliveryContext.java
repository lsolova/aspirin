package org.masukomi.aspirin.delivery;

import org.masukomi.aspirin.mail.MimeMessageWrapper;
import org.masukomi.aspirin.store.queue.QueueInfo;

import javax.mail.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the context of a delivery which contains all required 
 * informations used or created in the delivery process.
 * 
 * @author Laszlo Solova
 *
 */
public class DeliveryContext {
	private QueueInfo queueInfo;
	public QueueInfo getQueueInfo() {
		return queueInfo;
	}
	public DeliveryContext setQueueInfo(QueueInfo queueInfo) {
		this.queueInfo = queueInfo;
		return this;
	}
	private MimeMessageWrapper message;
	public MimeMessageWrapper getMessage() {
		return message;
	}
	public DeliveryContext setMessage(MimeMessageWrapper message) {
		this.message = message;
		return this;
	}
	private Session mailSession;
	public Session getMailSession() {
		return mailSession;
	}
	public DeliveryContext setMailSession(Session mailSession) {
		this.mailSession = mailSession;
		return this;
	}
	
	private Map<String, Object> contextVariables = new HashMap<String, Object>();
	public Map<String, Object> getContextVariables() {
		return contextVariables;
	}
	public void addContextVariable(String name, Object variable) {
		contextVariables.put(name, variable);
	}
	@SuppressWarnings("unchecked")
	public <T> T getContextVariable(String name) {
		if( contextVariables.containsKey(name) )
			return (T)contextVariables.get(name);
		return null;
	}

    private transient String ctxToString;

    @Override
    public String toString() {
        if (ctxToString == null) {
            ctxToString = getClass().getSimpleName() + " [" + "qi=" + queueInfo + "]; ";
        }
        return ctxToString;
    }

}
