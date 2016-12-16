package org.ngo.ether.common.magic;

public class SimpleMagicBuilder extends MagicBuilder {

	private final static byte MAGIC1 = (byte)0xBE;
	private final static byte MAGIC2 = (byte)0xA1;
	
	private final static byte[] magic =  {MAGIC1, MAGIC2};

	@Override
	public byte[] build() {
		return magic;
	}

	@Override
	public boolean isValid(byte[] magic) {
		return magic != null ? 
				(magic.length == 2 ? 
					(magic[0] == MAGIC1 ?
							(magic[1] == MAGIC2 ? true : false)
					:		
					false)
				:false)
			:false;
	}

}
