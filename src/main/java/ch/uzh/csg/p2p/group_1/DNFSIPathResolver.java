package ch.uzh.csg.p2p.group_1;

/**
 * Created by janmeier on 02.05.15.
 */
public interface DNFSIPathResolver {

    public DNFSFolder getFolder(DNFSPath path) throws DNFSException;
    public DNFSFile getFile(DNFSPath path) throws DNFSException;
    public DNFSiNode getINode(DNFSPath path) throws DNFSException;

}

