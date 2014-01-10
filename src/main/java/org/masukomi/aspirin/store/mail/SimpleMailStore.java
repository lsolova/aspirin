package org.masukomi.aspirin.store.mail;

import org.masukomi.aspirin.AspirinInternal;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



/**
 * This store implementation has a simple HashMap to store all MimeMessage objects. Please, be careful:
 * if you has a lot of objects in memory it could cause OutOfMemoryError.
 *
 */
public class SimpleMailStore implements MailStore {
	
	private Map<String, MimeMessage> messageMap = new ConcurrentHashMap<>();
	

	@Override
	public MimeMessage get(String mailid) {
		return messageMap.get(mailid);
	}
	
	@Override
	public List<String> getMailIds() {
		return new ArrayList<>(messageMap.keySet());
	}
	
	@Override
	public void init() {
		// Do nothing	
	}

	@Override
	public void remove(String mailid) {
		messageMap.remove(mailid);
	}

	@Override
	public void set(MimeMessage msg) {
		messageMap.put(AspirinInternal.getMailID(msg), msg);
	}

}
