package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by janmeier on 06.04.15.
 */
public class DNFSFolder extends DNFSAbstractFile {
    final private static Logger LOGGER = Logger.getLogger(DNFSFolder.class.getName());
    final private static String SEPARATOR = " ";

    private HashMap<String, DNFSFolderEntry> childEntries;
    private DNFSFolderEntry parentEntry;
    private DNFSFolderEntry selfEntry;

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
    public static DNFSFolder createNew(DNFSIPeer peer) {
        DNFSFolder folder = new DNFSFolder(peer.createINode(), peer);

        DNFSBlock block = folder.getPeer().createBlock();
        folder.getINode().addBlock(block);
        block.append(folder.getINode().getId() + SEPARATOR + ".");

        return folder;
    }

    public static DNFSFolder getExisting(DNFSiNode iNode, DNFSIPeer peer) {
        DNFSFolder folder = new DNFSFolder(iNode, peer);
        return folder;
    }


    public List<DNFSFolderEntry> getChildEntries() {
        List<DNFSFolderEntry> result = new ArrayList<DNFSFolderEntry>(this.childEntries.values());

        if (this.selfEntry != null){
            result.add(0, this.selfEntry);

        }
        if (this.parentEntry != null) {
            result.add(1, this.parentEntry);
        }

        return result;
    }

    public List<DNFSFileSystemEntry> getChildren() {
        List<DNFSFileSystemEntry> entries = new ArrayList<DNFSFileSystemEntry>();
        DNFSiNode iNode;
        for (DNFSFolderEntry entry : this.childEntries.values()) {
            try {
                iNode = this.getPeer().getINode(entry.getKey());
                if(iNode.isDir()) {
                    entries.add(DNFSFolder.getExisting(iNode, this.getPeer()));

                }
                else {
                    entries.add(DNFSFile.getExisting(iNode, this.getPeer()));
                }
            } catch (DNFSException e) {

            }


        }
        return entries;
    }


    public void addChild(DNFSFileSystemEntry entry, String name) {
        this.addChild(entry.getINode(), name);
    }

    // TODO: There are different method doing the same thing => find way to unify it
    // Basic problem is, that i cant decide weather getting children by name or id
    public DNFSFileSystemEntry getChild(String name) throws DNFSException.NoSuchFileOrFolder {
        DNFSiNode iNode = this.getChildINode(name);
        if(iNode.isDir()) {
            return DNFSFolder.getExisting(iNode, this.getPeer());

        }
        else {
            return DNFSFile.getExisting(iNode, this.getPeer());
        }
    }

    public void addChild(DNFSiNode iNode, String name) {
        DNFSBlock block = this.getPeer().getBlock(this.getINode().getBlockIDs().get(0));
        block.append("\n" + iNode.getId() + SEPARATOR + name);
        this.updateFolderEntries();
    }

    public void removeChild(String name) throws DNFSException.NoSuchFileOrFolder {
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getFolderFileData()));
        String newContent = "";
        DNFSFileSystemEntry child = this.getChild(name);
        child.delete();
        try {
            String line;
            DNFSFolderEntry entry;
            while ((line = br.readLine()) != null) {
                String[] lineComponents = line.split(this.SEPARATOR);

                if (!lineComponents[1].equals(name)) {
                    entry = new DNFSFolderEntry(new Number160(lineComponents[0]), lineComponents[1]);
                    newContent = newContent + entry.toString() + "\n";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        DNFSBlock block = this.getBlock();
        block.truncate(0);
        block.write(ByteBuffer.wrap(newContent.getBytes()), newContent.getBytes().length, 0);
        this.updateFolderEntries();

    }

    public boolean hasChild(String name) {
        return this.childEntries.containsKey(name);
    }

    public void renameChild(String oldName, String newName) {
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getFolderFileData()));
        String newContent = "";
        try {
            String line;
            DNFSFolderEntry entry;
            while ((line = br.readLine()) != null) {
                String[] lineComponents = line.split(" ");

                String name = lineComponents[1];
                if (name.equals(oldName)) {
                    name = newName;
                }
                entry = new DNFSFolderEntry(new Number160(lineComponents[0]), name);
                newContent = newContent + entry.toString() + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        DNFSBlock block = this.getBlock();
        block.truncate(0);
        block.write(ByteBuffer.wrap(newContent.getBytes()), newContent.getBytes().length, 0);
        this.updateFolderEntries();
    }


    /**
     * Utilities
     */


    public DNFSiNode getChildINode(String name) throws DNFSException.NoSuchFileOrFolder {
        try {
            return this.getPeer().getINode(this.getIDOfChild(name));
        } catch (DNFSException e) {
            throw new DNFSException.NoSuchFileOrFolder();
        }
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

        if (this.hasChild(name)) {
            return this.childEntries.get(name).getKey();
        }

        throw new DNFSException();
    }

    private DNFSBlock getBlock() {
        return this.getPeer().getBlock(this.getINode().getBlockIDs().get(0));
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

        @Override
        public String toString() {
            return this.getKey() + SEPARATOR + name;
        }
    }

    private void updateFolderEntries() {
        this.childEntries = this.extractFolderEntries();
    }

    /**
     * @return
     */
    private HashMap<String, DNFSFolderEntry> extractFolderEntries() {
        HashMap<String, DNFSFolderEntry> list = new HashMap<String, DNFSFolderEntry>();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getFolderFileData()));

        try {
            String line;
            String name;
            Number160 inodeId;
            while ((line = br.readLine()) != null) {
                String[] lineComponents = line.split(SEPARATOR);
                if (lineComponents.length == 2) {
                    name= lineComponents[1];
                    inodeId = new Number160(lineComponents[0]);

                    if (name.equals(".")){
                        this.selfEntry = new DNFSFolderEntry(inodeId, name);
                    }
                    else if (name.equals("..")){
                        this.parentEntry = new DNFSFolderEntry(inodeId, name);
                    }
                    else {
                        list.put(name, new DNFSFolderEntry(inodeId, name));
                    }
                } else {
                    LOGGER.warn("Format failure in folder data.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private InputStream getFolderFileData() {
        if (this.getINode().getNumBlocks() < 1) {
            return new ByteArrayInputStream("".getBytes());
        }

        DNFSBlock block = this.getBlock();
        return block.getInputStream();
    }

    @Override
    public int delete() {
        for(DNFSFileSystemEntry entry : this.getChildren()){
            entry.delete();
        }
        // TODO: How to handle the case where an inode appears at several places
        this.getPeer().deleteINode(this.getINode().getId());
        return 0;
    }
}
