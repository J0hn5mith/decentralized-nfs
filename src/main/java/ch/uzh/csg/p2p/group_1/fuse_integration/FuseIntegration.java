
/**
 * Created by janmeier on 28.03.15.
 */
package ch.uzh.csg.p2p.group_1.fuse_integration;

import ch.uzh.csg.p2p.group_1.Settings;
import ch.uzh.csg.p2p.group_1.file_system.PathResolver;
import ch.uzh.csg.p2p.group_1.file_system.DNFSPath;
import ch.uzh.csg.p2p.group_1.file_system.File;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException.DNFSBlockStorageException;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException.DNFSNetworkNotInit;
import ch.uzh.csg.p2p.group_1.file_system.DirectoryINodeMapEntry;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.file_system.Directory;
import net.fusejna.*;
import net.fusejna.types.TypeGid;
import net.fusejna.types.TypeMode;
import net.fusejna.types.TypeUid;
import net.fusejna.util.FuseFilesystemAdapterAssumeImplemented;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.ExternallyRolledFileAppender;

import java.nio.ByteBuffer;


public class FuseIntegration extends FuseFilesystemAdapterAssumeImplemented {

    final private static Logger LOGGER = Logger.getLogger(FuseIntegration.class.getName());

    private PathResolver pathResolver;

    /**
     *
     */
    public FuseIntegration() {
        super();
        LOGGER.setLevel(Level.WARN);
    }

    public FuseIntegration setUp(Settings settings) {
        this.log(settings.getConfig().getBoolean("FuseLogging"));
        return this;
    }

    /**
     * @param pathResolver
     */
    public void setPathResolver(PathResolver pathResolver) {
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
        try {
            DNFSIiNode iNode = this.pathResolver.getINode(path);
            if (!this.checkModeChangeRights(iNode)) {
                LOGGER.info("Could execute chmod because the user is not the owner of the file.");
                return -ErrorCodes.EPERM();
            }
            iNode.setMode(mode.mode());
        } catch (DNFSException.DNFSPathNotFound dnfsPathNotFound) {
            LOGGER.info("Could execute chmod because file does not exist.");
            return -ErrorCodes.EPERM();
        }
        return 0;
    }

    /**
     * Change the owner and group of a file.
     */
    @Override
    public int chown(String pathAsString, long uid, long gid) {
        try {
            return _chown(pathAsString, uid, gid);
        } catch (DNFSException.DNFSPathNotFound dnfsPathNotFound) {
            dnfsPathNotFound.printStackTrace();
        }
        return 0;
    }

    private int _chown(String pathAsString, long uid, long gid) throws DNFSException.DNFSPathNotFound {
        DNFSPath path = new DNFSPath(pathAsString);
        DNFSIiNode iNode = this.pathResolver.getINode(path);
        if (!this.checkModeChangeRights(iNode)) {
            LOGGER.error("Could not change the owner of the file or directory because the user does not own it.");
            return -ErrorCodes.EPERM();
        }
        iNode.setGid(new TypeGid(gid));
        iNode.setUid(new TypeUid(uid));
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

        try {

            DNFSPath dnfsPath = new DNFSPath(path);
            String fileName = dnfsPath.getComponent(-1);
            DNFSPath subPath = dnfsPath.getSubPath(0, -1);

            Directory targetDir = this.pathResolver.getDirectory(subPath);
            if (targetDir.hasChild(fileName)) {
                return -ErrorCodes.EEXIST();
            }

            File file = File.createNew(this.pathResolver.getStorage());

            DNFSIiNode fileINode = file.getINode();

            fileINode.setMode(mode.mode());
            fileINode.setUid(this.getFuseContextUid());
            fileINode.setGid(this.getFuseContextGid());

            targetDir.addChild(fileINode, fileName);

        } catch (DNFSException.DNFSPathNotFound dnfsPathNotFound) {
            return -ErrorCodes.ENOENT();
        } catch (DNFSException.DNFSNotDirectoryException e) {
            return -ErrorCodes.ENOTDIR();
        } catch (DNFSException.INodeStorageException e) {
            e.printStackTrace();
            LOGGER.error("Creating new file failed because iNode could not be created", e);
            return -1;
        } catch (DNFSBlockStorageException e) {
            return -1;
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
            mode.mode(mode.mode() | iNode.getMode());
            stat.mode(mode.mode());
            return 0;
        } else {
            TypeMode.ModeWrapper mode = new TypeMode.ModeWrapper(TypeMode.NodeType.FILE.getBits());
            mode.mode(mode.mode() | iNode.getMode());
            stat.mode(mode.mode()).size(iNode.getSize());
            return 0;
        }
    }


    @Override
    public int mkdir(String path, TypeMode.ModeWrapper mode) {

        try {

            DNFSPath dnfsPath = new DNFSPath(path);
            String dirName = dnfsPath.getComponent(-1);
            DNFSPath subPath = dnfsPath.getSubPath(0, -1);

            Directory targetDir = this.pathResolver.getDirectory(subPath);
            if (targetDir.hasChild(dirName)) {
                return -ErrorCodes.EEXIST();
            }

            Directory directory = Directory.createNew(this.pathResolver.getStorage());

            DNFSIiNode dirINode = directory.getINode();
            dirINode.setGid(this.getFuseContextGid());
            dirINode.setUid(this.getFuseContextUid());

            targetDir.addChild(dirINode, dirName);

        } catch (DNFSException.DNFSNetworkNotInit e) {
            e.printStackTrace();
            return -ErrorCodes.ENOENT();

        } catch (DNFSBlockStorageException e) {
            e.printStackTrace();
            return -ErrorCodes.ENOENT();

        } catch (DNFSException e) {
            e.printStackTrace();
            return -ErrorCodes.ENOENT();
        }

        return 0;
    }


    @Override
    public int open(final String pathString, final StructFuseFileInfo.FileInfoWrapper info) {
        DNFSPath path = new DNFSPath(pathString);
        DNFSIiNode iNode = null;
        try {
            iNode = this.pathResolver.getINode(path);
        } catch (DNFSException.DNFSPathNotFound dnfsPathNotFound) {
            return -ErrorCodes.EEXIST();
        }
        if (!this.checkAccessRights(info.openMode(), iNode)) {
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
        if (!this.checkAccessRights(info.openMode(), iNode)) {
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
            File file = this.pathResolver.getFile(dnfsPath);
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
        Directory directory = null;
        try {
            directory = pathResolver.getDirectory(new DNFSPath(path));
        } catch (DNFSException.DNFSPathNotFound e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOENT();
        } catch (DNFSException.DNFSNotDirectoryException e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOTDIR();
        } catch (DNFSBlockStorageException e) {
            return -1;
        }
        for (DirectoryINodeMapEntry iNodeMapEntry : directory.getINodeMap()) {
            filler.add(iNodeMapEntry.getName());
        }

        return 0;
    }


    @Override
    public int rename(String oldPathString, String newPathString) {

        try {

            DNFSPath oldPath = new DNFSPath(oldPathString);
            DNFSPath newPath = new DNFSPath(newPathString);

            DNFSIiNode iNode = this.pathResolver.getINode(oldPath);

            Directory oldParentDir = this.pathResolver.getDirectory(oldPath.getParent());
            Directory newParentDir = this.pathResolver.getDirectory(newPath.getParent());
            
            oldParentDir.removeChild(oldPath.getFileName());
            newParentDir.addChild(iNode, newPath.getFileName());
            
        } catch (DNFSException.DNFSPathNotFound e) {
            return -ErrorCodes.ENOENT();
        } catch (DNFSException.DNFSNotDirectoryException e) {
            return -ErrorCodes.ENOTDIR();
        } catch (DNFSException.NoSuchFileOrDirectory e) {
            return -ErrorCodes.ENOENT();
        } catch (DNFSException.DNFSNetworkNotInit e) {
            return -ErrorCodes.ENOENT();
        } catch (DNFSException e) {
            return -ErrorCodes.ENOENT();
        }

        return 0;
    }

    @Override
    public int rmdir(String pathString) {
        Directory parentFolder;
        DNFSPath path;
        try {
            path = new DNFSPath(pathString);
            parentFolder = this.pathResolver.getDirectory(path.getParent());
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
            File file = this.pathResolver.getFile(path);
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
            File file = this.pathResolver.getFile(new DNFSPath(path));
            if (!this.checkAccessRights(info.openMode(), file.getINode())) {
                return -ErrorCodes.EACCES();
            }
            ;

            return _write(file, buf, bufSize, writeOffset);

        } catch (DNFSException e) {
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOENT();
        }
    }

    public int _write(File file, ByteBuffer buf, long bufSize, long writeOffset) throws DNFSNetworkNotInit, DNFSException.DNFSBlockStorageException {
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
            Directory parentFolder = this.pathResolver.getDirectory(parentPath);

            parentFolder.removeChild(entryPath.getFileName());

        } catch (DNFSException e) {
            LOGGER.warn("Faild to remove file");
            LOGGER.error(e.toString());
            return -ErrorCodes.ENOENT();
        }
        return 0;
    }

    private boolean checkModeChangeRights(DNFSIiNode iNode) {
        return iNode.getUid() == getFuseContextUid();
    }

    private boolean checkAccessRights(StructFuseFileInfo.FileInfoWrapper.OpenMode openMode, DNFSIiNode iNode) {

        TypeUid uid = getFuseContextUid();
        TypeGid gid = getFuseContextGid();
        switch (openMode) {
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
