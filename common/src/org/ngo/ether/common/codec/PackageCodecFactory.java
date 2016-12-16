package org.ngo.ether.common.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class PackageCodecFactory implements ProtocolCodecFactory {

	private ProtocolEncoder encoder;
    private ProtocolDecoder decoder;
    
    public PackageCodecFactory() {
        encoder = new PackageEncoder();
        decoder = new PackageDecoder();
    }
    
	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return encoder;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return decoder;
	}

}
