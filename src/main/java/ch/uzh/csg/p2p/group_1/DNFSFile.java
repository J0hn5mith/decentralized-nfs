package ch.uzh.csg.p2p.group_1;

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
    DNFSFile(DNFSiNode iNode, DNFSPathResolver pathResolver){
        super(iNode, pathResolver);
        //TODO: Check if iNode is File!
    }
    
    /**
     * 
     * @return
     */
    public InputStream getInputStream(){
        return this.getPathResolver().getBlock((this.getINode().getBlockIDs().get(0))).getInputStream();
    }
    
}
