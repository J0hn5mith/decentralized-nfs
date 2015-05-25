/**
 * Created by janmeier on 29.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import java.io.IOException;

import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class DNFSPathResolver implements IPathResolver {
    final private static Logger LOGGER = Logger.getLogger(DNFSFolder.class.getName());
    private IStorage peer;

    /**
     */
    public DNFSPathResolver(IStorage peer) {
        LOGGER.setLevel(Level.WARN);
        this.setPeer(peer);
    }

    /**
     *
     */
    public void setUp() {

        LOGGER.info("Successfully set up connection");
    }

    public IStorage getPeer() {
        return peer;
    }

    public void setPeer(IStorage peer) {
        this.peer = peer;
    }

    @Override
    /**
     * @param path
     * @return
     */
    public DNFSFolder getFolder(DNFSPath path) throws DNFSException.DNFSPathNotFound, DNFSException.DNFSNotFolderException, DNFSException.DNFSNetworkNotInit {
        DNFSIiNode iNode = this.resolve(path);
        if (!iNode.isDir()) {
            throw new DNFSException.DNFSNotFolderException();
        }
        return DNFSFolder.getExisting(iNode, this.getPeer());
    }

    @Override
    /**
     * @param path
     * @return
     */
    public DNFSFile getFile(DNFSPath path) throws DNFSException.DNFSNotFileException, DNFSException.DNFSPathNotFound {
        DNFSIiNode iNode = this.resolve(path);
        if (iNode.isDir()) {
            throw new DNFSException.DNFSNotFileException();
        }
        return new DNFSFile(iNode, this.getPeer());
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
        DNFSFolder currentFolder = null;
        try {
            currentFolder = this.getRootFolder();
            if (path.length() == 0) {
                return currentFolder.getINode();
            }

            for (String pathComponent : path.getComponents(0, -1)) {
                currentFolder = currentFolder.getChildFolder(pathComponent);
            }

            return currentFolder.getChildINode(path.getComponent(-1));

        } catch (DNFSException e) {
            LOGGER.debug("Reason for path resolver failing: ", e);
            throw new DNFSException.DNFSPathNotFound();
        }
    }


    /**
     * @throws IOException
     */
    private DNFSFolder getRootFolder() throws DNFSException {
        return DNFSFolder.getExisting(peer.getRootINode(), this.getPeer());
    }

}

