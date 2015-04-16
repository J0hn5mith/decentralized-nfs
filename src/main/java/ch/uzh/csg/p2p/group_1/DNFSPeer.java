package ch.uzh.csg.p2p.group_1;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.*;

/**
 * Created by janmeier on 02.04.15.
 */
public class DNFSPeer implements DNFSIPeer{

    private PeerDHT peer;


    /**
     * 
     */

    /**
     * 
     * @throws IOException
     */
    public void setUp() throws IOException {
        final Random RND = new Random(42L);
        this.peer = new PeerBuilderDHT(new PeerBuilder(new Number160(RND)).ports(6111988).start()).start();
    }


    /**
     * 
     * @param path
     * @param file
     * @return
     * @throws IOException
     */
    public FuturePut putFile(String path, String file) throws IOException {
        DNFSData<String> data = new DNFSData<String>(path, file);
        return peer.put(this.createKey(path)).data(new Data(data)).start();
    }

    /**
     * 
     * @param path
     * @return
     */
    public FutureGet getFile(String path) {
        return this.peer.get(this.createKey(path)).start();
    }

    /**
     * 
     * @param key
     * @return
     */
    private Number160 createKey(String key) {
        return Number160.createHash(key);
    }

    @Override
    public DNFSBlock getNewBlock() {
        return null;
    }

    @Override
    public DNFSBlock getBlock(Number160 id) {
        return null;
    }

    @Override
    public DNFSBlock deleteBlock(Number160 id) {
        return null;
    }

    @Override
    public DNFSiNode getINode(Number160 iNodeID) throws DNFSException {
        return null;
    }

    @Override
    public DNFSiNode getNewINode() {
        return null;
    }

    @Override
    public DNFSiNode deleteINode(Number160 iNodeID) {
        return null;
    }

    @Override
    public DNFSiNode getRootINode() throws DNFSException {
        return null;
    }
}
