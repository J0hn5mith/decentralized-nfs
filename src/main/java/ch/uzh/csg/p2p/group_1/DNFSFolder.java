package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.util.ArrayList;

/**
 * Created by janmeier on 06.04.15.
 */
public class DNFSFolder extends DNFSAbstractFile{


    public DNFSFolder(DNFSiNode iNode) {
        this.setiNode(iNode);
    }

    public ArrayList<DNFSFolderEntry> getEntries(){
        ArrayList<DNFSFolderEntry> list = new ArrayList<DNFSFolderEntry>();
        list.add(new DNFSFolderEntry(Number160.createHash(123), "test.txt"));
        list.add(new DNFSFolderEntry(Number160.createHash(2), "heey.txt"));
        return list;
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
