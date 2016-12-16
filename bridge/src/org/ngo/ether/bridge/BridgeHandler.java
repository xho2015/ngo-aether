package org.ngo.ether.bridge;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.ngo.ether.common.Constants;
import org.ngo.ether.common.EtherPack;
import org.ngo.ether.common.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BridgeHandler extends IoHandlerAdapter {

	private final static Logger LOGGER = LoggerFactory.getLogger(BridgeHandler.class);

	
	private static final Map<Short, List<IoSession>> DNS = new ConcurrentHashMap<Short,List<IoSession>>();
	
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		LOGGER.debug(String.format("new session opened, address=%s", session.getRemoteAddress()));	
		session.setAttribute(Constants.ATTR_KA_STATE, 0);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		Short port = (Short) session.getAttribute(Constants.ATTR_PORT);
		synchronized(DNS)
		{
			List<IoSession> connections = DNS.get(port);
			connections.remove(session);
		}
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		Integer state = (Integer)session.getAttribute(Constants.ATTR_KA_STATE);
		Short port = (Short)session.getAttribute(Constants.ATTR_PORT);
		
		if (state == 0)
		{
			session.write(PackType.KA.toPack(port));
			session.setAttribute(Constants.ATTR_KA_STATE, 1);
		}
		else if (state == 1)
		{
			LOGGER.debug(String.format("broken session to be closed now, port=%s, address=%s", session.getAttribute(Constants.ATTR_PORT), session.getRemoteAddress()));
			session.closeNow();
		}	
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		LOGGER.warn("Unexpected exception.", cause);
        // Close connection when unexpected exception is caught.
        session.closeNow();
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		
		//revert the KA status to normal status
		session.setAttribute(Constants.ATTR_KA_STATE, 0);
		
		EtherPack epack = (EtherPack)message;
		PackType type = PackType.valueOf(epack.getType());
		
		LOGGER.debug("messageReceived:"+epack);

		if (type == PackType.REG)
		{
			short port = epack.getDestination();
			synchronized(DNS)
			{
				List<IoSession> connections = DNS.get(port);
				if (connections == null)
				{
					connections = new LinkedList<IoSession>();
					DNS.put(port, connections);
				}
				
				IoSession stale = null;
				for (IoSession s : connections)
				{
					if (s.getRemoteAddress().equals(session.getRemoteAddress()))
						stale = s;
				}
				
				if (stale != null)
					connections.remove(stale);
				
				//appended to the end of the list
				connections.add(session);
				session.setAttribute(Constants.ATTR_PORT, port);
				LOGGER.debug(String.format("new session registered, port=%s, address=%s", port, session.getRemoteAddress()));
				
				//send out a REGA immediately
				session.write(PackType.REGA.toPack(port));
			}		
		}
		else if (type == PackType.KAA)
		{
			//duplicated 
			//session.setAttribute(Constants.ATTR_KA_STATE, 0);
		}
		else if (type == PackType.DAT)
		{
			//do data forwarding
			short port = epack.getDestination();
			//XHO? really need to do synchronize here?
			synchronized (DNS) {
				List<IoSession> connections = DNS.get(port);
				if (connections != null)
		            for (IoSession s : connections) {
		                if (s.isConnected()) {
		                	LOGGER.debug(String.format(">>>, port=%s, address=%s", port, s.getRemoteAddress()));
		                    s.write(epack);
		                }
		            }
	        }
		}
	}

}
