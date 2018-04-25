package org.ngo.ether.endpoint;

import java.net.SocketAddress;
import java.nio.charset.Charset;

import javax.net.ssl.SSLContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.ngo.ether.common.EtherPack;
import org.ngo.ether.common.PackType;
import org.ngo.ether.common.codec.PackageCodecFactory;
import org.ngo.ether.common.ssl.BogusSslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointSupport {

	private final static Logger LOGGER = LoggerFactory.getLogger(EndpointSupport.class);

	private final IoHandler handler;
	private IoSession session;
	private final String name;

	public EndpointSupport(String name, IoHandler handler) {
		if (name == null || handler == null) {
			throw new IllegalArgumentException("Name or handler can not be null");
		}
		this.name = name;
		this.handler = handler;
		LOGGER.info("Endpoint v1.1 initialized.");
	       
	}

	public boolean connect(SocketAddress address, boolean useSsl) {
		
		if (session != null && session.isConnected()) {
			throw new IllegalStateException("Already connected. Disconnect first.");
		}

		try {
			NioSocketConnector connector = new NioSocketConnector();
			
			IoFilter loggingFilter = new LoggingFilter();
			IoFilter codecFilter = new ProtocolCodecFilter(new PackageCodecFactory());

			connector.getFilterChain().addLast("codec", codecFilter);
			connector.getFilterChain().addLast("logger", loggingFilter);

			if (useSsl) {
				SSLContext sslContext = BogusSslContextFactory.getInstance(false);
				SslFilter sslFilter = new SslFilter(sslContext);
				sslFilter.setUseClientMode(true);
				connector.getFilterChain().addFirst("sslFilter", sslFilter);
			}

			connector.setHandler(handler);
			ConnectFuture future1 = connector.connect(address);
			future1.awaitUninterruptibly();
			if (!future1.isConnected()) {
				return false;
			}
			session = future1.getSession();
			return true;
		} catch (Exception e) {
			LOGGER.error(String.format("connect to [%s] failed. error = [%s]",address.toString(), e.getMessage()));
			return false;
		}
	}
	
	public void sendMessage(String message, int from, int to) {
		EtherPack epack = new EtherPack();
		
		//lookup this endpoint's id
		//2018-04-25 fix source empty issue
		epack.setSource((short)from);
		epack.setDestination((short)to);
		epack.setType(PackType.DAT.value());
		
		//generate md5 checksum
		byte[] digest = DigestUtils.md5(message);
		epack.setChecksum(digest);
		
		epack.setLength(message.length());
		//fix charset issue.
		epack.setPayload(message.getBytes(Charset.forName("utf-8")));
		
        session.write(epack);
    }

	public void quit() {
        if (session != null) {
            if (session.isConnected()) {
            	session.closeNow();
            } 
        }
    }



}
