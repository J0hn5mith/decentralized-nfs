/**
 * Created by janmeier on 29.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class DNFSPathResolver {
    final private static Logger LOGGER = Logger.getLogger(DNFSFolder.class.getName());
    private DNFSConfigurator config;
    private DNFSPeer peer;

    /**
     * 
     * @param config
     */
    public DNFSPathResolver(DNFSConfigurator config) {
        this.config = config;
        Main.LOGGER.setLevel(Level.WARN);
    }

    /**
     * 
     */
    public void setUp() {
    	
        try {
            this.peer = new DNFSPeer();
            this.peer.setUp();
        } catch (IOException e) {
            e.printStackTrace();
            Main.LOGGER.error("Failed to set up peer");
        }
        
        Main.LOGGER.info("Successfully set up connection");
    }

    public DNFSPeer getPeer() {
        return peer;
    }

    public void setPeer(DNFSPeer peer) {
        this.peer = peer;
    }

    /**
     * 
     * @param path
     * @return
     */
    public DNFSFolder getFolder(DNFSPath path){
        DNFSiNode iNode = new DNFSiNode(Number160.createHash(1));
        iNode.setDir(true);
        return new DNFSFolder(iNode, this);
    }


    /**
     * 
     * @param path
     * @return
     */
    public DNFSFile getFile(DNFSPath path){
        DNFSiNode iNode = new DNFSiNode(Number160.createHash(1000));
        return new DNFSFile(iNode, this);
    }

    /**
     * 
     * @param nodeID
     * @return
     * @throws DNFSException
     */
    public DNFSiNode getINodeByID(Number160 nodeID) throws DNFSException{
        return peer.getINode(nodeID);
    }

    /**
     * 
     * @param path
     * @return
     */
    public DNFSiNode getINode(DNFSPath path) throws DNFSException {
        return this.resolve(path);
    }

    public DNFSBlock getBlock(Number160 blockID){
        return this.peer.getBlock(blockID);
    }


    public DNFSiNode resolve(DNFSPath path) throws DNFSException {
        DNFSFolder currentFolder = this.getRootFolder();
        if (path.length() == 0){
            return currentFolder.getINode();
        }

        for (String pathComponent : path.getComponents(0, -1)) {
            currentFolder = currentFolder.getChildFolder(pathComponent);

        }

        return currentFolder.getChildINode(path.getComponent(-1));
    }


    /**
     * 
     * @throws IOException
     */
    private DNFSFolder getRootFolder() throws DNFSException{
        return new DNFSFolder(peer.getRootINode(), this);
    }
    
}

