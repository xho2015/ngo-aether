package org.ngo.ether.common.magic;

public abstract class MagicBuilder {
	
    private static class LazyHolder {  
        private static final MagicBuilder INSTANCE = new SimpleMagicBuilder();  
    }  
   
    public static MagicBuilder getInstance() {  
        return LazyHolder.INSTANCE;  
    } 
    
    public abstract byte[] build();
    
    public abstract boolean isValid(byte[] magic);
}
