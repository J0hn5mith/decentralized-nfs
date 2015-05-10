/**
 * Created by janmeier on 10.05.15.
 */
package ch.uzh.csg.p2p.group_1.test_bed;

import ch.uzh.csg.p2p.group_1.DecentralizedNetFileSystem;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ch.uzh.csg.p2p.group_1.utlis.DNFSCommandLineOptionsFactory;
import org.apache.commons.cli.*;

public class DNFSTestBed {

    final static int NUM_PEERS = 10;
    final static int BASE_PORT = 10000;
    final static int PORT_INTERVAL = 10;

    static int currentPort = BASE_PORT;

    static List<DecentralizedNetFileSystem> dnfsInstances;


    public static void main(String[] args) throws InterruptedException, ParseException {

        for (int i = 0; i < NUM_PEERS; i++) {
            createDnfsInstance();
        }

        return;
    }

    static private void createDnfsInstance(){
        CommandLineParser parser = new GnuParser();
        String[] args = new String[5];
        args[0] = "-p";
        args[1] = getNextPort().toString();

        args[2] = "-m";
        args[3] = getNextMountPoint();
        args[4] = "-d";
        CommandLine cmd ;
        try {
            cmd = parser.parse(DNFSCommandLineOptionsFactory.getOptions(), args);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
            return;
        }

        DecentralizedNetFileSystem dnfs = new DecentralizedNetFileSystem();
        dnfs.setUp("./conf/settings.xml", cmd);
        dnfs.start();
        dnfsInstances.add(dnfs);
    }

    static private Integer getNextPort(){
        int port = currentPort;
        currentPort += PORT_INTERVAL;
        return port;
    }

    static private String getNextMountPoint(){
        try {
            return createTempDirectory().toString();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    public static File createTempDirectory()
            throws IOException
    {
        final File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if(!(temp.delete()))
        {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if(!(temp.mkdir()))
        {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }


}
