package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by janmeier on 02.04.15.
 */


public class DNFSFile extends DNFSAbstractFile {
	

    /**
     * 
     * @param iNode
     */
    DNFSFile(DNFSiNode iNode, DNFSIPeer peer){
        super(iNode, peer);
        this.getINode().addBlock(this.getPeer());
    }

    public static DNFSFile createNewFile(DNFSIPeer peer){
        return new DNFSFile(peer.createINode(), peer);
    }

    /**
     * 
     * @return
     */
    public InputStream getInputStream(){
        return this.getPeer().getBlock((this.getINode().getBlockIDs().get(0))).getInputStream();
    }
    
}
