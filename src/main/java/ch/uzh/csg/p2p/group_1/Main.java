package ch.uzh.csg.p2p.group_1;
/**
 * Created by janmeier on 23.03.15.
 */
import net.kencochrane.raven.Dsn;
import net.kencochrane.raven.Raven;
import net.kencochrane.raven.RavenFactory;
import net.kencochrane.raven.event.Event;
import net.kencochrane.raven.event.EventBuilder;
import net.kencochrane.raven.event.interfaces.ExceptionInterface;
//import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.PropertyConfigurator;

public class Main {
//    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        String rawDsn = "http://ba5caee44caa44bc912363ea6345b21c:9ee047d12df84eea8d6fa800c4fd3dd2@sentry.jan-meier.ch/2";
        Raven raven = RavenFactory.ravenInstance(new Dsn(rawDsn));

        // record a simple message
        EventBuilder eventBuilder = new EventBuilder()
                .setMessage("Hello from Raven!")
                .setLevel(Event.Level.ERROR)
                .setLogger(Main.class.getName())
                .addSentryInterface(new ExceptionInterface(new Exception("hello")));

        raven.runBuilderHelpers(eventBuilder); // Optional
        raven.sendEvent(eventBuilder.build());
        System.out.print("done");
    }
//        try{
//            PropertyConfigurator.configure("src/log4j.properties");
//        }
//        catch (Exception e){
//            System.out.print(e);
//        }
//        logger.info("This is a test");
//        logger.error("Exception caught");
////        MDC.put("extra_key", "extra_value");
//        // NDC extras are sent under 'log4J-NDC'
////        NDC.push("Extra_details");
////        System.out.print("Hello World");

}
