/**
 * Created by janmeier on 10.05.15.
 */
package ch.uzh.csg.p2p.group_1.test_bed;

import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import ch.uzh.csg.p2p.group_1.DWARFS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ch.uzh.csg.p2p.group_1.CommandLineOptionsFactory;
import ch.uzh.csg.p2p.group_1.Settings;

import org.apache.commons.cli.*;

public class DNFSTestBed {

    final static int NUM_PEERS = 1;
    final static int BASE_PORT = 10000;
    final static int PORT_INTERVAL = 10;

    final static int NUM_PEERS_SHUTDOWN = 5;
    final static int PEER_SHUTDOWN_INTERVAL = 10;
    final static int PEER_SHUTDOWN_TIME_OFFSET = 30;


    static int currentPort = BASE_PORT;

    static List<DNFSRunnable> dnfsInstances;


    public static void main(String[] args) throws InterruptedException, ParseException {
        dnfsInstances = new ArrayList<DNFSRunnable>();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (DNFSRunnable runnable : dnfsInstances) {
                    runnable.stop();
                }
                System.out.println("Shutdown hook ran!");
            }
        });

        for (int i = 0; i < NUM_PEERS; i++) {
            createDnfsInstance();
        }

        for (DNFSRunnable dnfsInstance : dnfsInstances) {
            dnfsInstance.start();
        }


//        if (NUM_PEERS_SHUTDOWN > 0) {
//            shutDownProcess();
//        }


        return;
    }

    static private void shutDownProcess() {
        try {
            Thread.sleep(PEER_SHUTDOWN_TIME_OFFSET / 1000);
            Random random = new Random();
            for (int i = 0; i < Math.min(NUM_PEERS_SHUTDOWN, NUM_PEERS); i++) {
                DNFSRunnable instance = dnfsInstances.get(random.nextInt(dnfsInstances.size()));
                instance.stop();
                Thread.sleep(PEER_SHUTDOWN_INTERVAL / 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    static private void createDnfsInstance() {
        CommandLineParser parser = new GnuParser();
        String[] args = new String[6];
        args[0] = "-p";
        args[1] = getNextPort().toString();
        args[2] = "-m";
        args[3] = getNextMountPoint();
        args[4] = "-a";
        args[5] = "127.0.0.1:1337";

        CommandLine cmd;
        try {
            cmd = parser.parse(CommandLineOptionsFactory.getOptions(), args);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
            return;
        }

        Settings settings = null;
        try {
            settings = new Settings("./conf/settings.xml", cmd);
        } catch (DNFSException.DNFSSettingsException e) {
            System.err.println("Could not set up settings.");
            e.printStackTrace();
            System.exit(-1);
        }

        DWARFS dnfs = new DWARFS();
        dnfs.setUp(settings);
        
        dnfsInstances.add(new DNFSRunnable(dnfs));
    }

    static private Integer getNextPort() {
        int port = currentPort;
        currentPort += PORT_INTERVAL;
        return port;
    }

    static private String getNextMountPoint() {
        try {
            return createTempDirectory().toString();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    public static File createTempDirectory()
            throws IOException {
        final File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }

    static class DNFSRunnable implements Runnable {
        private Thread t;
        private DWARFS dnfs;

        DNFSRunnable(DWARFS dnfs) {
            this.dnfs = dnfs;
        }

        public void run() {
            dnfs.start();
        }

        public void start() {
            t = new Thread(this);
            t.start();
        }

        public void stop() {
            this.t.interrupt();
        }

    }


}
