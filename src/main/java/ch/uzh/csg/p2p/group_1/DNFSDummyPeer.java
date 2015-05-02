package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by janmeier on 16.04.15.
 */
public class DNFSDummyPeer implements DNFSIPeer {

    private Map<Number160, DNFSBlock> blocks;
    private Map<Number160, DNFSiNode> iNodes;
    private DNFSiNode rootINode;
    private Random random;

    public DNFSDummyPeer() {
        DNFSBlock fileBlock = new DNFSBlock(
                Number160.createHash(1000), "Hello world, again"
        );
        DNFSBlock folderBlock = new DNFSBlock(
                Number160.createHash(1)
        );
        this.blocks = new HashMap<Number160, DNFSBlock>();
        this.iNodes = new HashMap<Number160, DNFSiNode>();


        this.blocks.put(fileBlock.getId(), fileBlock);
        this.blocks.put(folderBlock.getId(), folderBlock);


        this.random = new Random();
    }

    @Override
    public DNFSiNode createINode() {
        DNFSiNode iNode = new DNFSiNode(this.getNewINodeID());
        this.iNodes.put(iNode.getId(), iNode);
        return iNode;
    }

    /**
     * @param iNodeID
     * @return
     * @throws DNFSException
     */
    public DNFSiNode getINode(Number160 iNodeID) throws DNFSException {
        DNFSiNode iNode = new DNFSiNode(Number160.createHash(1000));
        iNode.setDir(false);
        return iNode;
    }

    @Override
    public void deleteINode(Number160 iNodeID) {

    }


    @Override
    public void updateINode(DNFSiNode iNode) {

    }

    /**
     * @return
     * @throws DNFSException
     */
    public DNFSiNode getRootINode() throws DNFSException {
        return this.rootINode;
    }

    @Override
    public DNFSiNode createRootINode() throws DNFSException {
        return null;
    }

    public Number160 getNewINodeID() {
        int randomInt = this.random.nextInt();
        Number160 id = Number160.createHash(randomInt);
        return id;
    }

    public Number160 getNewBlockID() {
        return Number160.createHash(this.random.nextInt());
    }

    @Override
    public DNFSBlock createBlock() {
        DNFSBlock block = new DNFSBlock(this.getNewBlockID());
        this.blocks.put(block.getId(), block);
        return block;
    }

    public DNFSBlock getBlock(Number160 id) {
        return this.blocks.get(id);
    }

    @Override
    public void updateBlock(DNFSBlock block) {

    }

    @Override
    public void deleteBlock(Number160 id) {

    }


    @Override
    public void setUp() throws IOException {
        DNFSFolder rootFolder = DNFSFolder.createNewFolder(this);
        this.rootINode = rootFolder.getINode();
        DNFSFile testFile = DNFSFile.createNewFile(this);
        rootFolder.addChild(testFile, "Test_File_1.txt");

        testFile = DNFSFile.createNewFile(this);
        rootFolder.addChild(testFile, "Test_File_2.txt");
    }
}
