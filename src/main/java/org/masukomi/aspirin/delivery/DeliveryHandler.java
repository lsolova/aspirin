package org.masukomi.aspirin.delivery;

import org.masukomi.aspirin.config.Configuration;

/**
 * This interface defines an atomic part of delivery chain.
 * A DeliveryHandler is a particular task in the delivery chain. You can create 
 * your own chain in the {@link Configuration}.
 * 
 * @author Laszlo Solova
 *
 */
public interface DeliveryHandler {
	public void handle(DeliveryContext dCtx) throws DeliveryException;
}
