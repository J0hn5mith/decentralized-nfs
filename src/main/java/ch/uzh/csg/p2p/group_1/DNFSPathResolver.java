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
    private final Logger LOGGER = Logger.getLogger(this.getClass());

    private DNFSConfigurator config;
    private DNFSPeer peer;

    public DNFSPathResolver(DNFSConfigurator config) {
        this.config = config;
        LOGGER.setLevel(Level.INFO);
    }

    public void setUp(){
        try {
            this.peer = new DNFSPeer();
            this.peer.setUp();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Failed to set up peer");
        }
        LOGGER.info("Successfully set up connection");
    }

    public DNFSFolder getFolder(String path){
        DNFSiNode iNode = new DNFSiNode();
        iNode.setDir(true);
        return new DNFSFolder(iNode, this);
    }

    public DNFSFile getFile(String path){
        DNFSiNode iNode = new DNFSiNode();
        return new DNFSFile(iNode, this);
    }

    public DNFSiNode getINodeByID(Number160 nodeID) throws DNFSException{
        return peer.getINode(nodeID);
    }

    public DNFSiNode getINode(String path){
        DNFSiNode iNode = new DNFSiNode();

        if(path.endsWith("/")){
            iNode.setDir(true);
        }
        return iNode;
    }

    public DNFSiNode resolve(String path) throws DNFSException {
        String[] parts = path.split(File.separator);
        DNFSFolder currentFolder = this.getRootFolder();

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            currentFolder = currentFolder.getChildFolder(part);
        }

        return currentFolder.getChildINode(parts[parts.length-1]);

    }

    private DNFSFolder getRootFolder() throws DNFSException{

        return new DNFSFolder(peer.getRootINode(), this);

    }
}

