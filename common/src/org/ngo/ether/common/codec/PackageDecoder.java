package org.ngo.ether.common.codec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.ngo.ether.common.EtherPack;
import org.ngo.ether.common.magic.MagicBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageDecoder extends CumulativeProtocolDecoder {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(PackageDecoder.class);

	private static final String DECODER_STATE_KEY = PackageDecoder.class.getName() + ".STATE";

	//XHO: ? destingush the max package size
    public static final int MAX_PACKAGE_SIZE = 1 * 1024 * 1024;

    private static class DecoderState {
        EtherPack epack;
    }
    
    /**
     * XHO:MINA ensures that there will never be more than one thread simultaneously executing the decode() function for the same IoSession,
     *  but it does not guarantee that it will always be the same thread
     *  
     *  http://mina.apache.org/mina-project/userguide/ch9-codec-filter/ch9-codec-filter.html
     */
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {

		DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
        if (decoderState == null) {
            decoderState = new DecoderState();
            session.setAttribute(DECODER_STATE_KEY, decoderState);
        }
        
        if (decoderState.epack == null) {
            // try to read header
            if (in.remaining() >= EtherPack.headerSize()) {
                byte[] magic = new byte[2];
            	magic[0] = in.get();
                magic[1] = in.get();
                
                if (!MagicBuilder.getInstance().isValid(magic))
                {
                	//XHO: ? how to handle the situation that magic number does not match
                	//normally, the session should be closed immediately.
                	LOGGER.warn(String.format("Magic number is not valid. close the session [%s]",session.getRemoteAddress().toString()));
                	session.closeNow();
                }
                
                //source port & IP
                short src = in.getShort();
                
                //dest port & IP
                short dest = in.getShort();
                
                //type
                byte type = in.get();
                
                //checksum
                byte[] checksum = new byte[16];
                in.get(checksum);
                
                EtherPack epack = new EtherPack();
                epack.setSource(src);
                epack.setDestination(dest);
            	epack.setType(type);
            	epack.setChecksum(checksum);
            	
            	//cache the package in session
            	decoderState.epack = epack;
            	
            	//try to read payload
                if (in.prefixedDataAvailable(4, MAX_PACKAGE_SIZE)) {
                	readPayload(in, out, decoderState, session);
                	return true;
                } 
                
            } else {
                // no enough data available to read header
                return false;
            }
        } 
        else // this case is for payload arrived
        {
        	if (in.prefixedDataAvailable(4, MAX_PACKAGE_SIZE)) {
        		readPayload(in, out, decoderState, session);
            	return true;
            }
        }
        
        return false;
	}
	
	/**
	 * read the payload from the IoBuffer
	 * @param in
	 * @param out
	 * @param decoderState
	 */
	private void readPayload(IoBuffer in, ProtocolDecoderOutput out, DecoderState decoderState, IoSession session)
	{
		int length =  in.getInt();
    	byte[] payload = new byte[length];
    	in.get(payload);
    	
    	//XHO? validate checksum
    	if (!Arrays.equals(decoderState.epack.getChecksum(), EtherPack.NULL_CHECKSUM))
    	{
    			byte[] digest = DigestUtils.md5(new String(payload,Charset.forName("UTF8")));
				if (!Arrays.equals(digest,decoderState.epack.getChecksum()))
				{
					LOGGER.warn(String.format("checksum is not valid. close the session [%s]",session.getRemoteAddress().toString()));
                	session.closeNow();
                	return;
				}	
			
    	}
    	
    	//populate ether package
    	decoderState.epack.setLength(length);
    	decoderState.epack.setPayload(payload);
    	
    	out.write(decoderState.epack);
    	decoderState.epack = null;
	}

}
