/**
 * Created by janmeier on 29.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class DNFSPathResolver implements DNFSIPathResolver {
    final private static Logger LOGGER = Logger.getLogger(DNFSFolder.class.getName());
    private DNFSIPeer peer;

    /**
     */
    public DNFSPathResolver(DNFSIPeer peer) {
        Main.LOGGER.setLevel(Level.WARN);
        this.setPeer(peer);
    }

    /**
     *
     */
    public void setUp() {

        Main.LOGGER.info("Successfully set up connection");
    }

    public DNFSIPeer getPeer() {
        return peer;
    }

    public void setPeer(DNFSIPeer peer) {
        this.peer = peer;
    }

    @Override
    /**
     * @param path
     * @return
     */
    public DNFSFolder getFolder(DNFSPath path) throws DNFSException.DNFSPathNotFound, DNFSException.DNFSNotFolderException {
        DNFSiNode iNode = this.resolve(path);
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
        DNFSiNode iNode = this.resolve(path);
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
    public DNFSiNode getINode(DNFSPath path) throws DNFSException.DNFSPathNotFound  {
        return this.resolve(path);
    }


    private DNFSiNode resolve(DNFSPath path) throws DNFSException.DNFSPathNotFound {
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

