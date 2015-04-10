package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by janmeier on 06.04.15.
 */
public class DNFSFolder extends DNFSAbstractFile {

	/**
	 * 
	 * @param iNode
	 */
    DNFSFolder(DNFSiNode iNode, DNFSPathResolver pathResolver){
        super(iNode, pathResolver);
        //TODO: Check if iNode is Folder!
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

    public DNFSFile getChildFile(String name) throws DNFSException{
        return new DNFSFile(this.getChildINode(name), this.getPathResolver());
    }

    public DNFSiNode getChildINode(String name) throws DNFSException{
        return this.getPathResolver().getINodeByID(this.getIDOfChild(name));
    }

    private Number160 getIDOfChild(String name) throws DNFSException{
        for (DNFSFolderEntry dnfsFolderEntry : this.getEntries()) {
            if (dnfsFolderEntry.getName().equals(name)){
                return dnfsFolderEntry.getKey();
            }
        }
        return Number160.createHash(10);
    }

    private InputStream getFolderFileData(){
        String string =  "1 ./\n2 hello.txt\n3 heeey.txt";
        InputStream is = new ByteArrayInputStream(string.getBytes());
        return is;
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
