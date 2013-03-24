package org.masukomi.aspirin.listener;

public enum ResultState {
	FAILED, // Delivery to a recipient failed
	FINISHED, // Delivery to all recipient finished
	SENT // Delivery to a recipient successed
}
