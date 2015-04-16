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

    /**
	 * 
	 * @param iNode
	 */
    DNFSFolder(DNFSiNode iNode, DNFSPathResolver pathResolver){
        super(iNode, pathResolver);

        //TODO: Check if iNode is Folder!
        DNFSBlock block = new DNFSBlock(Number160.createHash(1));
        this.getINode().addBlock(block);
    }

    /**
     * 
     * @return
     */
    public ArrayList<DNFSFolderEntry> getEntries(){
        ArrayList<DNFSFolderEntry> list = new ArrayList<DNFSFolderEntry>();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getFolderFileData()));

        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineComponents = line.split(" ");
                list.add(new DNFSFolderEntry(Number160.createHash(lineComponents[0]), lineComponents[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return list;
    }


    public DNFSFolder getChildFolder(String name) throws DNFSException{
        return new DNFSFolder(this.getChildINode(name), this.getPathResolver());
    }

    public void addChildFolder(String name){
        DNFSBlock block = this.getPathResolver().getBlock(this.getINode().getBlockIDs().get(0));
        DNFSiNode newINode = this.getPathResolver().getPeer().getNewINode();
        DNFSFolder folder = new DNFSFolder(newINode, this.getPathResolver());
        block.append("\n " + folder.getINode().getId() + " " + name);
    }

    public DNFSFile getChildFile(String name) throws DNFSException{
        return new DNFSFile(this.getChildINode(name), this.getPathResolver());
    }

    public DNFSiNode getChildINode(String name) throws DNFSException{
        return this.getPathResolver().getINodeByID(this.getIDOfChild(name));
    }

    private Number160 getIDOfChild(String name) throws DNFSException{
        LOGGER.info("get child with name: " + name);

        if (name.equals(".") || name.equals("object")){
            return this.getINode().getId();
        }
        for (DNFSFolderEntry dnfsFolderEntry : this.getEntries()) {
            if (dnfsFolderEntry.getName().equals(name)){
                return dnfsFolderEntry.getKey();
            }
        }
        throw new DNFSException();
    }

    private InputStream getFolderFileData(){
        DNFSBlock block = this.getPathResolver().getBlock(this.getINode().getBlockIDs().get(0));
        return block.getInputStream();
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
    
}
