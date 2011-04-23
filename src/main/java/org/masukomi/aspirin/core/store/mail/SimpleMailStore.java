package org.masukomi.aspirin.core.store.mail;

import java.util.HashMap;

import javax.mail.internet.MimeMessage;



/**
 * This store implementation has a simple hashmap to 
 * store all MimeMessage objects. Please, be careful: 
 * if you has a lot of objects in memory it could cause 
 * OutOfMemoryError.
 * 
 * @author Laszlo Solova
 *
 */
public class SimpleMailStore implements MailStore {
	
	private HashMap<String, MimeMessage> messageMap = new HashMap<String, MimeMessage>();
	
	@Override
	public void set(String name, MimeMessage msg) {
		messageMap.put(name, msg);
	}

	@Override
	public MimeMessage get(String name) {
		return messageMap.get(name);
	}

	@Override
	public void remove(String name) {
		messageMap.remove(name);
	}

}