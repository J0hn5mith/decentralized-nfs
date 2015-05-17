package ch.uzh.csg.p2p.group_1;
/**
 * Created by janmeier on 02.05.15.
 */
import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
public interface DNFSIPathResolver {

    public DNFSFolder getFolder(DNFSPath path) throws DNFSException;
    public DNFSFile getFile(DNFSPath path) throws DNFSException;
    public DNFSIiNode getINode(DNFSPath path) throws DNFSException;

}

