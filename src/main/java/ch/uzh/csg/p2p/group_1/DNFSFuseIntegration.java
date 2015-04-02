
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
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LogLevel;

//public class DNFSFuseIntegration extends FuseFilesystemAdapterFull {
////    public static void main(final String... args) throws FuseException
////    {
////        if (args.length != 1) {
////            System.err.println("Usage: HelloFS <mountpoint>");
////            System.exit(1);
////        }
////        new HelloFS().log(true).mount(args[0]);
////    }
//
//    final String filename = "/hello.txt";
//    final String contents = "Hello World!\n";
//
//    @Override
//    public int getattr(final String path, final StructStat.StatWrapper stat) {
//        if (path.equals(File.separator)) { // Root directory
//            stat.setMode(TypeMode.NodeType.DIRECTORY);
//            return 0;
//        }
//        if (path.equals(filename)) { // hello.txt
//            stat.setMode(TypeMode.NodeType.FILE).size(contents.length());
//            return 0;
//        }
//        return -ErrorCodes.ENOENT();
//    }
//
//    @Override
//    public int read(final String path, final ByteBuffer buffer, final long size, final long offset,
//                    final StructFuseFileInfo.FileInfoWrapper info) {
//        // Compute substring that we are being asked to read
//        final String s = contents.substring((int) offset,
//                (int) Math.max(offset, Math.min(contents.length() - offset, offset + size)));
//        buffer.put(s.getBytes());
//        return s.getBytes().length;
//    }
//
//    @Override
//    public int readdir(final String path, final DirectoryFiller filler) {
//        filler.add(filename);
//        return 0;
//    }
//}


public class DNFSFuseIntegration extends FuseFilesystemAdapterFull {
    private Logger LOGGER = Logger.getLogger(this.getClass());


    public DNFSFuseIntegration() {
        super();
        this.log(true);
        this.LOGGER.setLevel(Level.DEBUG);
    }

//    For testing
    private final String fileName = "/test_file.txt";
    final String contents = "Hello World!\n";

    @Override
    public int getattr(final String path, final StructStat.StatWrapper stat) {
        if (path.equals(File.separator)) { // Root directory
            stat.setMode(TypeMode.NodeType.DIRECTORY);
            return 0;
        }
        if (path.equals(this.fileName)) { // hello.txt
            stat.setMode(TypeMode.NodeType.FILE).size(contents.length());
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }
    //    @Override
    public int read(String path, final ByteBuffer buffer, final long size, long offset, StructFuseFileInfo.FileInfoWrapper info) {
        LOGGER.debug("read was called");
        // Compute substring that we are being asked to read
        final String s = contents.substring((int) offset,
                (int) Math.max(offset, Math.min(contents.length() - offset, offset + size)));
        buffer.put(s.getBytes());
        return s.getBytes().length;
    }

    @Override
    public int readdir(final String path, final DirectoryFiller filler) {
        filler.add(this.fileName);
        return 0;
    }

//    @Override
//    public int access(String path, int access) {
//        LOGGER.debug("acces was called.");
//        return 0;
//    }
//
//    @Override
//    public void afterUnmount(File mountPoint) {
//        LOGGER.debug("after Unmount was called");
//
//    }
//
//    @Override
//    public void beforeMount(File mountPoint) {
//        LOGGER.debug("before mount  was called");
//    }
//
//    @Override
//    public int bmap(String path, StructFuseFileInfo.FileInfoWrapper info) {
//        LOGGER.debug("bmap was called");
//        return 0;
//    }

//    @Override
//    public int chmod(String path, TypeMode.ModeWrapper mode) {
//        LOGGER.debug("chmod was called");
//        return 0;
//    }
//
//    @Override
//    public int chown(String path, long uid, long gid) {
//        LOGGER.debug("chown was called");
//        return 0;
//    }
//
//    @Override
//    public int create(String path, TypeMode.ModeWrapper mode, StructFuseFileInfo.FileInfoWrapper info) {
//        LOGGER.debug("create was called");
//        return 0;
//    }
//
//    @Override
//    public void destroy() {
//        LOGGER.debug("destory was called");
//
//    }

//    @Override
//    public int fgetattr(String path, StructStat.StatWrapper stat, StructFuseFileInfo.FileInfoWrapper info) {
//        LOGGER.debug("fgetattr was called");
//        return 0;
//    }

//    @Override
//    public int flush(String path, StructFuseFileInfo.FileInfoWrapper info) {
//        LOGGER.debug("flush was called");
//        return 0;
//    }

//    @Override
//    public int fsync(String path, int datasync, StructFuseFileInfo.FileInfoWrapper info) {
//        LOGGER.debug("fsync was called");
//        return 0;
//    }

//    @Override
//    public int fsyncdir(String path, int datasync, StructFuseFileInfo.FileInfoWrapper info) {
//        LOGGER.debug("fsyncdir was called");
//        return 0;
//    }

//    @Override
//    public int ftruncate(String path, long offset, StructFuseFileInfo.FileInfoWrapper info) {
//        LOGGER.debug("ftruncate was called");
//        return 0;
//    }


//    @Override
//    protected String getName() {
//        LOGGER.debug("getName was called");
//        return "Fuse HD";
//    }
//
//    @Override
//    protected String[] getOptions() {
//        LOGGER.debug("getOptions was called");
//        return new String[0];
//    }
//
//    @Override
//    public int getxattr(String path, String xattr, XattrFiller filler, long size, long position) {
//        LOGGER.debug("getaxattr was called");
//        return 0;
//    }
//
//    @Override
//    public void init() {
//        LOGGER.debug("init was called");
//
//    }
//
//    @Override
//    public int link(String path, String target) {
//        LOGGER.debug("link was called");
//        return 0;
//    }
//
//    @Override
//    public int listxattr(String path, XattrListFiller filler) {
//        LOGGER.debug("listaxattr was called");
//        return 0;
//    }
//
//    @Override
//    public int lock(String path, StructFuseFileInfo.FileInfoWrapper info, FlockCommand command, StructFlock.FlockWrapper flock) {
//        LOGGER.debug("lock was called");
//        return 0;
//    }
//
//    @Override
//    public int mkdir(String path, TypeMode.ModeWrapper mode) {
//        LOGGER.debug("mkdir was called");
//        return 0;
//    }
//
//    @Override
//    public int mknod(String path, TypeMode.ModeWrapper mode, long dev) {
//        LOGGER.debug("mknod was called");
//        return 0;
//    }

//    @Override
//    public int open(String path, StructFuseFileInfo.FileInfoWrapper info) {
//        LOGGER.debug("open was called");
//        return 0;
//    }

//    @Override
//    public int opendir(String path, StructFuseFileInfo.FileInfoWrapper info) {
//        LOGGER.debug("opedir was called");
//        return 0;
//    }


//
//    @Override
//    public int readlink(String path, ByteBuffer buffer, long size) {
//        LOGGER.debug("readlink was called");
//        return 0;
//    }
//
//    @Override
//    public int release(String path, StructFuseFileInfo.FileInfoWrapper info) {
//        return 0;
//    }
//
//    @Override
//    public int releasedir(String path, StructFuseFileInfo.FileInfoWrapper info) {
//        LOGGER.debug("relasedir was called");
//        return 0;
//    }
//
//    @Override
//    public int removexattr(String path, String xattr) {
//        LOGGER.debug("removeattr was called");
//        return 0;
//    }
//
//    @Override
//    public int rename(String path, String newName) {
//        LOGGER.debug("rename was called");
//        return 0;
//    }
//
//    @Override
//    public int rmdir(String path) {
//        LOGGER.debug("rmdir was called");
//        return 0;
//    }
//
//    @Override
//    public int setxattr(String path, String xattr, ByteBuffer value, long size, int flags, int position) {
//        LOGGER.debug("setxattr was called");
//        return 0;
//    }
//
//    @Override
//    public int statfs(String path, StructStatvfs.StatvfsWrapper wrapper) {
//        LOGGER.debug("statfs was called");
//        return 0;
//    }
//
//    @Override
//    public int symlink(String path, String target) {
//        LOGGER.debug("symlink was called");
//        return 0;
//    }
//
//    @Override
//    public int truncate(String path, long offset) {
//        LOGGER.debug("truncate was called");
//        return 0;
//    }
//
//    @Override
//    public int unlink(String path) {
//        LOGGER.debug("unlink was called");
//        return 0;
//    }
//
//    @Override
//    public int utimens(String path, StructTimeBuffer.TimeBufferWrapper wrapper) {
//        LOGGER.debug("utimens was called");
//        return 0;
//    }
//
//    @Override
//    public int write(String path, ByteBuffer buf, long bufSize, long writeOffset, StructFuseFileInfo.FileInfoWrapper info) {
//        LOGGER.debug("write was called");
//        return 0;
//    }
}
