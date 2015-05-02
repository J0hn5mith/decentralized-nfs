package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by janmeier on 02.04.15.
 */


public class DNFSFile extends DNFSAbstractFile {
    static String LOREM_IPSUM = "al;skdjfl;aksjdfl;aksjdfl;aksjdf;laksjdflkjashdfklajhsdfklajshdfklasjdh";


    /**
     * 
     * @param iNode
     */
    DNFSFile(DNFSiNode iNode, DNFSIPeer peer){
        super(iNode, peer);
        DNFSBlock block = this.getINode().addBlock(this.getPeer());
        block.append(LOREM_IPSUM);
    }

    public static DNFSFile createNew(DNFSIPeer peer){
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
