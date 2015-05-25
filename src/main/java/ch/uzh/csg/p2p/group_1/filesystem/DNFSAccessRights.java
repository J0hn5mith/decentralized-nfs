/**
 * Created by janmeier on 25.05.15.
 */
package ch.uzh.csg.p2p.group_1.filesystem;

import net.fusejna.types.TypeGid;
import net.fusejna.types.TypeUid;

import org.apache.log4j.Logger;

public class DNFSAccessRights {
    private long mode;
    private TypeUid uid;
    private TypeGid gid;

    final private static Logger LOGGER = Logger.getLogger(DNFSAccessRights.class.getName());

    public DNFSAccessRights(long mode, TypeUid uid, TypeGid gid) {
        this.mode = mode;
        this.uid = uid;
        this.gid = gid;
    }

    public boolean isAllowedToWrite(TypeUid uid, TypeGid gid) {

        if (uid.equals(this.uid) && this.ownerWritable()) {
            return true;
        } else if (gid.equals(this.gid) && this.groupWritable()) {
            return true;
        } else {
            return this.otherWritable();
        }


    }

    public boolean isAllowedToRead(TypeUid uid, TypeGid gid) {
        if (uid.equals(this.uid) && this.ownerReadable()) {
            return true;
        } else if (gid.equals(this.gid) && this.groupReadable()) {
            return true;
        } else {
            return this.otherReadable();
        }
    }

    public boolean isAllowedToWriteAndRead(TypeUid uid, TypeGid gid) {
        if (uid.equals(this.uid) && this.ownerWritable() && this.ownerReadable()) {
            return true;
        } else if (gid.equals(this.gid) && this.groupWritable() && this.groupReadable()) {
            return true;
        } else {
            return this.otherReadable() && this.ownerWritable();
        }
    }


    private boolean ownerWritable() {
        return (this.mode & AccessRightByteValues.OWNER_WRITABLE) > 0;
    }

    private boolean ownerReadable() {
        return (this.mode & AccessRightByteValues.OWNER_READABLE) > 0;
    }

    private boolean ownerExecutable() {
        return (this.mode & AccessRightByteValues.OWNER_EXECUTABLE) > 0;
    }

    private boolean groupWritable() {
        return (this.mode & AccessRightByteValues.GROUP_WRITABLE) > 0;
    }

    private boolean groupReadable() {
        return (this.mode & AccessRightByteValues.GROUP_READABLE) > 0;
    }

    private boolean groupExecutable() {
        return (this.mode & AccessRightByteValues.GROUP_EXECUTABLE) > 0;
    }

    private boolean otherWritable() {
        return (this.mode & AccessRightByteValues.OTHER_WRITABLE) > 0;
    }

    private boolean otherReadable() {
        return (this.mode & AccessRightByteValues.OTHER_READABLE) > 0;
    }

    private boolean otherExecutable() {
        return (this.mode & AccessRightByteValues.OTHER_EXECUTABLE) > 0;
    }

    private static class AccessRightByteValues {
        static long OWNER_READABLE = 256L;
        static long OWNER_WRITABLE = 128L;
        static long OWNER_EXECUTABLE = 64L;

        static long GROUP_READABLE = 32L;
        static long GROUP_WRITABLE = 16L;
        static long GROUP_EXECUTABLE = 8L;

        static long OTHER_READABLE = 4L;
        static long OTHER_WRITABLE = 2L;
        static long OTHER_EXECUTABLE = 1L;
    }
}
