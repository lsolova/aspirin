package org.masukomi.aspirin.store.mail;

import org.masukomi.aspirin.mail.MimeMessageWrapper;

import java.util.List;

/**
 * This store contain all MimeMessage instances. This is useful, when we want to reduce memory usage, because we can
 * store all MimeMessage objects in files or in RDBMS or in other places, instead of memory.
 *
 */
public interface MailStore {
	public MimeMessageWrapper get(String mailid);
	public List<String> getMailIds();
	public void init();
	public void remove(String mailid);
	public void set(MimeMessageWrapper msg);
}
