package org.masukomi.aspirin.dns;

import junit.framework.Assert;
import org.junit.Test;

import javax.mail.URLName;
import java.util.Collection;

public class DNSResolverTests {
	
	@Test
	public void resolveMXRecords() {
		final Collection<URLName> mxRecords1 = DnsResolver.getMXRecordsForHost("gmx.net");
		Assert.assertNotNull(mxRecords1);
		Assert.assertTrue(mxRecords1.size() > 0);

		final Collection<URLName> mxRecords2 = DnsResolver.getMXRecordsForHost("green.ch");
		Assert.assertNotNull(mxRecords2);
		Assert.assertTrue(mxRecords2.size() > 0);

		final Collection<URLName> mxRecords3 = DnsResolver.getMXRecordsForHost("tschannen.cc");
		Assert.assertNotNull(mxRecords3);
		Assert.assertTrue(mxRecords3.size() > 0);
	}

}
