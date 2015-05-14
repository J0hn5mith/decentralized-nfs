package ch.uzh.csg.p2p.group_1;

/**
 * Created by janmeier on 10.04.15.
 */
public class DNFSException extends Throwable {

    private static final long serialVersionUID = 2740073990296063507L;


    static public class DNFSNotFileException extends DNFSException {
        private static final long serialVersionUID = -3918444955252984445L;
    }

    
    static public class NoSuchFileOrFolder extends DNFSException{
        private static final long serialVersionUID = 1229382105660628170L;
    }


    static public class DNFSNotFolderException extends NoSuchFileOrFolder  {
        private static final long serialVersionUID = -5132810907932619010L;
    }

    
    static public class DNFSPathNotFound extends DNFSException {
        private static final long serialVersionUID = -7866969856858819069L;
    }

    static public class DNFSBlockStorageException extends DNFSException {

    }

    static public class DNFSNetworkNoConnection extends DNFSException {
        public DNFSNetworkNoConnection() {
            super("Your network is not connected.");
        }
    }

    static public class DNFSNetworkSetupException extends DNFSException {
        private static final long serialVersionUID = 482847664421090530L;

        public DNFSNetworkSetupException(String message) {
            super(message);
        }
    }
    
    
    static public class DNFSNetworkPutException extends DNFSException {
        private static final long serialVersionUID = -8121432314741122319L;

        public DNFSNetworkPutException(String message) {
            super(message);
        }
    }
    
    
    static public class DNFSNetworkGetException extends DNFSException {
        private static final long serialVersionUID = -9205989666839957529L;

        public DNFSNetworkGetException(String message) {
            super(message);
        }
    }
    
    
    static public class DNFSNetworkDeleteException extends DNFSException {
        private static final long serialVersionUID = -1935907951987843485L;

        public DNFSNetworkDeleteException(String message) {
            super(message);
        }
    }
    
    
    public DNFSException() {
        super();
    }
    
    
    public DNFSException(String message) {
        super(message);
    }

}

