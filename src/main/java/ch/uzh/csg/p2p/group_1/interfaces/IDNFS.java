package ch.uzh.csg.p2p.group_1.interfaces;

import ch.uzh.csg.p2p.group_1.DNFSSettings;

/**
 * Created by janmeier on 28.03.15.
 */
public interface IDNFS {

    public void setUp(DNFSSettings settings);
    public void start();
    public void shutDown();
    
}