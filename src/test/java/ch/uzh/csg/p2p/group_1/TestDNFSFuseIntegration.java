package ch.uzh.csg.p2p.group_1;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by janmeier on 16.04.15.
 */
public class TestDNFSFuseIntegration {

    private DecentralizedNetFileSystem dnfs;

    @Before
    public void setUp(){
        this.dnfs = new DecentralizedNetFileSystem();

        this.dnfs.loadConfig("./conf/settings.xml");
        this.dnfs.setUp();
        this.dnfs.start();

    }

    @Test
    public void testCreationFromString(){
        assertEquals(true, true);
    }


    @After
    public void cleanUp(){
        this.dnfs.shutDown();
    }
}
