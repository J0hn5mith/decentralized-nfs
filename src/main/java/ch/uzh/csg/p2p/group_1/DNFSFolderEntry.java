package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

public class DNFSFolderEntry {

    
    private Number160 iNodeKey;
    private String name;

    
    /**
     * 
     * @param line
     */
    public DNFSFolderEntry(String line) {
    }

    
    /**
     * 
     * @param iNodeKey
     * @param name
     */
    public DNFSFolderEntry(Number160 iNodeKey, String name) {
        this.iNodeKey = iNodeKey;
        this.name = name;
    }

    
    /**
     * 
     * @return
     */
    public Number160 getINodeKey() {
        return iNodeKey;
    }

    
    /**
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    
    /**
     * 
     */
    @Override
    public String toString() {
        return this.getINodeKey() + DNFSFolder.SEPARATOR + name;
    }
    
}
