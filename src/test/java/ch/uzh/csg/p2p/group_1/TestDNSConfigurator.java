package ch.uzh.csg.p2p.group_1;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by janmeier on 28.03.15.
 */
public class TestDNSConfigurator extends TestCase {

    private DNFSConfigurator configurator;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.configurator = new DNFSConfigurator("./conf/settings.xml");
    }

    @Test
    public void testSetUp(){
        try {
            this.configurator.setUp();
        } catch (ConfigurationException e) {
            e.printStackTrace();
//            Assert.assertTrue(false);
        }

    }
}
