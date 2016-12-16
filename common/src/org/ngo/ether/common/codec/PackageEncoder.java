package org.ngo.ether.common.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.ngo.ether.common.EtherPack;

public class PackageEncoder extends ProtocolEncoderAdapter {

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		EtherPack epack = (EtherPack)message;
		
		int capacity = epack.capacity();
		IoBuffer buffer = IoBuffer.allocate(capacity, false);
        buffer.setAutoExpand(true);
        
        buffer.put(epack.getMagic());
        buffer.putShort(epack.getSource());
        buffer.putShort(epack.getDestination());
        buffer.put(epack.getType());
        buffer.put(epack.getChecksum());
        buffer.putInt(epack.getLength());
        buffer.put(epack.getPayload());
        
        buffer.flip();
        out.write(buffer);
	}

}
