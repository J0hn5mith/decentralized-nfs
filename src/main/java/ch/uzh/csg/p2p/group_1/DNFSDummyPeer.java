package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by janmeier on 16.04.15.
 */
public class DNFSDummyPeer implements DNFSIPeer{

    private Map<Number160, DNFSBlock> blocks;
    private Map<Number160, DNFSiNode> iNodes;
    private DNFSiNode rootINode;
    private Random random;

    public DNFSDummyPeer() {
        DNFSBlock fileBlock =  new DNFSBlock(
                Number160.createHash(1000), "Hello world, again"
        );
        DNFSBlock folderBlock = new DNFSBlock(
                Number160.createHash(1)
        );
        this.blocks = new HashMap<Number160, DNFSBlock>();
        this.iNodes = new HashMap<Number160, DNFSiNode>();

        this.rootINode = DNFSFolder.createNewFolder(this).getINode();


        this.blocks.put(fileBlock.getId(), fileBlock);
        this.blocks.put(folderBlock.getId(), folderBlock);





        this.random = new Random();
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
        this.iNodes.put(iNode.getId(), iNode);
        return iNode;
    }

    @Override
    public DNFSiNode deleteINode(Number160 iNodeID) {
        return null;
    }

    /**
     *
     * @return
     * @throws DNFSException
     */
    public DNFSiNode getRootINode() throws DNFSException {
        return this.rootINode;
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

    @Override
    public DNFSBlock deleteBlock(Number160 id) {
        return null;
    }

    @Override
    public void setUp() throws IOException {

    }
}
