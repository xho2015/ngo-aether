package org.ngo.ether.bridge;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.ngo.ether.common.codec.PackageCodecFactory;
import org.ngo.ether.common.ssl.BogusSslContextFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Bridge {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(Bridge.class);

	public static void main(String[] args) {
		
		NioSocketAcceptor acceptor = new NioSocketAcceptor();
        
        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();

        // Inject the SSL filter
        boolean ssl = Boolean.valueOf(System.getProperty("ngo.bridge.secure","false"));
        
        if (ssl)
        {
        	SslFilter sslFilter;
			try {
				sslFilter = new SslFilter(BogusSslContextFactory.getInstance(true), false);
				chain.addLast("sslFilter", sslFilter);	
			} catch (GeneralSecurityException e) {
				LOGGER.error(e.getMessage());
			}
        }

        // The logger, if needed. Commented atm
        chain.addLast("logger", new LoggingFilter());

        // the Ether Package codec filter
        IoFilter codecFilter = new ProtocolCodecFilter(new PackageCodecFactory());
        chain.addLast("codec", codecFilter);
        
        //XHO: ExecutorFilter, http://mina.apache.org/mina-project/xref/org/apache/mina/filter/executor/ExecutorFilter.html
        // Use one thread pool for most events.
        int worker = Integer.valueOf(System.getProperty("ngo.bridge.worker","6"));
        chain.addLast("executor1", new ExecutorFilter(worker));

        IoHandler handler = new BridgeHandler();
        acceptor.setHandler(handler);
        
        //idle time in second
        int idle = Integer.valueOf(System.getProperty("ngo.bridge.idle","30"));
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, idle);
        
        int port = Integer.valueOf(System.getProperty("ngo.bridge.port","60001"));
        try {
			acceptor.bind(new InetSocketAddress("192.168.0.5",port));
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}    
	}

}
