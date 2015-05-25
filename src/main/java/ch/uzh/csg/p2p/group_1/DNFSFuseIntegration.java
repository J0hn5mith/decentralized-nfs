
/**
 * Created by janmeier on 28.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkNotInit;
import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
import net.fusejna.*;
import net.fusejna.types.TypeGid;
import net.fusejna.types.TypeMode;
import net.fusejna.types.TypeUid;
import net.fusejna.util.FuseFilesystemAdapterAssumeImplemented;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import java.nio.ByteBuffer;


public class DNFSFuseIntegration extends FuseFilesystemAdapterAssumeImplemented {

    final private static Logger LOGGER = Logger.getLogger(DNFSFuseIntegration.class.getName());

    private DNFSPathResolver pathResolver;

    /**
     *
     */
    public DNFSFuseIntegration() {
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


    @Override
    public int access(final String pathString, final int access) {
        return 0;
    }

    /**
     * Change the permission bits of a file.
     */
    @Override
    public int chmod(String pathAsString, TypeMode.ModeWrapper mode) {
        DNFSPath path = new DNFSPath(pathAsString);

        try{
            DNFSIiNode iNode = this.pathResolver.getINode(path);
            iNode.setMode(mode.mode());
        } catch (DNFSException.DNFSPathNotFound dnfsPathNotFound) {
            dnfsPathNotFound.printStackTrace();
        }
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

        DNFSFile file = null;
        try {
            file = DNFSFile.createNew(this.pathResolver.getPeer());
        } catch (DNFSException e) {
            LOGGER.error("Could not create new file", e);
            return -1;
        }
        try {
            file.getINode().setMode(mode.mode());
            file.getINode().setUid(this.getFuseContextUid());
            file.getINode().setGid(this.getFuseContextGid());
            targetFolder.addChild(file, fileName);
        } catch (DNFSException.DNFSNetworkNotInit DNFSNetworkNotInit) {
            DNFSNetworkNotInit.printStackTrace();
        }
        return 0;
    }

    /**
     * From FUSE API:
     * Get file attributes.
     * Similar to stat(). The 'st_dev' and 'st_blksize' fields are ignored. The 'st_ino' field is ignored except if the 'use_ino' mount option is given.
     */
    @Override
    public int getattr(final String path, final StructStat.StatWrapper stat) {

        DNFSIiNode iNode = null;
        try {
            iNode = this.pathResolver.getINode(new DNFSPath(path));
        } catch (DNFSException e) {
            LOGGER.info("Could not find attrs for path: " + path);
            LOGGER.debug("Reason: ", e);
            return -ErrorCodes.ENOENT();
        }

        if (iNode.isDir()) {
            TypeMode.ModeWrapper mode = new TypeMode.ModeWrapper(TypeMode.NodeType.DIRECTORY.getBits());
            mode.mode( mode.mode() | iNode.getMode());
            stat.mode(mode.mode());
            return 0;
        } else {
            TypeMode.ModeWrapper mode = new TypeMode.ModeWrapper(TypeMode.NodeType.FILE.getBits());
            mode.mode( mode.mode() | iNode.getMode());
            stat.mode(mode.mode()).size(iNode.getSize());
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

        try {
            targetFolder.addChild(DNFSFolder.createNew(this.pathResolver.getPeer()), folderName);
        } catch (DNFSException.DNFSNetworkNotInit DNFSNetworkNotInit) {
            DNFSNetworkNotInit.printStackTrace();
            return -1;
        }

        return 0;
    }

    /**
     *
     */
    @Override
    public int open(final String pathString, final StructFuseFileInfo.FileInfoWrapper info) {
        DNFSPath path = new DNFSPath(pathString);
        DNFSIiNode iNode = null;
        try {
            iNode = this.pathResolver.getINode(path);
        } catch (DNFSException.DNFSPathNotFound dnfsPathNotFound) {
            return -ErrorCodes.EEXIST();
        }
        if(!this.checkAccessRights(info.openMode(), iNode )){
            return -ErrorCodes.EACCES();
        }
        return 0;
    }


    @Override
    public int opendir(String pathString, StructFuseFileInfo.FileInfoWrapper info) {
        DNFSPath path = new DNFSPath(pathString);
        DNFSIiNode iNode = null;
        try {
            iNode = this.pathResolver.getINode(path);
        } catch (DNFSException.DNFSPathNotFound dnfsPathNotFound) {
            return -ErrorCodes.EEXIST();
        }
        if(!this.checkAccessRights(info.openMode(), iNode )){
            return -ErrorCodes.EACCES();
        }
        return 0;
    }

    /**
     *
     *
     * */
    @Override
    public int read(String path, final ByteBuffer buffer, final long size, long offset, StructFuseFileInfo.FileInfoWrapper info) {
        // Compute substring that we are being asked to read
        DNFSPath dnfsPath = new DNFSPath(path);
        try {
            DNFSFile file = this.pathResolver.getFile(dnfsPath);
            int bytesRead = file.read(buffer, size, offset);
            LOGGER.warn(String.format("%d bytes requested and %d bytes read.", size, bytesRead));
            return bytesRead;

        } catch (DNFSException.DNFSNotFileException e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.EISDIR();
        } catch (DNFSException.DNFSPathNotFound e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOENT();
        } catch (DNFSException.DNFSBlockStorageException e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOENT();
        } catch (DNFSException.DNFSNetworkNotInit e) {
            LOGGER.error(e.toString());
            return -1;
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
        } catch (DNFSNetworkNotInit e) {
            LOGGER.error(e.toString());
        }
        for (DNFSFolderEntry o : folder.getChildEntries()) {
            filler.add(o.getName());
        }

        return 0;
    }


    @Override
    public int rename(String path, String newName) {
        DNFSIiNode iNode;
        DNFSFolder newParentFolder;
        DNFSFolder oldParentFolder;

        //TODO: Clean up, there path is created multiple times
        try {
            iNode = this.pathResolver.getINode(new DNFSPath(path));
            oldParentFolder = this.pathResolver.getFolder(new DNFSPath(path).getParent());
            newParentFolder = this.pathResolver.getFolder(new DNFSPath(newName).getParent());
            newParentFolder.addChild(iNode, new DNFSPath(newName).getFilerName());
            oldParentFolder.removeChild(new DNFSPath(path).getFilerName().toString());
        } catch (DNFSException.DNFSPathNotFound e) {
            LOGGER.warn("Rename failed.", e);
            return -ErrorCodes.ENOENT();
        } catch (DNFSException.DNFSNotFolderException e) {
            LOGGER.warn("Rename failed.", e);
            return -ErrorCodes.ENOTDIR();
        } catch (DNFSException.NoSuchFileOrFolder e) {
            LOGGER.warn("Rename failed.", e);
            return -ErrorCodes.ENOENT();
        } catch (DNFSException.DNFSNetworkNotInit e) {
            LOGGER.warn("Rename failed.", e);
            return -1;
        }

        return 0;
    }

    @Override
    public int rmdir(String pathString) {
        DNFSFolder parentFolder;
        DNFSPath path;
        try {
            path = new DNFSPath(pathString);
            parentFolder = this.pathResolver.getFolder(path.getParent());
            parentFolder.removeChild(path.getComponent(-1));
        } catch (DNFSException e) {
            return -ErrorCodes.ENOENT();
        }
        return 0;
    }

    @Override
    public int truncate(final String pathString, final long offset) {
        DNFSPath path;
        try {
            path = new DNFSPath(pathString);
            DNFSFile file = this.pathResolver.getFile(path);
            file.truncate(offset);

        } catch (DNFSException e) {
            return -ErrorCodes.ENOENT();
        }

        return 0;
    }

    @Override
    public int write(
            String path,
            ByteBuffer buf,
            long bufSize,
            long writeOffset,
            StructFuseFileInfo.FileInfoWrapper info
    ) {
        try {
            DNFSFile file = this.pathResolver.getFile(new DNFSPath(path));
            if(!this.checkAccessRights(info.openMode(), file.getINode())){
                return -ErrorCodes.EACCES();
            };

            return _write(file, buf, bufSize, writeOffset);

        } catch (DNFSException e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOENT();
        }
    }

    public int _write(DNFSFile file, ByteBuffer buf, long bufSize, long writeOffset) throws DNFSNetworkNotInit, DNFSException.DNFSBlockStorageException {
        int bytesWritten = file.write(buf, bufSize, writeOffset);
        LOGGER.debug("File path has been written and has now " + file.getINode().getBlockIDs().size() + " blocks");
        return bytesWritten;

    }

    @Override
    public int unlink(String path) {
        try {

            if (path.equals("/")) {
                return -1;
            }

            DNFSPath entryPath = new DNFSPath(path);
            DNFSPath parentPath = entryPath.getParent();
            DNFSFolder parentFolder = this.pathResolver.getFolder(parentPath);

            parentFolder.removeChild(entryPath.getFilerName());

        } catch (DNFSException e) {
            LOGGER.warn("Faild to remove file");
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOENT();
        }
        return 0;
    }

    private boolean checkAccessRights(StructFuseFileInfo.FileInfoWrapper.OpenMode openMode, DNFSIiNode iNode){

        TypeUid uid = getFuseContextUid();
        TypeGid gid = getFuseContextGid();
        switch(openMode) {
            case READONLY:
                return iNode.getAccessRights().isAllowedToRead(uid, gid);
            case WRITEONLY:
                return iNode.getAccessRights().isAllowedToWrite(uid, gid);
            case READWRITE:
                return iNode.getAccessRights().isAllowedToWriteAndRead(uid, gid);
            default:
                return false;
        }
    }


}
