package ch.uzh.csg.p2p.group_1;

import org.apache.commons.cli.CommandLine;

/**
 * Created by janmeier on 28.03.15.
 */
public interface IDecentralizedNetFileSystem {

    public void setUp(String settingsPath, CommandLine cmd);
    public void loadConfig(String configFile);
    public void start();
    public void pause();
    public void resume();
    public void shutDown();
    
}