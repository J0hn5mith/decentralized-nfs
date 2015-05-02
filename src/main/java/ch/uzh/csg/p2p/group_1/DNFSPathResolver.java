/**
 * Created by janmeier on 29.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class DNFSPathResolver implements DNFSIPathResolver {
    final private static Logger LOGGER = Logger.getLogger(DNFSFolder.class.getName());
    private DNFSConfigurator config;
    private DNFSIPeer peer;

    /**
     * @param config
     */
    public DNFSPathResolver(DNFSConfigurator config) {
        this.config = config;
        Main.LOGGER.setLevel(Level.WARN);
    }

    /**
     *
     */
    public void setUp() {

        try {
            this.setPeer(new DNFSDummyPeer());
            this.getPeer().setUp();
        } catch (IOException e) {
            e.printStackTrace();
            Main.LOGGER.error("Failed to set up peer");
        }

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
    public DNFSFolder getFolder(DNFSPath path) throws DNFSException {
        DNFSiNode iNode = this.resolve(path);
        return DNFSFolder.getExistingFolder(iNode, this.getPeer());
    }

    @Override
    /**
     * @param path
     * @return
     */
    public DNFSFile getFile(DNFSPath path) throws DNFSException {
        DNFSiNode iNode = this.resolve(path);
        return new DNFSFile(iNode, this.getPeer());
    }

    @Override
    /**
     * @param path
     * @return
     */
    public DNFSiNode getINode(DNFSPath path) throws DNFSException {
        return this.resolve(path);
    }


    private DNFSiNode resolve(DNFSPath path) throws DNFSException {
        DNFSFolder currentFolder = this.getRootFolder();
        if (path.length() == 0) {
            return currentFolder.getINode();
        }

        for (String pathComponent : path.getComponents(0, -1)) {
            currentFolder = currentFolder.getChildFolder(pathComponent);

        }

        return currentFolder.getChildINode(path.getComponent(-1));
    }


    /**
     * @throws IOException
     */
    private DNFSFolder getRootFolder() throws DNFSException {
        return DNFSFolder.getExistingFolder(peer.getRootINode(), this.getPeer());
    }

}

