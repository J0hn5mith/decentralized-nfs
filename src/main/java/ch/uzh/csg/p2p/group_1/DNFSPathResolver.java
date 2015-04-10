/**
 * Created by janmeier on 29.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import net.tomp2p.storage.Data;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

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
        return new DNFSFolder(iNode);
    }

    /**
     * 
     * @param path
     * @return
     */
    public DNFSFile getFile(String path){
        DNFSiNode iNode = new DNFSiNode();
        return new DNFSFile(iNode);
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
     * @param targetPath
     * @param dirName
     */
    public void mkdir(String targetPath, String dirName){
//        File file = peer.getFile(targetPath).awaitUninterruptibly();
    }

    /**
     * 
     * @param path
     * @param file
     * @throws IOException
     */
    public void write_file(String path, String file) throws IOException {
        peer.putFile(path, file).awaitUninterruptibly();

    }

    /**
     * 
     * @param path
     * @throws IOException
     */
    public void read_file(String path) throws IOException {
        this.peer.getFile(path).awaitUninterruptibly();
    }
}

