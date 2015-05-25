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
    }

    public boolean isAllowedToWrite(TypeUid uid, TypeGid gid){
        LOGGER.warn(String.format("isAllowedToWrite is only a dummy implementation."));
        return true;
    }

    public boolean isAllowedToRead(TypeUid uid, TypeGid gid){
        LOGGER.warn(String.format("isAllowedToRead is only a dummy implementation."));
        return true;
    }

    public boolean isAllowedToWriteAndRead(TypeUid uid, TypeGid gid){
        LOGGER.warn(String.format("isAllowedToRead is only a dummy implementation."));
        return true;
    }
}
