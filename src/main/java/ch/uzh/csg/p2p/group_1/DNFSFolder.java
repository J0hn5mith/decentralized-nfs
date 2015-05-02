package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by janmeier on 06.04.15.
 */
public class DNFSFolder extends DNFSAbstractFile {
    final private static Logger LOGGER = Logger.getLogger(DNFSFolder.class.getName());

    private ArrayList<DNFSFolderEntry> entries;

    /**
     * @param iNode
     */
    private DNFSFolder(DNFSiNode iNode, DNFSIPeer peer) {
        super(iNode, peer);
        this.getINode().setDir(true);

        this.updateFolderEntries();
    }

    /**
     * Factory method for creating new folders.
     */
    public static DNFSFolder createNew(DNFSIPeer peer){
        DNFSFolder folder = new DNFSFolder(peer.createINode(), peer);

        DNFSBlock block = folder.getPeer().createBlock();
        folder.getINode().addBlock(block);
        block.append(folder.getINode().getId() + " " + "./");

        return folder;
    }

    public static DNFSFolder getExisting(DNFSiNode iNode, DNFSIPeer peer){
        DNFSFolder folder = new DNFSFolder(iNode, peer);
        return folder;
    }

    public ArrayList<DNFSFolderEntry> getEntries() {
        return this.entries;
    }


    public void addChild(DNFSFileSystemEntry entry, String name){
        DNFSBlock block = this.getPeer().getBlock(this.getINode().getBlockIDs().get(0));
        block.append("\n" + entry.getINode().getId() + " " + name);
        this.updateFolderEntries();
    }

    public DNFSiNode getChildINode(String name) throws DNFSException {
        return this.getPeer().getINode(this.getIDOfChild(name));
    }

    public DNFSFolder getChildFolder(String name) throws DNFSException {
        return DNFSFolder.getExisting(this.getChildINode(name), this.getPeer());
    }

    public DNFSFile getChildFile(String name) throws DNFSException {
        return DNFSFile.createNew(this.getPeer());
    }


    private Number160 getIDOfChild(String name) throws DNFSException {
        LOGGER.info("get child with name: " + name);

        if (name.equals(".") || name.equals("object")) {
            return this.getINode().getId();
        }

        for (DNFSFolderEntry dnfsFolderEntry : this.entries) {
            if (dnfsFolderEntry.getName().equals(name)) {
                return dnfsFolderEntry.getKey();
            }
        }
        throw new DNFSException();
    }


    /**
     *
     */
    static public class DNFSFolderEntry {

        private Number160 key;
        private String name;

        public DNFSFolderEntry(String line) {
        }

        public DNFSFolderEntry(Number160 key, String name) {
            this.key = key;
            this.name = name;
        }

        public Number160 getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

    }

    private void updateFolderEntries(){
        this.entries = this.extractFolderEntries();
    }
    /**
     * @return
     */
    private ArrayList<DNFSFolderEntry> extractFolderEntries() {
        ArrayList<DNFSFolderEntry> list = new ArrayList<DNFSFolderEntry>();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getFolderFileData()));

        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineComponents = line.split(" ");
                if (lineComponents.length == 2){
                    list.add(new DNFSFolderEntry(new Number160(lineComponents[0]), lineComponents[1]));
                }
                else {
                    LOGGER.warn("Format failure in folder data.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return list;
    }

    private InputStream getFolderFileData() {
        if (this.getINode().getNumBlocks() < 1){
            return new ByteArrayInputStream("".getBytes());
        }

        DNFSBlock block = this.getPeer().getBlock(this.getINode().getBlockIDs().get(0));
        return block.getInputStream();
    }

}
