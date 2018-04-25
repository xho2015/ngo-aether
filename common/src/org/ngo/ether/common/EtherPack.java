package org.ngo.ether.common;

import org.ngo.ether.common.magic.MagicBuilder;

public class EtherPack {
	
	/**
	 *  XHO: date length table
	 *  -----------------
	 *  byte 	1
	 *  short 	2
	 *  int		4
	 *  long	8
	 *  char	2
	 */
	
	//2 bytes
	private byte[] magic = MagicBuilder.getInstance().build();
	//2 bytes
	private short source;
	//2 bytes
	private short destination;
	//1 byte
	private byte type;
	//16 bytes
	private byte[] checksum;
	//4 bytes
	private int length;
	//variable
	private byte[] payload;
	
	public final static byte [] NULL_CHECKSUM = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; 
	private final static byte [] NULL_PAYLOAD = {}; 
	
	
	public short getSource() {
		return source;
	}

	public void setSource(short source) {
		this.source = source;
	}	

	public byte[] getMagic() {
		return magic;
	}

	public short getDestination() {
		return destination;
	}
	public void setDestination(short destination) {
		this.destination = destination;
	}
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	public byte[] getChecksum() {
		return checksum == null ? NULL_CHECKSUM : checksum;
	}
	public void setChecksum(byte[] checksum) {
		this.checksum = checksum;
	}
	public int getLength() {
		return (payload == null ? 0 : payload.length);
	}
	public void setLength(int length) {
		this.length = length;
	}
	public byte[] getPayload() {
		return payload == null ? NULL_PAYLOAD : payload;
	}
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	/**
	 * 	2 bytes magic;
		2 bytes source;
		2 bytes	destination;
		1 byte	type;
		16 bytes checksum;
		4 bytes	length;
	 * @return
	 */
	public static int headerSize(){
		return 2+2+2+1+16+4 ;
	}
	
	public int capacity() {
		return headerSize() + getLength();
	}
	
	public String toString()
	{
		return String.format("EtherPack[type=%s, src=%d, dest=%d]", PackType.toString(type), this.source, this.destination);
	}
	
}
