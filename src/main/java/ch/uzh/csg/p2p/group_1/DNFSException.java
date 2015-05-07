package ch.uzh.csg.p2p.group_1;

/**
 * Created by janmeier on 10.04.15.
 */
public class DNFSException extends Throwable {

    static public class DNFSNotFileException extends DNFSException {

    }

    static public class DNFSNotFolderException extends DNFSException {

    }

    static public class DNFSPathNotFound extends DNFSException {

    }

}

