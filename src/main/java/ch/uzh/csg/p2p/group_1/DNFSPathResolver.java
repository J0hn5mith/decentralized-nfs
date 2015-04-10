/**
 * Created by janmeier on 29.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Level;

public class DNFSPathResolver {

    private DNFSConfigurator config;
    private DNFSPeer peer;

    /**
     * 
     * @param config
     */
    public DNFSPathResolver(DNFSConfigurator config) {
        this.config = config;
        Main.LOGGER.setLevel(Level.INFO);
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

    /**
     * 
     */
    public void bootStrap(){

    }

    /**
     * 
     * @param path
     * @return
     */
    public DNFSFolder getFolder(String path){
        DNFSiNode iNode = new DNFSiNode();
        iNode.setDir(true);
        return new DNFSFolder(iNode, this);
    }

    /**
     * 
     * @param path
     * @return
     */
    public DNFSFile getFile(String path){
        DNFSiNode iNode = new DNFSiNode();
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
    public DNFSiNode getINode(String path){
        DNFSiNode iNode = new DNFSiNode();

        if(path.endsWith("/")){
            iNode.setDir(true);
        }
        return iNode;
    }

    /**
     * 
     * @param path
     * @return
     * @throws DNFSException
     */
    public DNFSiNode resolve(String path) throws DNFSException {
        String[] parts = path.split(File.separator);
        DNFSFolder currentFolder = this.getRootFolder();

        for(int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            currentFolder = currentFolder.getChildFolder(part);
        }

        return currentFolder.getChildINode(parts[parts.length - 1]);
    }

    /**
     * 
     * @param path
     * @param file
     * @throws IOException
     */
    private DNFSFolder getRootFolder() throws DNFSException{
        return new DNFSFolder(peer.getRootINode(), this);
    }
    
}

