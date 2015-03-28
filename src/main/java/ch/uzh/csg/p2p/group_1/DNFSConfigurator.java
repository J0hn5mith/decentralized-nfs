/**
 * Created by janmeier on 28.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import org.apache.commons.configuration.*;
import org.apache.log4j.Logger;

import java.net.URL;

public class DNFSConfigurator {
    private final Logger LOGGER = Logger.getLogger(this.getClass());

    private String file_path;
    private XMLConfiguration config;
    private DecentralizedNetFileSystem dnfs;

    public DNFSConfigurator(String config_file, DecentralizedNetFileSystem dnfs){
        this.file_path = config_file;
        this.config = new XMLConfiguration();
        this.dnfs = dnfs;
    }

    public void setUp() throws ConfigurationException {
        this.config.setFileName(this.file_path);
        this.config.setValidating(false); // We don't have a dtd schema by now
        this.config.load();
    }

    private void apply(){
        LOGGER.info("Apply settings to dnfs.");

    }

}
