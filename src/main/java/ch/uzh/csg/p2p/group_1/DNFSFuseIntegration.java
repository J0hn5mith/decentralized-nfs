
/**
 * Created by janmeier on 28.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import net.fusejna.*;
import net.fusejna.types.TypeMode;

import java.io.File;
import java.nio.ByteBuffer;

import net.fusejna.util.FuseFilesystemAdapterFull;

import org.apache.log4j.Level;


public class DNFSFuseIntegration extends FuseFilesystemAdapterFull {
	
    private DNFSPathResolver pathResolver;

    /**
     * 
     */
    public DNFSFuseIntegration() {
        super();
        this.log(true);
        Main.LOGGER.setLevel(Level.DEBUG);
    }

    /**
     * 
     * @param pathResolver
     */
    public void setPathResolver(DNFSPathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    @Override
    public int getattr(final String path, final StructStat.StatWrapper stat) {
        DNFSiNode iNode = this.pathResolver.getINode(path);
        if(iNode.isDir()) { // Root directory
            stat.setMode(TypeMode.NodeType.DIRECTORY);
            return 0;
        } else {
            DNFSFile file = new DNFSFile(iNode, this.pathResolver);
            stat.setMode(TypeMode.NodeType.FILE).size(file.getData().length());
            return 0;
        }
        //return -ErrorCodes.ENOENT(); // No needed right now because getattr cannot fail
    }
    
    /**
     * 
     */
    @Override
    public int read(String path, final ByteBuffer buffer, final long size, long offset, StructFuseFileInfo.FileInfoWrapper info) {
        // Compute substring that we are being asked to read
        String content = this.pathResolver.getFile(path).getData();
        final String s = content.substring((int) offset,
                (int) Math.max(offset, Math.min(content.length() - offset, offset + size)));
        buffer.put(s.getBytes());
        return s.getBytes().length;
    }

    @Override
    public int readdir(final String path, final DirectoryFiller filler) {
        DNFSFolder folder = pathResolver.getFolder(path);
        for(DNFSFolder.DNFSFolderEntry o : folder.getEntries()) {
            filler.add(o.getName());
        }

        return 0;
    }

    /**
     * Change the permission bits of a file.
     */
    @Override
    public int chmod(String path, TypeMode.ModeWrapper mode) {
        Main.LOGGER.debug("chmod() was called");
        return 0;
    }

    /**
     * Change the owner and group of a file.
     */
    @Override
    public int chown(String path, long uid, long gid) {
        Main.LOGGER.debug("chown() was called");
        return 0;
    }

    /**
     * From FUSE API:
     * Create and open a file
     * If the file does not exist, first create it with the specified mode, and then open it.
     * If this method is not implemented or under Linux kernel versions earlier than 2.6.15, the mknod() and open() methods will be called instead.
     */
    @Override
    public int create(String path, TypeMode.ModeWrapper mode, StructFuseFileInfo.FileInfoWrapper info) {
        Main.LOGGER.debug("create() was called");
        return 0;
    }

    /**
     * From FUSE API:
     * Clean up file system.
     * Called on file system exit.
     */
    @Override
    public void destroy() {
        Main.LOGGER.debug("destory() was called");

    }

    @Override
    public int fgetattr(String path, StructStat.StatWrapper stat, StructFuseFileInfo.FileInfoWrapper info) {
        Main.LOGGER.debug("fgetattr was called");
        return 0;
    }

    @Override
    public int flush(String path, StructFuseFileInfo.FileInfoWrapper info) {
        Main.LOGGER.debug("flush was called");
        return 0;
    }

    @Override
    public int fsync(String path, int datasync, StructFuseFileInfo.FileInfoWrapper info) {
        Main.LOGGER.debug("fsync was called");
        return 0;
    }

    @Override
    public int fsyncdir(String path, int datasync, StructFuseFileInfo.FileInfoWrapper info) {
        Main.LOGGER.debug("fsyncdir was called");
        return 0;
    }

    @Override
    public int ftruncate(String path, long offset, StructFuseFileInfo.FileInfoWrapper info) {
        Main.LOGGER.debug("ftruncate was called");
        return 0;
    }


    @Override
    protected String getName() {
        Main.LOGGER.debug("getName was called");
        return "Fuse HD";
    }

    @Override
    protected String[] getOptions() {
        Main.LOGGER.debug("getOptions was called");
        return new String[0];
    }

    @Override
    public int getxattr(String path, String xattr, XattrFiller filler, long size, long position) {
        Main.LOGGER.debug("getaxattr was called");
        return 0;
    }

    @Override
    public void init() {
        Main.LOGGER.debug("init was called");

    }

    @Override
    public int link(String path, String target) {
        Main.LOGGER.debug("link was called");
        return 0;
    }

    @Override
    public int listxattr(String path, XattrListFiller filler) {
        Main.LOGGER.debug("listaxattr was called");
        return 0;
    }

    @Override
    public int lock(String path, StructFuseFileInfo.FileInfoWrapper info, FlockCommand command, StructFlock.FlockWrapper flock) {
        Main.LOGGER.debug("lock was called");
        return 0;
    }

    @Override
    public int mkdir(String path, TypeMode.ModeWrapper mode) {
        Main.LOGGER.debug("mkdir was called");
        return 0;
    }

    @Override
    public int mknod(String path, TypeMode.ModeWrapper mode, long dev) {
        Main.LOGGER.debug("mknod was called");
        return 0;
    }

    @Override
    public int open(String path, StructFuseFileInfo.FileInfoWrapper info) {
        Main.LOGGER.debug("open was called");
        return 0;
    }

    @Override
    public int opendir(String path, StructFuseFileInfo.FileInfoWrapper info) {
        Main.LOGGER.debug("opedir was called");
        return 0;
    }



    @Override
    public int readlink(String path, ByteBuffer buffer, long size) {
        Main.LOGGER.debug("readlink was called");
        return 0;
    }

    @Override
    public int release(String path, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int releasedir(String path, StructFuseFileInfo.FileInfoWrapper info) {
        Main.LOGGER.debug("relasedir was called");
        return 0;
    }

    @Override
    public int removexattr(String path, String xattr) {
        Main.LOGGER.debug("removeattr was called");
        return 0;
    }

    @Override
    public int rename(String path, String newName) {
        Main.LOGGER.debug("rename was called");
        return 0;
    }

    @Override
    public int rmdir(String path) {
        Main.LOGGER.debug("rmdir was called");
        return 0;
    }

    @Override
    public int setxattr(String path, String xattr, ByteBuffer value, long size, int flags, int position) {
        Main.LOGGER.debug("setxattr was called");
        return 0;
    }

    @Override
    public int statfs(String path, StructStatvfs.StatvfsWrapper wrapper) {
        Main.LOGGER.debug("statfs was called");
        return 0;
    }

    @Override
    public int symlink(String path, String target) {
        Main.LOGGER.debug("symlink was called");
        return 0;
    }

    @Override
    public int truncate(String path, long offset) {
        Main.LOGGER.debug("truncate was called");
        return 0;
    }

    @Override
    public int unlink(String path) {
        Main.LOGGER.debug("unlink was called");
        return 0;
    }

    @Override
    public int utimens(String path, StructTimeBuffer.TimeBufferWrapper wrapper) {
        Main.LOGGER.debug("utimens was called");
        return 0;
    }

    @Override
    public int write(String path, ByteBuffer buf, long bufSize, long writeOffset, StructFuseFileInfo.FileInfoWrapper info) {
        Main.LOGGER.debug("write was called");
        return 0;
    }
    
    /*
     * =================================
     * Unused methods 
     * =================================
     */
    
    /**
     * From FUSE API:
     * Check file access permissions
     * This will be called for the access() system call. If the 'default_permissions' mount option is given, this method is not called.
     */
    @Override
    public int access(String path, int access) {
        Main.LOGGER.debug("access() was called.");
        return 0;
    }

    /**
     * Called after the FUSE file system is unmounted.
     */
    @Override
    public void afterUnmount(File mountPoint) {
        Main.LOGGER.debug("afterUnmount() was called");
    }

    /**
     * Called before the FUSE file system is mounted.
     */
    @Override
    public void beforeMount(File mountPoint) {
        Main.LOGGER.debug("beforeMount() was called");
    }

    /**
     * From FUSE API:
     * Map block index within file to block index within device
     * Note: This makes sense only for block device backed file systems mounted with the 'blkdev' option
     */
    @Override
    public int bmap(String path, StructFuseFileInfo.FileInfoWrapper info) {
        Main.LOGGER.debug("bmap() was called");
        return 0;
    }
    
}
