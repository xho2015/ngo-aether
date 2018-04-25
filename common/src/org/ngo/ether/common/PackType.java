package org.ngo.ether.common;

public class PackType {

	public static final PackType REG = new PackType((byte)1);
	
	public static final PackType REGA = new PackType((byte)5);
	
	public static final PackType DAT = new PackType((byte)127);

	public static final PackType DNS = new PackType((byte)2);
	
	public static final PackType KA = new PackType((byte)3);
	
	public static final PackType KAA = new PackType((byte)4);
	
	private final byte num;

	private PackType(byte num) {
	        this.num = num;
	}
	
	public byte value()
	{
		return num;
	}

	public static PackType valueOf(byte value) {
		
		if (value == 1) {
			return REG;
		}
		else if (value == 5) {
			return REGA;
		}
		
		else if (value == 127) {
			return DAT;
		}
		else if (value == 3) {
			return KA;
		}
		if (value == 4) {
			return KAA;
		}

		throw new IllegalArgumentException("Unrecognized ether package type: " + value);
	}

	public Object toPack(int from, int to) {
		if (this == REG) {
			EtherPack reg = new EtherPack();
			reg.setSource((short)from);
			reg.setDestination((short)to);
			reg.setType(REG.value());
			return reg;
		}
		else if (this == REGA) {
			EtherPack rega = new EtherPack();
			rega.setSource((short)from);
			rega.setDestination((short)to);
			rega.setType(REGA.value());
			return rega;
		}
		else if (this == KA) {
			EtherPack ka = new EtherPack();
			ka.setSource((short)from);
			ka.setDestination((short)to);
			ka.setType(KA.value());
			return ka;
		}
		else if (this == KAA) {
			EtherPack kaa = new EtherPack();
			kaa.setSource((short)from);
			kaa.setDestination((short)to);
			kaa.setType(KAA.value());
			return kaa;
		}
		return null;
	}

	public static Object toString(byte type) {
		switch (type)
		{
		case 1:
			return "REG";
		case 3:
			return "KA";
		case 4:
			return "KAA";
		case 5:
			return "REGA";
		case 127:
			return "DAT";
		}
		return null;
	}
}
