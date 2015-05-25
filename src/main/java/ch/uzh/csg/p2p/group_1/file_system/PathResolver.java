/**
 * Created by janmeier on 29.03.15.
 */
package ch.uzh.csg.p2p.group_1.file_system;

import java.io.IOException;

import ch.uzh.csg.p2p.group_1.file_system.interfaces.IPathResolver;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.storage.interfaces.IStorage;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class PathResolver implements IPathResolver {
    final private static Logger LOGGER = Logger.getLogger(Directory.class.getName());
    private IStorage storage;

    /**
     */
    public PathResolver(IStorage storage) {
        LOGGER.setLevel(Level.WARN);
        this.setStorage(storage);
    }

    /**
     *
     */
    public void setUp() {

        LOGGER.info("Successfully set up connection");
    }

    public IStorage getStorage() {
        return storage;
    }

    public void setStorage(IStorage storage) {
        this.storage = storage;
    }

    @Override
    /**
     * @param path
     * @return
     */
    public Directory getDirectory(DNFSPath path) throws DNFSException.DNFSPathNotFound, DNFSException.DNFSNotDirectoryException, DNFSException.DNFSNetworkNotInit {
        DNFSIiNode iNode = this.resolve(path);
        if (!iNode.isDir()) {
            throw new DNFSException.DNFSNotDirectoryException();
        }
        return Directory.getExisting(iNode, this.getStorage());
    }

    @Override
    /**
     * @param path
     * @return
     */
    public File getFile(DNFSPath path) throws DNFSException.DNFSNotFileException, DNFSException.DNFSPathNotFound {
        DNFSIiNode iNode = this.resolve(path);
        if (iNode.isDir()) {
            throw new DNFSException.DNFSNotFileException();
        }
        return new File(iNode, this.getStorage());
    }

    @Override
    /**
     * @param path
     * @return
     */
    public DNFSIiNode getINode(DNFSPath path) throws DNFSException.DNFSPathNotFound {
        return this.resolve(path);
    }


    private DNFSIiNode resolve(DNFSPath path) throws DNFSException.DNFSPathNotFound {
        Directory currentDirectory = null;
        try {
            currentDirectory = this.getRootDirectory();
            if (path.length() == 0) {
                return currentDirectory.getINode();
            }

            for (String pathComponent : path.getComponents(0, -1)) {
                currentDirectory = currentDirectory.getChildDirectory(pathComponent);
            }

            return currentDirectory.getChildINode(path.getComponent(-1));

        } catch (DNFSException e) {
            LOGGER.debug("Reason for path resolver failing: ", e);
            throw new DNFSException.DNFSPathNotFound();
        }
    }


    /**
     * @throws IOException
     */
    private Directory getRootDirectory() throws DNFSException {
        return Directory.getExisting(storage.getRootINode(), this.getStorage());
    }

}

