
/**
 * Created by janmeier on 28.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import net.fusejna.*;
import net.fusejna.types.TypeMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import net.fusejna.util.FuseFilesystemAdapterFull;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class DNFSFuseIntegration extends FuseFilesystemAdapterFull {
    final private static Logger LOGGER = Logger.getLogger(DNFSFolder.class.getName());

    private DNFSPathResolver pathResolver;

    /**
     * 
     */
    public DNFSFuseIntegration() {
        super();
        this.log(false);
        LOGGER.setLevel(Level.DEBUG);
    }

    /**
     * 
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
        LOGGER.debug("create() was called");
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


        if(iNode.isDir()) { // Root directory
            stat.setMode(TypeMode.NodeType.DIRECTORY);
            return 0;
        } else {
            DNFSFile file = DNFSFile.createNewFile(this.pathResolver.getPeer());
            stat.setMode(TypeMode.NodeType.FILE).size(file.getINode().getSize());
            return 0;
        }
    }

    @Override
    protected String getName() {
        LOGGER.debug("getName was called");
        return "Fuse HD";
    }

    @Override
    protected String[] getOptions() {
        LOGGER.debug("getOptions was called");
        return new String[0];
    }

    @Override
    public int getxattr(String path, String xattr, XattrFiller filler, long size, long position) {
        LOGGER.debug("getaxattr was called");
        return 0;
    }

    @Override
    public void init() {
        LOGGER.debug("init was called");

    }

    @Override
    public int link(String path, String target) {
        LOGGER.debug("link was called");
        return 0;
    }

    @Override
    public int listxattr(String path, XattrListFiller filler) {
        LOGGER.debug("listaxattr was called");
        return 0;
    }

    /**
     * From FUSE API:
     * OPTIONAL!
     */
    @Override
    public int lock(String path, StructFuseFileInfo.FileInfoWrapper info, FlockCommand command, StructFlock.FlockWrapper flock) {
        LOGGER.debug("lock was called");
        return 0;
    }

    @Override
    public int mkdir(String path, TypeMode.ModeWrapper mode) {
        LOGGER.debug("mkdir was called for path: " + path);

        DNFSPath dnfsPath = new DNFSPath(path);
        String folderName = dnfsPath.getComponent(-1);
        DNFSPath subPath = dnfsPath.getSubPath(0, -1);
        DNFSFolder targetFolder = null;
        try {
            targetFolder = this.pathResolver.getFolder(subPath);
        } catch (DNFSException e) {
            e.printStackTrace();
            return -1; //TODO: return proper error code
        }

        targetFolder.addChildFolder(DNFSFolder.createNewFolder(this.pathResolver.getPeer()), folderName);

        return 0;
    }

    @Override
    public int mknod(String path, TypeMode.ModeWrapper mode, long dev) {
        LOGGER.debug("mknod was called");

        return 0;
    }

    /**
     * From FUSE API:
     * OPTIONAL!
     */
    @Override
    public int open(String path, StructFuseFileInfo.FileInfoWrapper info) {
        LOGGER.debug("open was called");
        return 0;
    }

    /**
     * From FUSE API:
     * OPTIONAL!
     */
    @Override
    public int opendir(String path, StructFuseFileInfo.FileInfoWrapper info) {
        LOGGER.debug("opedir was called");
        return 0;
    }

    /**
     * 
     */
    @Override
    public int read(String path, final ByteBuffer buffer, final long size, long offset, StructFuseFileInfo.FileInfoWrapper info) {
        // Compute substring that we are being asked to read
        DNFSPath dnfsPath = new DNFSPath(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(this.pathResolver.getFile(dnfsPath).getInputStream()));
        String content = null;
        try {
            content = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final String s = content.substring((int) offset,
                (int) Math.max(offset, Math.min(content.length() - offset, offset + size)));
        buffer.put(s.getBytes());
        return s.getBytes().length;
    }

    @Override
    public int readdir(final String path, final DirectoryFiller filler) {
        DNFSFolder folder = null;
        try {
            folder = pathResolver.getFolder(new DNFSPath(path));
        } catch (DNFSException e) {
            e.printStackTrace();
            return -1; //TODO: return proper error code
        }
        for(DNFSFolder.DNFSFolderEntry o : folder.getEntries()) {
            filler.add(o.getName());
        }

        return 0;
    }
    
    @Override
    public int readlink(String path, ByteBuffer buffer, long size) {
        LOGGER.debug("readlink was called");
        return 0;
    }

    /**
     * From FUSE API:
     * OPTIONAL!
     */
    @Override
    public int release(String path, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    /**
     * From FUSE API:
     * OPTIONAL!
     */
    @Override
    public int releasedir(String path, StructFuseFileInfo.FileInfoWrapper info) {
        LOGGER.debug("relasedir was called");
        return 0;
    }

    @Override
    public int removexattr(String path, String xattr) {
        LOGGER.debug("removeattr was called");
        return 0;
    }

    @Override
    public int rename(String path, String newName) {
        LOGGER.debug("rename was called");
        return 0;
    }

    @Override
    public int rmdir(String path) {
        LOGGER.debug("rmdir was called");
        return 0;
    }

    @Override
    public int setxattr(String path, String xattr, ByteBuffer value, long size, int flags, int position) {
        LOGGER.debug("setxattr was called");
        return 0;
    }

    @Override
    public int statfs(String path, StructStatvfs.StatvfsWrapper wrapper) {
        LOGGER.debug("statfs was called");
        return 0;
    }

    @Override
    public int symlink(String path, String target) {
        LOGGER.debug("symlink was called");
        return 0;
    }

    @Override
    public int truncate(String path, long offset) {
        LOGGER.debug("truncate was called");
        return 0;
    }

    @Override
    public int unlink(String path) {
        LOGGER.debug("unlink was called");
        return 0;
    }

    @Override
    public int utimens(String path, StructTimeBuffer.TimeBufferWrapper wrapper) {
        LOGGER.debug("utimens was called");
        return 0;
    }

    @Override
    public int write(String path, ByteBuffer buf, long bufSize, long writeOffset, StructFuseFileInfo.FileInfoWrapper info) {
        LOGGER.debug("write was called");
        return 0;
    }
    
    /*
     * =================================
     * Unused methods 
     * =================================
     */
    
    /**
     * From FUSE API:
     * OPTIONAL!
     * Check file access permissions
     * This will be called for the access() system call. If the 'default_permissions' mount option is given, this method is not called.
     */
    @Override
    public int access(String path, int access) {
        LOGGER.debug("access() was called.");
        return 0;
    }

    /**
     * Called after the FUSE file system is unmounted.
     */
    @Override
    public void afterUnmount(File mountPoint) {
        LOGGER.debug("afterUnmount() was called");
    }

    /**
     * Called before the FUSE file system is mounted.
     */
    @Override
    public void beforeMount(File mountPoint) {
        LOGGER.debug("beforeMount() was called");
    }

    /**
     * From FUSE API:
     * Map block index within file to block index within device
     * Note: This makes sense only for block device backed file systems mounted with the 'blkdev' option
     */
    @Override
    public int bmap(String path, StructFuseFileInfo.FileInfoWrapper info) {
        LOGGER.debug("bmap() was called");
        return 0;
    }
    
    /**
     * From FUSE API:
     * OPTIONAL!
     * Clean up file system.
     * Called on file system exit.
     */
    @Override
    public void destroy() {
        LOGGER.debug("destory() was called");
    }
    
    /**
     * From FUSE API:
     * OPTIONAL!
     * Get attributes from an open file
     * This method is called instead of the getattr() method if the file information is available.
     * Currently this is only called after the create() method if that is implemented (see above). Later it may be called for invocations of fstat() too.
     */
    @Override
    public int fgetattr(String path, StructStat.StatWrapper stat, StructFuseFileInfo.FileInfoWrapper info) {
        LOGGER.debug("fgetattr() was called");
        return 0;
    }
    
    /**
     * From FUSE API:
     * OPTIONAL!
     * Possibly flush cached data.
     * BIG NOTE: This is not equivalent to fsync(). It's not a request to sync dirty data.
     * Flush is called on each close() of a file descriptor. So if a filesystem wants to return write errors in close() and the file has cached dirty data, this is a good place to write back data and return any errors. Since many applications ignore close() errors this is not always useful.
     * NOTE: The flush() method may be called more than once for each open(). This happens if more than one file descriptor refers to an opened file due to dup(), dup2() or fork() calls. It is not possible to determine if a flush is final, so each flush should be treated equally. Multiple write-flush sequences are relatively rare, so this shouldn't be a problem.
     * Filesystems shouldn't assume that flush will always be called after some writes, or that if will be called at all.
     */
    @Override
    public int flush(String path, StructFuseFileInfo.FileInfoWrapper info) {
        LOGGER.debug("flush() was called");
        return 0;
    }
    
    /**
     * From FUSE API:
     * OPTIONAL!
     * Synchronize file contents
     * If the datasync parameter is non-zero, then only the user data should be flushed, not the meta data.
     */
    @Override
    public int fsync(String path, int datasync, StructFuseFileInfo.FileInfoWrapper info) {
        LOGGER.debug("fsync() was called");
        return 0;
    }
    
    /**
     * From FUSE API:
     * OPTIONAL!
     * Synchronize directory contents
     * If the datasync parameter is non-zero, then only the user data should be flushed, not the meta data.
     */
    @Override
    public int fsyncdir(String path, int datasync, StructFuseFileInfo.FileInfoWrapper info) {
        LOGGER.debug("fsyncdir() was called");
        return 0;
    }
    
    /**
     * From FUSE API:
     * OPTIONAL!
     * Change the size of an open file
     * This method is called instead of the truncate() method if the truncation was invoked from an ftruncate() system call.
     * If this method is not implemented or under Linux kernel versions earlier than 2.6.15, the truncate() method will be called instead.
     */
    @Override
    public int ftruncate(String path, long offset, StructFuseFileInfo.FileInfoWrapper info) {
        LOGGER.debug("ftruncate() was called");
        return 0;
    }

}
