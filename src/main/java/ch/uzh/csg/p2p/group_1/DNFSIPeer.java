package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.utlis.DNFSSettings;

/**
 * Created by janmeier on 16.04.15.
 */
public interface DNFSIPeer extends DNFSIBlockStorage, DNFSIiNodeStorage{


    public void setUp(DNFSSettings settings) throws DNFSException;
}
