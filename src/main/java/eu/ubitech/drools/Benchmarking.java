package eu.ubitech.drools;

import eu.ubitech.drools.om.Clazz;
import eu.ubitech.drools.om.InstanceOfClazz;
import eu.ubitech.drools.om.ObjectProperty;
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

    private final static int NUM_OF_THREADS = 1000;
    private static BufferedWriter bwriter;

    public static void main(String[] args) {
        try {
            System.gc();
            Runtime runtime = Runtime.getRuntime();
            bwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("test", false)));
            List<Thread> plist = new ArrayList<>();

            // Load the knowledge base
            KieServices kieservices = KieServices.Factory.get();
            KieContainer kContainer = kieservices.getKieClasspathContainer();
            KieSession kiesession = kContainer.newKieSession("ksession-rules");
            
            //Initialize XACML Skeleton
            //C,Request,null
            //C,Subject,null
            //C,Object,null
            //C,Action,null
            //C,IPAddress,null
            //OP,requestHasSubject, Request, Subject,false,null
            //OP,requestHasObject, Request, Object,false,null
            //OP,requestHasAction, Request, Action,false,null
            //OP,requestHasIPAddress, Request, IPAddress,false,null 
            //IoC,requestor1,Subject
            //IoC,requestor2,Subject
            //IoC,requestor3,Subject
            //IoC,requestor4,Subject
            //IoC,requestor5,Subject
            //IoC,resource1,Object
            //IoC,resource2,Object
            //IoC,resource3,Object
            //IoC,resource4,Object
            //IoC,resource5,Object
            //IoC,action1,Action
            //IoC,action2,Action
            //IoC,action3,Action
            //IoC,action4,Action
            //IoC,action5,Action
            Clazz request = new Clazz("Request",null);
            Clazz subject = new Clazz("Subject",null);
            Clazz object = new Clazz("Object",null);
            Clazz action = new Clazz("Action",null);
            Clazz ip = new Clazz("IP",null);
            ObjectProperty requestHasSubject = new ObjectProperty("requestHasSubject", request, subject,false,null);
            ObjectProperty requestHasObject = new ObjectProperty("requestHasObject", request, object,false,null);
            ObjectProperty requestHasAction = new ObjectProperty("requestHasAction", request, action,false,null);
            ObjectProperty requestHasIP = new ObjectProperty("requestHasIP", request, ip,false,null);
            InstanceOfClazz requestor1 = new InstanceOfClazz("requestor1", subject);
            InstanceOfClazz requestor2 = new InstanceOfClazz("requestor2", subject);
            InstanceOfClazz requestor3 = new InstanceOfClazz("requestor3", subject);
            InstanceOfClazz requestor4 = new InstanceOfClazz("requestor4", subject);
            InstanceOfClazz requestor5 = new InstanceOfClazz("requestor5", subject);
            InstanceOfClazz resource1 = new InstanceOfClazz("resource1", object);
            InstanceOfClazz resource2 = new InstanceOfClazz("resource2", object);
            InstanceOfClazz resource3 = new InstanceOfClazz("resource3", object);
            InstanceOfClazz resource4 = new InstanceOfClazz("resource4", object);
            InstanceOfClazz resource5 = new InstanceOfClazz("resource5", object);
            InstanceOfClazz action1 = new InstanceOfClazz("action1", action);
            InstanceOfClazz action2 = new InstanceOfClazz("action2", action);
            InstanceOfClazz action3 = new InstanceOfClazz("action3", action);
            InstanceOfClazz action4 = new InstanceOfClazz("action4", action);
            InstanceOfClazz action5 = new InstanceOfClazz("action5", action);
            InstanceOfClazz ip1 = new InstanceOfClazz("ip1", ip);
            InstanceOfClazz ip2 = new InstanceOfClazz("ip2", ip);
            InstanceOfClazz ip3 = new InstanceOfClazz("ip3", ip);
            InstanceOfClazz ip4 = new InstanceOfClazz("ip4", ip);
            InstanceOfClazz ip5 = new InstanceOfClazz("ip5", ip);
            //Add to knowledge session
            kiesession.insert(request);
            kiesession.insert(subject);
            kiesession.insert(object);
            kiesession.insert(action);
            kiesession.insert(ip);
            kiesession.insert(requestHasSubject);
            kiesession.insert(requestHasObject);
            kiesession.insert(requestHasAction);
            kiesession.insert(requestHasIP);
            kiesession.insert(requestor1);
            kiesession.insert(requestor2);
            kiesession.insert(requestor3);
            kiesession.insert(requestor4);
            kiesession.insert(requestor5);
            kiesession.insert(resource1);
            kiesession.insert(resource2);
            kiesession.insert(resource3);
            kiesession.insert(resource4);
            kiesession.insert(resource5);
            kiesession.insert(action1);
            kiesession.insert(action2);
            kiesession.insert(action3);
            kiesession.insert(action4);
            kiesession.insert(action5);            
            kiesession.insert(ip1);            
            kiesession.insert(ip2);            
            kiesession.insert(ip3);            
            kiesession.insert(ip4);            
            kiesession.insert(ip5);            
            
            for (int i = 0; i < NUM_OF_THREADS; i++) {
                RequestThread rthread = new RequestThread(i, kiesession, bwriter);
                Thread thread = new Thread(rthread);
                thread.setName("Thread " + i);
                plist.add(thread);
            }//for
            //Reset time memory
            long startTime = System.currentTimeMillis();
            System.gc();
            logger.info("Starting All Threads");
            
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
