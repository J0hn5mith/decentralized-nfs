package ch.uzh.csg.p2p.group_1.file_system.interfaces;
/**
 * Created by janmeier on 02.05.15.
 */
import ch.uzh.csg.p2p.group_1.file_system.DNFSPath;
import ch.uzh.csg.p2p.group_1.file_system.File;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.file_system.Directory;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;

public interface IPathResolver {

    public Directory getDirectory(DNFSPath path) throws DNFSException;
    public File getFile(DNFSPath path) throws DNFSException;
    public DNFSIiNode getINode(DNFSPath path) throws DNFSException;

}

