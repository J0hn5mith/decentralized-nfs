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
    public DNFSFolder getFolder(String path){
        DNFSiNode iNode = new DNFSiNode(Number160.createHash(1));
        iNode.setDir(true);
        return new DNFSFolder(iNode, this);
    }


    /**
     * 
     * @param path
     * @return
     */
    public DNFSFile getFile(String path){
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
    public DNFSiNode getINode(String path) throws DNFSException {
        return this.resolve(path);
    }

    public DNFSBlock getBlock(Number160 blockID){
        return this.peer.getBlock(blockID);
    }

    /**
     * 
     * @param path
     * @return
     * @throws DNFSException
     */
    public DNFSiNode resolve(String path) throws DNFSException {
        List<String> parts = this.splitPath(path);
        return this.resolve(parts);

    }

    public DNFSiNode resolve(List<String> pathParts) throws DNFSException {
        DNFSFolder currentFolder = this.getRootFolder();
        if (pathParts.size() == 0){
            return currentFolder.getINode();
        }

        for(int i = 0; i < pathParts.size() - 1; i++) {
            String part = pathParts.get(i);
            currentFolder = currentFolder.getChildFolder(part);
        }

        return currentFolder.getChildINode(pathParts.get(pathParts.size() - 1));
    }

    /**
     * Splits a path and makes sure that there are no empty items in it.
     * @param path
     * @return
     */
    static public List<String> splitPath(String path){
        List<String> parts = new ArrayList<String>(Arrays.asList(path.split(File.separator)));
        for (Iterator<String> iter = parts.listIterator(); iter.hasNext(); ) {
            String a = iter.next();
            if (a.isEmpty()) {
                iter.remove();
            }
        }
        return parts;
    }

    /**
     * 
     * @throws IOException
     */
    private DNFSFolder getRootFolder() throws DNFSException{
        return new DNFSFolder(peer.getRootINode(), this);
    }
    
}

