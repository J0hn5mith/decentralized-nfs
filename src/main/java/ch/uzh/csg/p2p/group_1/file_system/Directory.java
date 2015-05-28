package ch.uzh.csg.p2p.group_1.file_system;

import ch.uzh.csg.p2p.group_1.Main;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.storage.interfaces.IStorage;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import net.tomp2p.peers.Number160;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by janmeier on 06.04.15.
 */
public class Directory extends FileSystemEntry {


    final private static Logger LOGGER = Logger.getLogger(Directory.class);
    final public static String SEPARATOR = "\t";
    final public static String LINE_SEPARATOR = "\r";

    private HashMap<String, DirectoryINodeMapEntry> childEntries;
    private DirectoryINodeMapEntry parentEntry;
    private DirectoryINodeMapEntry selfEntry;


    /**
     * @param iNode
     */
    private Directory(DNFSIiNode iNode, IStorage storage) throws DNFSException.DNFSBlockStorageException {
        super(iNode, storage);
        this.updateINodeMap();
        LOGGER.setLevel(Main.LOGGER_LEVEL);
    }


    /**
     * Factory method for creating the root directory
     */
    public static Directory createRoot(IStorage storage) throws DNFSException.DNFSNetworkNotInit, DNFSException.DNFSBlockStorageException, DNFSException {
        DNFSIiNode iNode = storage.createRootINode();
        iNode.setDir(true);
        return new Directory(iNode, storage);
    }


    /**
     * Factory method for creating new directories.
     */
    public static Directory createNew(IStorage storage) throws DNFSException.DNFSNetworkNotInit, DNFSException.DNFSBlockStorageException, DNFSException {
        DNFSIiNode iNode = storage.createINode();
        iNode.setDir(true);
        return new Directory(iNode, storage);
    }

    /**
     * Factory method for creating objects for existing directories.
     */
    public static Directory getExisting(DNFSIiNode iNode, IStorage storage) throws DNFSException.DNFSBlockStorageException {
        return new Directory(iNode, storage);
    }


    public DNFSIiNode getChildINode(String name) throws DNFSException.NoSuchFileOrDirectory {
        try {
            Number160 iNodeId = this.getIdOfChild(name);
            return this.getStorage().getINode(iNodeId);
        } catch (DNFSException e) {
            throw new DNFSException.NoSuchFileOrDirectory();
        }
    }


    public FileSystemEntry getChild(String name) throws DNFSException.NoSuchFileOrDirectory, DNFSException.DNFSBlockStorageException {
        DNFSIiNode iNode = this.getChildINode(name);
        if (iNode.isDir()) {
            return Directory.getExisting(iNode, this.getStorage());

        } else {
            return File.getExisting(iNode, this.getStorage());
        }
    }


    public Directory getChildDirectory(String name) throws DNFSException.NoSuchFileOrDirectory, DNFSException.DNFSNetworkNotInit, DNFSException.DNFSBlockStorageException {
        DNFSIiNode iNode = this.getChildINode(name);
        return Directory.getExisting(iNode, this.getStorage());
    }


    public File getChildFile(String name) throws DNFSException.NoSuchFileOrDirectory, DNFSException.DNFSNetworkNotInit, DNFSException {
        this.getChildINode(name);
        return File.createNew(this.getStorage());
    }


    public boolean hasChild(String name) {
        return this.childEntries.containsKey(name);
    }


    public void addChild(DNFSIiNode iNode, String name) throws DNFSException.DNFSBlockStorageException {
        this.addINodeMapEntry(iNode, name);
        this.updateINodeMap();
    }


    public void removeChild(String name) throws DNFSException.NoSuchFileOrDirectory, DNFSException.DNFSNetworkNotInit, DNFSException {

        try {

            FileSystemEntry child = this.getChild(name);
            child.delete();

            BufferedReader br = new BufferedReader(new InputStreamReader(this.getINodeMapDataStream()));
            String newContent = "";

            String line;
            DirectoryINodeMapEntry entry;
            boolean firstLine = true;
            while((line = br.readLine()) != null) {
                
                String[] lineComponents = line.split(SEPARATOR);
                if(lineComponents.length == 2 && !lineComponents[1].equals(name)) {
                    entry = new DirectoryINodeMapEntry(new Number160(lineComponents[0]), lineComponents[1]);
                    if(firstLine) {
                        firstLine = false;
                    } else {
                        newContent += "\n";
                    }
                    newContent = newContent + entry.toString();
                }
            }
            
            byte[] newContentByteArray = newContent.getBytes();
            int newContentLength = newContentByteArray.length;
            ByteBuffer newContentBuffer = ByteBuffer.wrap(newContentByteArray);

            this.getBlockComposition().truncate(0);
            this.getBlockComposition().write(newContentBuffer, newContentLength, 0);
            
            br = new BufferedReader(new InputStreamReader(this.getINodeMapDataStream())); // TODO
            LOGGER.debug("---------WE JUST DELETED: " + name);
            try {
                while ((line = br.readLine()) != null) { // TODO
                    LOGGER.debug("--> LINE: " + line); // TODO
                }
            } catch (IOException e) { // TODO
                e.printStackTrace(); // TODO
            } // TODO
            
            this.updateINodeMap();

        } catch (DNFSException.DNFSBlockStorageException e) {
            throw new DNFSException.DNFSNetworkNotInit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void renameChild(String oldName, String newName) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getINodeMapDataStream()));
        String newContent = "";
        try {
            String line;
            DirectoryINodeMapEntry entry;
            while ((line = br.readLine()) != null) {
                String[] lineComponents = line.split(" ");

                String name = lineComponents[1];
                if (name.equals(oldName)) {
                    name = newName;
                }
                entry = new DirectoryINodeMapEntry(new Number160(lineComponents[0]), name);
                newContent = newContent + entry.toString() + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.getBlockComposition().truncate(0);
        this.getBlockComposition().write(ByteBuffer.wrap(newContent.getBytes()), newContent.getBytes().length, 0);
        this.updateINodeMap();
    }


    /**
     * @param name
     * @return
     * @throws DNFSException
     */
    private Number160 getIdOfChild(String name) throws DNFSException {

        if (name.equals(".") || name.equals("object")) {
            return this.getINode().getId();
        }

        if (this.hasChild(name)) {
            return this.childEntries.get(name).getINode();
        }

        throw new DNFSException();
    }


    /**
     *
     */
    private void updateINodeMap() throws DNFSException.DNFSBlockStorageException {

        HashMap<String, DirectoryINodeMapEntry> list = new HashMap<String, DirectoryINodeMapEntry>();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getINodeMapDataStream()));

        try {
            String line;
            String name;
            Number160 inodeId;
            while ((line = br.readLine()) != null) {
                LOGGER.debug(String.format("Pars line from directory data: \"%s'\"", line));
                String[] lineComponents = line.split(SEPARATOR);
                if (lineComponents.length == 2) {
                    name = lineComponents[1];
                    try {
                        inodeId = new Number160(lineComponents[0]);
                        if (name.equals(".")) {
                            this.selfEntry = new DirectoryINodeMapEntry(inodeId, name);
                        } else if (name.equals("..")) {
                            this.parentEntry = new DirectoryINodeMapEntry(inodeId, name);
                        } else {
                            list.put(name, new DirectoryINodeMapEntry(inodeId, name));
                        }

                    } catch (IllegalArgumentException e) {
                        LOGGER.warn(String.format("Faild to parse block line \"%s'\" because of components 1: \"%s\" 2: \"%s\"\n Line is ignored.", line, lineComponents[0], lineComponents[1]));
                    }
                } else {
                    LOGGER.warn("Format failure in directory data. Line is to sposed to have two values separated by an tab.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.childEntries = list;
    }


    public List<FileSystemEntry> getChildren() {
        List<FileSystemEntry> entries = new ArrayList<FileSystemEntry>();
        DNFSIiNode iNode;
        for (DirectoryINodeMapEntry entry : this.childEntries.values()) {
            try {
                iNode = this.getStorage().getINode(entry.getINode());
                if (iNode.isDir()) {
                    entries.add(Directory.getExisting(iNode, this.getStorage()));

                } else {
                    entries.add(File.getExisting(iNode, this.getStorage()));
                }
            } catch (DNFSException e) {

            }

        }
        return entries;
    }


    public List<DirectoryINodeMapEntry> getINodeMap() {

        List<DirectoryINodeMapEntry> result = new ArrayList<DirectoryINodeMapEntry>(this.childEntries.values());

        if (this.selfEntry != null) {
            result.add(0, this.selfEntry);
        }
        if (this.parentEntry != null) {
            result.add(1, this.parentEntry);
        }
        return result;
    }


    private InputStream getINodeMapDataStream() throws DNFSException.DNFSBlockStorageException {


        if (this.getINode().getNumBlocks() == 0) {
            return new ByteArrayInputStream("".getBytes());
        }

        long size = this.getBlockComposition().getSize();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[(int) size]);
        this.getBlockComposition().read(buffer, size, 0);

        return new ByteArrayInputStream(buffer.array());

    }


    @Override
    public void delete() throws DNFSException {
        for (FileSystemEntry entry : this.getChildren()) {
            entry.delete();
        }
        this.getStorage().deleteINode(this.getINode().getId());
    }


    private void addINodeMapEntry(DNFSIiNode iNode, String name){

        try {

            String entryAsString = iNode.getId() + SEPARATOR + name;
            if (this.childEntries.size() > 0) {
                entryAsString = LINE_SEPARATOR + entryAsString;
            }

            LOGGER.debug("+++++++++++++ADDED ENTRY: " + entryAsString); // TODO

            byte[] entryAsByteArray = entryAsString.getBytes();
            int entryLength = entryAsByteArray.length;
            ByteBuffer entry = ByteBuffer.wrap(entryAsByteArray);

            this.getBlockComposition().append(entry, entryLength);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(this.getINodeMapDataStream())); // TODO
            String line; // TODO
            try {
                while ((line = br.readLine()) != null) { // TODO
                    LOGGER.debug("--> LINE: " + line); // TODO
                }
            } catch (IOException e) { // TODO
                e.printStackTrace(); // TODO
            } // TODO
            
        } catch (DNFSException.DNFSBlockStorageException e) {
            e.printStackTrace();
        }
    }

}
