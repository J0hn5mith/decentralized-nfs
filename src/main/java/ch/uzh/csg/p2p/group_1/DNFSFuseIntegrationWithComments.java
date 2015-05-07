
/**
 * Created by janmeier on 28.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.StructFuseFileInfo;
import net.fusejna.StructStat;
import net.fusejna.types.TypeMode;
import net.fusejna.util.FuseFilesystemAdapterAssumeImplemented;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;


public class DNFSFuseIntegrationWithComments extends FuseFilesystemAdapterAssumeImplemented {
    final private static Logger LOGGER = Logger.getLogger(DNFSFuseIntegrationWithComments.class.getName());

    private DNFSPathResolver pathResolver;

    /**
     *
     */
    public DNFSFuseIntegrationWithComments() {
        super();
        this.log(false);
        LOGGER.setLevel(Level.WARN);
    }

    /**
     * @param pathResolver
     */
    public void setPathResolver(DNFSPathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    /**
     * Change the permission bits of a file.
     */
    @Override
    public int chmod(String path, TypeMode.ModeWrapper mode) {
        LOGGER.debug("chmod() was called");
        return 0;
    }

    /**
     * Change the owner and group of a file.
     */
    @Override
    public int chown(String path, long uid, long gid) {
        LOGGER.debug("chown() was called");
        return 0;
    }

    /**
     * From FUSE API:
     * OPTIONAL!
     * Create and open a file
     * If the file does not exist, first create it with the specified mode, and then open it.
     * If this method is not implemented or under Linux kernel versions earlier than 2.6.15, the mknod() and open() methods will be called instead.
     */
    @Override
    public int create(String path, TypeMode.ModeWrapper mode, StructFuseFileInfo.FileInfoWrapper info) {
        LOGGER.debug(String.format("create() was called.\nPath:%s\n mode: %s'\n info: %s", path, mode.toString(), info.toString()));
        DNFSPath dnfsPath = new DNFSPath(path);
        String fileName = dnfsPath.getComponent(-1);
        DNFSPath subPath = dnfsPath.getSubPath(0, -1);

        DNFSFolder targetFolder = null;
        try {
            targetFolder = this.pathResolver.getFolder(subPath);
        } catch (DNFSException e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOENT();
        }

        if (targetFolder.hasChild(fileName)) {
            return -ErrorCodes.EEXIST();
        }
        DNFSFile file = DNFSFile.createNew(this.pathResolver.getPeer());
        targetFolder.addChild(file, fileName);
        return 0;
    }

    /**
     * From FUSE API:
     * Get file attributes.
     * Similar to stat(). The 'st_dev' and 'st_blksize' fields are ignored. The 'st_ino' field is ignored except if the 'use_ino' mount option is given.
     */
    @Override
    public int getattr(final String path, final StructStat.StatWrapper stat) {

        DNFSiNode iNode = null;
        try {
            iNode = this.pathResolver.getINode(new DNFSPath(path));
        } catch (DNFSException e) {
            LOGGER.warn("Could not find attrs for path: " + path);
            return -ErrorCodes.ENOENT();
        }

        if (iNode.isDir()) {
            stat.setMode(TypeMode.NodeType.DIRECTORY);
            return 0;
        } else {
            DNFSFile file = DNFSFile.getExisting(iNode, this.pathResolver.getPeer());
            stat.setMode(TypeMode.NodeType.FILE).size(file.getINode().getSize());
            return 0;
        }
    }


    @Override
    public int mkdir(String path, TypeMode.ModeWrapper mode) {
        DNFSPath dnfsPath = new DNFSPath(path);
        String folderName = dnfsPath.getComponent(-1);
        DNFSPath subPath = dnfsPath.getSubPath(0, -1);
        DNFSFolder targetFolder = null;
        try {
            targetFolder = this.pathResolver.getFolder(subPath);
        } catch (DNFSException e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOENT();
        }

        if (targetFolder.hasChild(folderName)) {
            return -ErrorCodes.EEXIST();
        }

        targetFolder.addChild(DNFSFolder.createNew(this.pathResolver.getPeer()), folderName);

        return 0;
    }


    /**
     *
     */
    @Override
    public int read(String path, final ByteBuffer buffer, final long size, long offset, StructFuseFileInfo.FileInfoWrapper info) {
        // Compute substring that we are being asked to read
        DNFSPath dnfsPath = new DNFSPath(path);
        try {
            DNFSFile file = this.pathResolver.getFile(dnfsPath);
            return file.read(buffer, size, offset);
        } catch (DNFSException.DNFSNotFileException e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.EISDIR();
        } catch (DNFSException.DNFSPathNotFound e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOENT();
        }
    }

    @Override
    public int readdir(final String path, final DirectoryFiller filler) {
        DNFSFolder folder = null;
        try {
            folder = pathResolver.getFolder(new DNFSPath(path));
        } catch (DNFSException.DNFSPathNotFound e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOENT();
        } catch (DNFSException.DNFSNotFolderException e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOTDIR();
        }
        for (DNFSFolder.DNFSFolderEntry o : folder.getEntries()) {
            filler.add(o.getName());
        }

        return 0;
    }


    @Override
    public int rename(String path, String newName) {
        DNFSiNode iNode;
        DNFSFolder newParentFolder;
        DNFSFolder oldParentFolder;
        try {
            iNode = this.pathResolver.getINode(new DNFSPath(path));
            oldParentFolder = this.pathResolver.getFolder(new DNFSPath(path).getSubPath(0, -1));
        } catch (DNFSException e) {
            return -ErrorCodes.ENOENT();
        }

        try {
            newParentFolder = this.pathResolver.getFolder(new DNFSPath(newName).getSubPath(0, -1));
        } catch (DNFSException.DNFSPathNotFound dnfsPathNotFound) {
            return -ErrorCodes.ENOENT();
        } catch (DNFSException.DNFSNotFolderException e) {
            return -ErrorCodes.ENOTDIR();
        }

        oldParentFolder.removeChild(new DNFSPath(path).getSubPath(-1).toString());
        newParentFolder.addChild(iNode, new DNFSPath(newName).getSubPath(-1).toString());

        return 0;
    }

    @Override
    public int rmdir(String path) {
        LOGGER.debug("rmdir was called");
        return 0;
    }

    @Override
    public int write(String path, ByteBuffer buf, long bufSize, long writeOffset, StructFuseFileInfo.FileInfoWrapper info) {
        try {
            DNFSFile file = this.pathResolver.getFile(new DNFSPath(path));
            return file.write(buf, bufSize, writeOffset);

        } catch (DNFSException e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOENT();
        }
    }
    

}
