package ch.uzh.csg.p2p.group_1;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by janmeier on 16.04.15.
 */
public class TestDNFSPath {
    private String pathAsString;
    private List<String> pathAsList;
    private DNFSPath path;

    @Before
    public void setUp(){
        this.pathAsString = "/dir_1/dir_2/file.txt";
        this.pathAsList = new ArrayList<String>();
        this.pathAsList.add("dir_1");
        this.pathAsList.add("dir_2");
        this.pathAsList.add("file.txt");
        this.path = new DNFSPath(this.pathAsList);
    }

    @Test
    public void testCreationFromString(){
        assertEquals(path.toString(), this.pathAsString);
        assertEquals(path.getComponents(), this.pathAsList);
    }

    @Test
    public void testCreationFromList(){
        DNFSPath path = new DNFSPath(this.pathAsList);
        assertEquals(path.toString(), this.pathAsString);
        assertEquals(path.getComponents(), this.pathAsList);
    }

    @Test
    public void testGetComponent(){
        assertEquals(this.path.getComponent(0),"dir_1");
        assertEquals(this.path.getComponent(-1), "file.txt");
    }

    @Test
    public void testSubPath(){
        DNFSPath subPath = path.getSubPath(1);
        assertEquals(subPath.length(), 2);
        assertEquals(subPath.getComponent(0), "dir_2");
        assertEquals(subPath.getComponent(1), "file.txt");

        subPath = path.getSubPath(0, 1);
        assertEquals(subPath.length(), 1);
        assertEquals(this.path.getComponent(0),"dir_1");
    }
}
