package org.ngo.ether.endpoint;


public interface EndpointCallback {

	void connected();

    void disconnected();

    void messageReceived(String message);

    void error(String message);

}
