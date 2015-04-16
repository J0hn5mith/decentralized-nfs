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
public class DNFSPeer {

    private PeerDHT peer;
    private Random random;
    private Map<Number160, DNFSBlock> blocks;
    private Map<Number160, DNFSiNode> iNodes;
    /**
     * TEST
     */
    public DNFSBlock fileBlock;
    public DNFSBlock folderBlock;


    /**
     * 
     */
    public DNFSPeer() {
        DNFSBlock fileBlock =  new DNFSBlock(Number160.createHash(1000), "Hello world, again");
        DNFSBlock folderBlock = new DNFSBlock(
                Number160.createHash(1), "1 ./\n2 hello.txt\n3 heeey.txt\n4 another_file.txt\n5 hi_ben.txt");
        this.blocks = new HashMap<Number160, DNFSBlock>();
        this.iNodes = new HashMap<Number160, DNFSiNode>();
        this.blocks.put(fileBlock.getId(), fileBlock);
        this.blocks.put(folderBlock.getId(), folderBlock);
        this.random = new Random();
    }

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
     * @return
     * @throws DNFSException
     */
    public DNFSiNode getRootINode() throws DNFSException {
        DNFSiNode iNode = new DNFSiNode(Number160.createHash(1));
        iNode.setDir(true);
        return iNode;
    }

    /**
     * 
     * @param iNodeID
     * @return
     * @throws DNFSException
     */
    public DNFSiNode getINode(Number160 iNodeID) throws DNFSException {
        DNFSiNode iNode = new DNFSiNode(Number160.createHash(1000));
        iNode.setDir(false);
        return iNode;
    }

    public DNFSiNode getNewINode(){
        DNFSiNode iNode = new DNFSiNode(this.getNewINodeID());
        return iNode;
    }
    public DNFSBlock getNewBlock(){
        DNFSBlock block = new DNFSBlock(this.getNewBlockID());
        this.blocks.put(block.getId(), block);
        return block;
    }
    public Number160 getNewINodeID(){
        return Number160.createHash(this.random.nextInt());
    }

    public Number160 getNewBlockID(){
        return Number160.createHash(this.random.nextInt());
    }
    public DNFSBlock getBlock(Number160 id){

        if(id.equals(Number160.createHash(1000))){
            return this.blocks.get(Number160.createHash(1));

        }
        else if (id.equals(Number160.createHash(1))){
            return this.blocks.get(Number160.createHash(1));
        }
        return this.blocks.get(Number160.createHash(1));
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

}
