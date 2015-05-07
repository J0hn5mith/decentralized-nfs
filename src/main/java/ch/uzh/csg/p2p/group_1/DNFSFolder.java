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

    private HashMap<String, DNFSFolderEntry> entries;

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
        try {
            DNFSFolder folder = new DNFSFolder(peer.createINode(), peer);
    
            DNFSBlock block = folder.getPeer().createBlock();
            folder.getINode().addBlock(block);
            block.append(folder.getINode().getId() + SEPARATOR + "./");
    
            return folder;
        } catch(DNFSException e) {
            // TODO: DEAL WITH THIS
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static DNFSFolder getExisting(DNFSiNode iNode, DNFSIPeer peer) {
        DNFSFolder folder = new DNFSFolder(iNode, peer);
        return folder;
    }

    public List<DNFSFolderEntry> getEntries() {
        return new ArrayList<DNFSFolderEntry>(this.entries.values());
    }


    public void addChild(DNFSFileSystemEntry entry, String name) {
        this.addChild(entry.getINode(), name);
    }
    
    
    public void addChild(DNFSiNode iNode, String name) {
        try {
            DNFSBlock block = this.getPeer().getBlock(this.getINode().getBlockIDs().get(0));
            block.append("\n" + iNode.getId() + SEPARATOR + name);
            this.updateFolderEntries();
        } catch(DNFSException e) {
            // TODO: DEAL WITH THIS
            System.out.println(e.getMessage());
        }
    }

    public void removeChild(String name){
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getFolderFileData()));
        String newContent = "";
        try {
            String line;
            DNFSFolderEntry entry;
            while ((line = br.readLine()) != null) {
                String[] lineComponents = line.split(" ");

                if (!lineComponents[1].equals(name)) {
                    entry = new DNFSFolderEntry(new Number160(lineComponents[0]), name);
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
        return this.entries.containsKey(name);
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

        if (this.hasChild(name)) {
            return this.entries.get(name).getKey();
        }

        throw new DNFSException();
    }

    private DNFSBlock getBlock() {
        try {
            return this.getPeer().getBlock(this.getINode().getBlockIDs().get(0));
        } catch(DNFSException e) {
            // TODO: DEAL WITH THIS
            System.out.println(e.getMessage());
        }
        return null;
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
        this.entries = this.extractFolderEntries();
    }

    /**
     * @return
     */
    private HashMap<String, DNFSFolderEntry> extractFolderEntries() {
        HashMap<String, DNFSFolderEntry> list = new HashMap<String, DNFSFolderEntry>();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getFolderFileData()));

        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineComponents = line.split(SEPARATOR);
                if (lineComponents.length == 2) {
                    list.put(lineComponents[1], new DNFSFolderEntry(new Number160(lineComponents[0]), lineComponents[1]));
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

}
