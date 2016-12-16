package org.ngo.ether.endpoint;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Endpoint implements EndpointCallback {

	public static void main(String[] args) {
		new Endpoint().start();
	}
	
	private EndpointHandler handler;
	private EndpointSupport client;
	private String endPointName;
	
	private int dest;
	private int testData = 1;
    
	public void start()
	{
		SocketAddress address = parseSocketAddress("192.168.0.5:60001");
		
		int port = Integer.valueOf(System.getProperty("ngo.endpoint.reg.port","1"));
		endPointName = System.getProperty("ngo.endpoint.reg.name","test");
        
		handler= new EndpointHandler(this, (short)port);
		client = new EndpointSupport(endPointName, handler);
	    
		dest = Integer.valueOf(System.getProperty("ngo.endpoint.test.to","1"));
		
        if (!client.connect( address, false)) {
        	System.out.println("failed to connect to bridge");
        }
	}
	
	
	private static SocketAddress parseSocketAddress(String s) {
        s = s.trim();
        int colonIndex = s.indexOf(":");
        if (colonIndex > 0) {
            String host = s.substring(0, colonIndex);
            int port = parsePort(s.substring(colonIndex + 1));
            return new InetSocketAddress(host, port);
        } else {
            int port = parsePort(s.substring(colonIndex + 1));
            return new InetSocketAddress(port);
        }
    }

    private static int parsePort(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Illegal port number: " + s);
        }
    }

	@Override
	public void connected() {
		System.out.println("endpoint ["+endPointName+"] connected!");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.sendMessage("<DAT>"+testData+"</DAT>", dest);
	}

	@Override
	public void disconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageReceived(String message) {
		System.out.println("endpoint ["+endPointName+"] messageRecieved:"+message);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int msg = parsePort(message.replace("<DAT>", "").replace("</DAT>", ""))+1;
		client.sendMessage("<DAT>"+msg +"</DAT>", dest);
		
		
	}

	@Override
	public void error(String message) {
		// TODO Auto-generated method stub
		
	}

}
