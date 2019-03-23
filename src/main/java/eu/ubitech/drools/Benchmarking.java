package eu.ubitech.drools;

import eu.ubitech.drools.om.Clazz;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

/**
 *
 * @author Panagiotis Gouvas
 */
public class Benchmarking {

    //https://docs.jboss.org/drools/release/7.19.0.Final/drools-docs/html_single/ 
    private static final Logger logger = Logger.getLogger(Benchmarking.class.getName());

    private final static int NUM_OF_THREADS = 100000;
    private static BufferedWriter bwriter;

    public static void main(String[] args) {
        try {
            Runtime runtime = Runtime.getRuntime();
            bwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("test", false)));
            List<Thread> plist = new ArrayList<>();

            // Load the knowledge base
            KieServices kieservices = KieServices.Factory.get();
            KieContainer kContainer = kieservices.getKieClasspathContainer();
            KieSession kiesession = kContainer.newKieSession("ksession-rules");
            
            for (int i = 0; i < NUM_OF_THREADS; i++) {
                RequestThread rthread = new RequestThread(i, kiesession, bwriter);
                Thread thread = new Thread(rthread);
                thread.setName("Thread " + i);
                plist.add(thread);
            }//for
            logger.info("Starting All Threads");
            long startTime = System.currentTimeMillis();
            System.gc();
            long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
            //Start
            plist.parallelStream().forEach(s -> s.start());
            //Check that all are 
            Optional<Boolean> havealaterminated = plist.stream().map(t -> (t.getState() == State.TERMINATED)).reduce(Boolean::logicalAnd);
            while (!havealaterminated.get()) {
                Thread.sleep(100);
                havealaterminated = plist.stream().map(t -> (t.getState() == State.TERMINATED)).reduce(Boolean::logicalAnd);
            }
            
            //Take Measurement
            long estimatedTime = System.currentTimeMillis() - startTime;
            //System.gc();
            long memoryAfter = (runtime.totalMemory() - runtime.freeMemory());
            long memcons = (memoryAfter-memoryBefore)/1024;
            logger.info("All Threads terminated");
            logger.info("Total Requests:"+NUM_OF_THREADS+" Time:"+estimatedTime + " Memory (kB): "+memcons);

            bwriter.flush();
            bwriter.close();

//            logger.info("Facts: "+kiesession.getFactCount()+" "+memcons);
        } //EoMain
        catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

    }//EoMain

}//EoM
