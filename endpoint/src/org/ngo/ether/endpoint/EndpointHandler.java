package org.ngo.ether.endpoint;


import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.ngo.ether.common.EtherPack;
import org.ngo.ether.common.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointHandler extends IoHandlerAdapter {

	private final static Logger LOGGER = LoggerFactory.getLogger(EndpointHandler.class);

	private final EndpointCallback callback;
	
	private short port;

	public EndpointHandler(EndpointCallback callback, short port) {
	      this.callback = callback;
	      this.port = port;
	}

	 
	/**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
    	LOGGER.warn("Unexpected exception.", cause);
        // Close connection when unexpected exception is caught.
        session.closeNow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
    	EtherPack epack = (EtherPack) message;
        byte type = epack.getType();
        PackType etype = PackType.valueOf(type);
        
        if (etype == PackType.REGA)
        {
        	//XHO? call back connected only after a REGA
        	callback.connected();  	
        }
        //keep alive, send back a KAA immediately 
    	else if (etype == PackType.KA)
        {
        	if (epack.getDestination() == port)
        	{
        		session.write(PackType.KAA.toPack(port));
        	}
        }
        //application DATA, deliver to callback
        else if (etype == PackType.DAT)
        {
        	callback.messageReceived(new String(epack.getPayload(),"UTF8"));
        }

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
    	 callback.disconnected();
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionOpened(IoSession session) throws Exception {
    	//XHO? change the "Echo" to the real one, this should be known endpoint id
    	session.write(PackType.REG.toPack(port));	
    }
}
