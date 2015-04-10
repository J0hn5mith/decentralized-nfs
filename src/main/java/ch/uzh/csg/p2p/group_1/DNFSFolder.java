package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.util.ArrayList;

/**
 * Created by janmeier on 06.04.15.
 */
public class DNFSFolder extends DNFSAbstractFile{


    DNFSFolder(DNFSiNode iNode, DNFSPathResolver pathResolver){
        super(iNode, pathResolver);
        //TODO: Check if iNode is Folder!
    }

    public ArrayList<DNFSFolderEntry> getEntries(){
        ArrayList<DNFSFolderEntry> list = new ArrayList<DNFSFolderEntry>();
        list.add(new DNFSFolderEntry(Number160.createHash(123), "test.txt"));
        list.add(new DNFSFolderEntry(Number160.createHash(2), "heey.txt"));
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
        return Number160.createHash(10);
    }

    public class DNFSFolderEntry {

        private Number160 key;
        private String name;

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
