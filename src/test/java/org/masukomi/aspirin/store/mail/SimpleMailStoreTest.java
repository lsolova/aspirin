package org.masukomi.aspirin.store.mail;

public class SimpleMailStoreTest extends AbstractMailStoreTest {

    @Override
    public void initializeBefore() {
        mailStore = new SimpleMailStore();
    }
}
