package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
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
public class DNFSFolder extends DNFSFileSystemEntry {


    final private static Logger LOGGER = Logger.getLogger(DNFSFolder.class.getName());
    final public static String SEPARATOR = "\t";
    final public static String LINE_SEPARATOR = "\r";

    private HashMap<String, DNFSFolderEntry> childEntries;
    private DNFSFolderEntry parentEntry;
    private DNFSFolderEntry selfEntry;


    /**
     * @param iNode
     */
    private DNFSFolder(DNFSIiNode iNode, DNFSIPeer peer) throws DNFSException.DNFSNetworkNotInit {
        super(iNode, peer);
        this.updateFolderEntries();
    }
    
    
    /**
     * Factory method for creating the folder of the root directory
     */
    public static DNFSFolder createRoot(DNFSIPeer peer) throws DNFSException.DNFSNetworkNotInit, DNFSException.DNFSBlockStorageException, DNFSException {
        DNFSIiNode iNode = peer.createRootINode();
        iNode.setDir(true);
        return new DNFSFolder(iNode, peer);
    }


    /**
     * Factory method for creating new folders.
     */
    public static DNFSFolder createNew(DNFSIPeer peer) throws DNFSException.DNFSNetworkNotInit, DNFSException.DNFSBlockStorageException, DNFSException {
        DNFSIiNode iNode = peer.createINode();
        iNode.setDir(true);
        return new DNFSFolder(iNode, peer);
    }


    public static DNFSFolder getExisting(DNFSIiNode iNode, DNFSIPeer peer) throws DNFSException.DNFSNetworkNotInit {
        return new DNFSFolder(iNode, peer);
    }


    public List<DNFSFolderEntry> getChildEntries() {
        List<DNFSFolderEntry> result = new ArrayList<DNFSFolderEntry>(this.childEntries.values());

        if(this.selfEntry != null) {
            result.add(0, this.selfEntry);
        }
        if(this.parentEntry != null) {
            result.add(1, this.parentEntry);
        }

        return result;
    }


    /**
     * @return
     */
    public List<DNFSFileSystemEntry> getChildren() {
        List<DNFSFileSystemEntry> entries = new ArrayList<DNFSFileSystemEntry>();
        DNFSIiNode iNode;
        for (DNFSFolderEntry entry : this.childEntries.values()) {
            try {
                iNode = this.getPeer().getINode(entry.getINodeKey());
                if (iNode.isDir()) {
                    entries.add(DNFSFolder.getExisting(iNode, this.getPeer()));

                } else {
                    entries.add(DNFSFile.getExisting(iNode, this.getPeer()));
                }
            } catch (DNFSException e) {

            }


        }
        return entries;
    }


    /**
     * @param entry
     * @param name
     */
    public void addChild(DNFSFileSystemEntry entry, String name) throws DNFSException.DNFSNetworkNotInit {
        this.addChild(entry.getINode(), name);
    }


    // TODO: There are different method doing the same thing => find way to unify it
    // Basic problem is, that i cant decide weather getting children by name or id
    public DNFSFileSystemEntry getChild(String name) throws DNFSException.NoSuchFileOrFolder, DNFSException.DNFSNetworkNotInit {
        DNFSIiNode iNode = this.getChildINode(name);
        if (iNode.isDir()) {
            return DNFSFolder.getExisting(iNode, this.getPeer());

        } else {
            return DNFSFile.getExisting(iNode, this.getPeer());
        }
    }


    /**
     * @param iNode
     * @param name
     */
    public void addChild(DNFSIiNode iNode, String name) throws DNFSException.DNFSNetworkNotInit {
        this.addNewFolderEntry(iNode, name);
        this.updateFolderEntries();
    }


    /**
     * @param name
     * @throws DNFSException.NoSuchFileOrFolder
     */
    public void removeChild(String name) throws DNFSException.NoSuchFileOrFolder, DNFSException.DNFSNetworkNotInit {
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getFolderFileData()));
        String newContent = "";
        DNFSFileSystemEntry child = this.getChild(name);
        child.delete();
        try {
            String line;
            DNFSFolderEntry entry;
            while ((line = br.readLine()) != null) {
                String[] lineComponents = line.split(SEPARATOR);

                if (!lineComponents[1].equals(name)) {
                    entry = new DNFSFolderEntry(new Number160(lineComponents[0]), lineComponents[1]);
                    newContent = newContent + entry.toString() + "\n";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            int bytesWritten = (int) this.getBlockComposition().write(ByteBuffer.wrap(newContent.getBytes()), newContent.getBytes().length, 0);
            this.getBlockComposition().truncate(bytesWritten);
        } catch (DNFSException.DNFSBlockStorageException e) {
            throw new DNFSException.DNFSNetworkNotInit();
        }
        this.updateFolderEntries();
    }


    /**
     * @param name
     * @return
     */
    public boolean hasChild(String name) {
        return this.childEntries.containsKey(name);
    }


    /**
     * @param oldName
     * @param newName
     */
    public void renameChild(String oldName, String newName) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {
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

        this.getBlockComposition().truncate(0);
        this.getBlockComposition().write(ByteBuffer.wrap(newContent.getBytes()), newContent.getBytes().length, 0);
        this.updateFolderEntries();
    }


    /**
     * Utilities
     */


    /**
     * @param name
     * @return
     * @throws DNFSException.NoSuchFileOrFolder
     */
    public DNFSIiNode getChildINode(String name) throws DNFSException.NoSuchFileOrFolder {
        try {
            return this.getPeer().getINode(this.getIDOfChild(name));
        } catch (DNFSException e) {
            throw new DNFSException.NoSuchFileOrFolder();
        }
    }


    /**
     * @param name
     * @return
     * @throws DNFSException
     */
    public DNFSFolder getChildFolder(String name) throws DNFSException {
        return DNFSFolder.getExisting(this.getChildINode(name), this.getPeer());
    }


    /**
     * @param name
     * @return
     * @throws DNFSException
     */
    public DNFSFile getChildFile(String name) throws DNFSException {
        return DNFSFile.createNew(this.getPeer());
    }


    /**
     * @param name
     * @return
     * @throws DNFSException
     */
    private Number160 getIDOfChild(String name) throws DNFSException {
        LOGGER.info("get child with name: " + name);

        if (name.equals(".") || name.equals("object")) {
            return this.getINode().getId();
        }

        if (this.hasChild(name)) {
            return this.childEntries.get(name).getINodeKey();
        }

        throw new DNFSException();
    }


    /**
     *
     */
    private void updateFolderEntries() throws DNFSException.DNFSNetworkNotInit {
        this.childEntries = this.extractFolderEntries();
    }


    /**
     * @return
     */
    private HashMap<String, DNFSFolderEntry> extractFolderEntries() throws DNFSException.DNFSNetworkNotInit {
        HashMap<String, DNFSFolderEntry> list = new HashMap<String, DNFSFolderEntry>();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getFolderFileData()));

        try {
            String line;
            String name;
            Number160 inodeId;
            while ((line = br.readLine()) != null) {
                String[] lineComponents = line.split(SEPARATOR);
                if (lineComponents.length == 2) {
                    name = lineComponents[1];
                    try {
                        inodeId = new Number160(lineComponents[0]);
                        if (name.equals(".")) {
                            this.selfEntry = new DNFSFolderEntry(inodeId, name);
                        } else if (name.equals("..")) {
                            this.parentEntry = new DNFSFolderEntry(inodeId, name);
                        } else {
                            list.put(name, new DNFSFolderEntry(inodeId, name));
                        }
                        
                    } catch(IllegalArgumentException e) {
                        System.out.println("ILLEGAL LINE COMPONENTS : \"" + lineComponents[0] + "\", \"" + lineComponents[1] + "\""); // TODO
                        e.printStackTrace(); // TODO
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


    /**
     * @return
     */
    private InputStream getFolderFileData() throws DNFSException.DNFSNetworkNotInit {
        if (this.getINode().getNumBlocks() < 1) {
            return new ByteArrayInputStream("".getBytes());
        }
        try {
            long size = this.getBlockComposition().getSize();
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(int) size]);
            this.getBlockComposition().read(buffer, size, 0);
            return new ByteArrayInputStream(buffer.array());
        } catch (DNFSException.DNFSBlockStorageException e) {
            throw new DNFSException.DNFSNetworkNotInit();
        }
    }


    /**
     *
     */
    @Override
    public int delete() {
        for (DNFSFileSystemEntry entry : this.getChildren()) {
            entry.delete();
        }
        // TODO: How to handle the case where an inode appears at several places
        try {
            this.getPeer().deleteINode(this.getINode().getId());
        } catch (DNFSException e) {
            // TODO: DEAL WITH THIS
            System.out.println(e.getMessage());
        }
        return 0;
    }

    
    private void addNewFolderEntry(DNFSIiNode iNode, String name) throws DNFSException.DNFSNetworkNotInit {
        String entryAsString = LINE_SEPARATOR + iNode.getId() + SEPARATOR + name;
        if(this.childEntries.size() < 1){
             entryAsString = iNode.getId() + SEPARATOR + name;
        }
        ByteBuffer entry = ByteBuffer.wrap(entryAsString.getBytes());
        try {
            this.getBlockComposition().append(entry, entryAsString.getBytes().length);
        } catch (DNFSException.DNFSBlockStorageException e) {
            e.printStackTrace();
        }
    }
}
