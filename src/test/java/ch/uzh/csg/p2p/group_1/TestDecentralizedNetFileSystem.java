
/**
 * Created by janmeier on 28.03.15.
 */

package ch.uzh.csg.p2p.group_1;

import junit.framework.TestCase;
import org.junit.*;
import org.slf4j.Logger;


public class TestDecentralizedNetFileSystem {

    private DecentralizedNetFileSystem decentralizedNetFileSystem;
    @Before
    public void setUp() throws Exception {
        this.decentralizedNetFileSystem = new DecentralizedNetFileSystem();

    }

    @After
    public void tearDown() throws Exception {
    }

    @Ignore
    @Test
    public void testBasicFunctionality() {

//        this.decentralizedNetFileSystem.setUp("./conf/settings.xml", null);
        this.decentralizedNetFileSystem.start();
        this.decentralizedNetFileSystem.pause();
        this.decentralizedNetFileSystem.resume();
        this.decentralizedNetFileSystem.shutDown();
    }
}
