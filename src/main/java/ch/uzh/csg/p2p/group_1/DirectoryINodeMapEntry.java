package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

public class DirectoryINodeMapEntry {

    
    private Number160 iNode;
    private String name;

    
    public DirectoryINodeMapEntry(Number160 iNode, String name) {
        this.iNode = iNode;
        this.name = name;
    }

    
    public Number160 getINode() {
        return iNode;
    }


    public String getName() {
        return name;
    }

    
    @Override
    public String toString() {
        return this.getINode() + Directory.SEPARATOR + name;
    }
    
}
