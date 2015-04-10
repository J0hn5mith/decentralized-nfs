/**
 * Created by janmeier on 28.03.15.
 */
package ch.uzh.csg.p2p.group_1;

import org.apache.commons.configuration.*;
import org.apache.log4j.Logger;

public class DNFSConfigurator {
	
    private final Logger LOGGER = Logger.getLogger(this.getClass());

    private String filePath;
    private XMLConfiguration config;

    /**
     * 
     * @param config_file
     */
    public DNFSConfigurator(String config_file){
        this.filePath = config_file;
        this.config = new XMLConfiguration();
    }

    /**
     * 
     * @throws ConfigurationException
     */
    public void setUp() throws ConfigurationException {
        this.config.setFileName(this.filePath);
        this.config.setValidating(false); // We don't have a dtd schema by now
        this.config.load();
    }

    /**
     * 
     * @param dnfs
     */
    private void apply(DecentralizedNetFileSystem dnfs){
        LOGGER.info("Apply settings to dnfs.");
    }
    
    /**
     * 
     * @return
     */
    public Configuration getConfig(){
        return this.config;

    }

}
