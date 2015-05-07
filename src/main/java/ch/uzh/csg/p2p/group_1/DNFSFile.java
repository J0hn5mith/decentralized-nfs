package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by janmeier on 02.04.15.
 */


public class DNFSFile extends DNFSAbstractFile {

    /**
     * 
     * @param iNode
     */
    DNFSFile(DNFSiNode iNode, DNFSIPeer peer){
        super(iNode, peer);
        this.getINode().addBlock(this.getPeer());
    }

    public static DNFSFile createNew(DNFSIPeer peer){
        return new DNFSFile(peer.createINode(), peer);
    }
    public static DNFSFile getExisting(DNFSiNode iNode, DNFSIPeer peer){
        DNFSFile file = new DNFSFile(iNode, peer);
        return file;
    }

    /**
     * 
     * @return
     */
    public InputStream getInputStream(){
        return this.getFirstBlock().getInputStream();
    }

    public int write(final ByteBuffer buffer, final long bufSize, final long writeOffset)
    {
        int bytesWritten =  this.getFirstBlock().write(buffer, bufSize, writeOffset);
        this.getINode().setSize((int) this.getFirstBlock().getSize());
        return bytesWritten;
    }

    public int read(final ByteBuffer buffer, final long bytesToRead, final long offset){
        return this.getFirstBlock().read(buffer, bytesToRead, offset);

    }

    @Override
    public int delete() {
        return 0;
    }

    public int truncate(long offset){
        return this.getFirstBlock().truncate(offset);
    }

    private DNFSBlock getFirstBlock(){
        return this.getPeer().getBlock(this.getINode().getBlockIDs().get(0));
    }
}
