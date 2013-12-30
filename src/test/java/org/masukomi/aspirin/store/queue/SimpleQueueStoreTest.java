package org.masukomi.aspirin.store.queue;

public class SimpleQueueStoreTest extends AbstractQueueStoreTest {
    @Override
    public void initializeBefore() throws Exception {
        queueStore = new SimpleQueueStore();
    }
}
