
/**
 * Created by janmeier on 28.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import net.fusejna.*;
import net.fusejna.types.TypeMode;

import java.io.File;
import java.nio.ByteBuffer;

public class DNFSFuseIntegration extends FuseFilesystem {
    @Override
    public int access(String path, int access) {
        return 0;
    }

    @Override
    public void afterUnmount(File mountPoint) {

    }

    @Override
    public void beforeMount(File mountPoint) {

    }

    @Override
    public int bmap(String path, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int chmod(String path, TypeMode.ModeWrapper mode) {
        return 0;
    }

    @Override
    public int chown(String path, long uid, long gid) {
        return 0;
    }

    @Override
    public int create(String path, TypeMode.ModeWrapper mode, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public void destroy() {

    }

    @Override
    public int fgetattr(String path, StructStat.StatWrapper stat, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int flush(String path, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int fsync(String path, int datasync, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int fsyncdir(String path, int datasync, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int ftruncate(String path, long offset, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int getattr(String path, StructStat.StatWrapper stat) {
        return 0;
    }

    @Override
    protected String getName() {
        return null;
    }

    @Override
    protected String[] getOptions() {
        return new String[0];
    }

    @Override
    public int getxattr(String path, String xattr, XattrFiller filler, long size, long position) {
        return 0;
    }

    @Override
    public void init() {

    }

    @Override
    public int link(String path, String target) {
        return 0;
    }

    @Override
    public int listxattr(String path, XattrListFiller filler) {
        return 0;
    }

    @Override
    public int lock(String path, StructFuseFileInfo.FileInfoWrapper info, FlockCommand command, StructFlock.FlockWrapper flock) {
        return 0;
    }

    @Override
    public int mkdir(String path, TypeMode.ModeWrapper mode) {
        return 0;
    }

    @Override
    public int mknod(String path, TypeMode.ModeWrapper mode, long dev) {
        return 0;
    }

    @Override
    public int open(String path, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int opendir(String path, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int read(String path, ByteBuffer buffer, long size, long offset, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int readdir(String path, DirectoryFiller filler) {
        return 0;
    }

    @Override
    public int readlink(String path, ByteBuffer buffer, long size) {
        return 0;
    }

    @Override
    public int release(String path, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int releasedir(String path, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }

    @Override
    public int removexattr(String path, String xattr) {
        return 0;
    }

    @Override
    public int rename(String path, String newName) {
        return 0;
    }

    @Override
    public int rmdir(String path) {
        return 0;
    }

    @Override
    public int setxattr(String path, String xattr, ByteBuffer value, long size, int flags, int position) {
        return 0;
    }

    @Override
    public int statfs(String path, StructStatvfs.StatvfsWrapper wrapper) {
        return 0;
    }

    @Override
    public int symlink(String path, String target) {
        return 0;
    }

    @Override
    public int truncate(String path, long offset) {
        return 0;
    }

    @Override
    public int unlink(String path) {
        return 0;
    }

    @Override
    public int utimens(String path, StructTimeBuffer.TimeBufferWrapper wrapper) {
        return 0;
    }

    @Override
    public int write(String path, ByteBuffer buf, long bufSize, long writeOffset, StructFuseFileInfo.FileInfoWrapper info) {
        return 0;
    }
}
