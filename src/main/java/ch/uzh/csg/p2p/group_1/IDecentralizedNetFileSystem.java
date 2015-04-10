package ch.uzh.csg.p2p.group_1;

/**
 * Created by janmeier on 28.03.15.
 */
public interface IDecentralizedNetFileSystem {

    public void setUp();
    public void loadConfig(String configFile);
    public void start();
    public void pause();
    public void resume();
    public void shutDown();
    
}