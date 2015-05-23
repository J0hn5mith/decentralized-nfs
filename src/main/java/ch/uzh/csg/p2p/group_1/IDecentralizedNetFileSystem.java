package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.utlis.DNFSSettings;

/**
 * Created by janmeier on 28.03.15.
 */
public interface IDecentralizedNetFileSystem {

    public void setUp(DNFSSettings settings);
    public void start();
    public void shutDown();
    
}