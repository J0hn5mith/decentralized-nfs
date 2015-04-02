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

import java.io.IOException;
import java.util.Random;

import net.tomp2p.storage.Data;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DNFSConnection {
    private final Logger LOGGER = Logger.getLogger(this.getClass());

    private DNFSConfigurator config;
    private DNFSPeer peer;

    public DNFSConnection(DNFSConfigurator config) {
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

    public void bootStrap(){
    }

    public void mkdir(String path, String file){

    }

    public void write_file(String path, String file) throws IOException {
        peer.putFile(path, file).awaitUninterruptibly();

    }

    public void read_file(String path) throws IOException {
        this.peer.getFile(path).awaitUninterruptibly();
    }
}

