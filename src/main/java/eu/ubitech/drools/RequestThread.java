package eu.ubitech.drools;

import static eu.ubitech.drools.Util.getObjectFromAgenda;
import eu.ubitech.drools.om.Clazz;
import eu.ubitech.drools.om.CombinedAdvice;
import eu.ubitech.drools.om.InstanceOfClazz;
import eu.ubitech.drools.om.KnowledgeTriple;
import eu.ubitech.drools.om.ObjectProperty;
import java.io.BufferedWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.rule.FactHandle;

public class RequestThread implements Runnable {

    private static final Logger logger = Logger.getLogger(RequestThread.class.getName());

    private KieSession kiesession;
    private int index;
    private BufferedWriter bwriter;

    public RequestThread(int index, KieSession kiesession, BufferedWriter bwriter) {
        this.index = index;
        this.kiesession = kiesession;
        this.bwriter = bwriter;
    }//EoM

    @Override
    public void run() {
        int random = new Random().nextInt(10);
//        int random = 1;
        //logger.info("Initiating "+index);
        try {
            long startTime = System.currentTimeMillis();
            String requestid = "" + index;
            String subjectname = "requestor" + random;
            String objectname = "resource" + random;
            String actionname = "action" + random;
            String ipaddress = "ip" + random;

            ObjectProperty op1
                    = (ObjectProperty) kiesession.getObject(
                            (FactHandle) getObjectFromAgenda(kiesession, ObjectProperty.class, "requestHasIP"));
            ObjectProperty op2
                    = (ObjectProperty) kiesession.getObject(
                            (FactHandle) getObjectFromAgenda(kiesession, ObjectProperty.class, "requestHasSubject"));
            ObjectProperty op3
                    = (ObjectProperty) kiesession.getObject(
                            (FactHandle) getObjectFromAgenda(kiesession, ObjectProperty.class, "requestHasObject"));
            ObjectProperty op4
                    = (ObjectProperty) kiesession.getObject(
                            (FactHandle) getObjectFromAgenda(kiesession, ObjectProperty.class, "requestHasAction"));

            // create knowledge
            // 1 create request instance
            FactHandle reqclassf = (FactHandle) getObjectFromAgenda(kiesession, Clazz.class, "Request");
            Clazz reqclass = (Clazz) kiesession.getObject(reqclassf);
            InstanceOfClazz ioreq = new InstanceOfClazz(requestid, reqclass);

            // 2 handle subject
            FactHandle f1 = (FactHandle) getObjectFromAgenda(kiesession, Clazz.class, "Subject");
            Clazz cf1 = (Clazz) kiesession.getObject(f1);
            FactHandle f2
                    = (FactHandle) getObjectFromAgenda(kiesession, InstanceOfClazz.class, subjectname);
            InstanceOfClazz iocsubject;
            if (f2 != null) {
                iocsubject = (InstanceOfClazz) kiesession.getObject(f2);
            } else {
                iocsubject = new InstanceOfClazz(subjectname, cf1);
                kiesession.insert(iocsubject);
            }

            // Any
            // 3 handle object
            FactHandle f3 = (FactHandle) getObjectFromAgenda(kiesession, Clazz.class, "Object");
            Clazz cf3 = (Clazz) kiesession.getObject(f3);
            FactHandle f4 = (FactHandle) getObjectFromAgenda(kiesession, InstanceOfClazz.class, objectname);
            InstanceOfClazz iocobject;
            if (f4 != null) {
                iocobject = (InstanceOfClazz) kiesession.getObject(f4);
            } else {
                iocobject = new InstanceOfClazz(objectname, cf3);
                kiesession.insert(iocobject);
            }
            // 4 handle action
            FactHandle f5 = (FactHandle) getObjectFromAgenda(kiesession, Clazz.class, "Action");
            Clazz cf5 = (Clazz) kiesession.getObject(f5);
            FactHandle f6 = (FactHandle) getObjectFromAgenda(kiesession, InstanceOfClazz.class, actionname);
            InstanceOfClazz iocaction;
            if (f6 != null) {
                iocaction = (InstanceOfClazz) kiesession.getObject(f6);
            } else {
                iocaction = new InstanceOfClazz(actionname, cf5);
                kiesession.insert(iocaction);
            }

            // 5 handle IP
            FactHandle f7 = (FactHandle) getObjectFromAgenda(kiesession, Clazz.class, "IP");
            Clazz cf7 = (Clazz) kiesession.getObject(f7);
            FactHandle f8 = (FactHandle) getObjectFromAgenda(kiesession, InstanceOfClazz.class, ipaddress);
            InstanceOfClazz iocip;
            if (f8 != null) {
                iocip = (InstanceOfClazz) kiesession.getObject(f8);
            } else {
                iocip = new InstanceOfClazz(ipaddress, cf7);
                kiesession.insert(iocip);
            }

            // Handlers
            KnowledgeTriple t1 = new KnowledgeTriple(ioreq, op2, iocsubject);   // request  requestHasSubject Subject
            KnowledgeTriple t2 = new KnowledgeTriple(ioreq, op3, iocobject);    // request  requestHasObject  Object
            KnowledgeTriple t3 = new KnowledgeTriple(ioreq, op4, iocaction);    // request  requestHasAction  Object
            KnowledgeTriple t4 = new KnowledgeTriple(ioreq, op1, iocip);        // request  requestHasIP  IP

            // insert additional knowledge
            kiesession.insert(ioreq);
            kiesession.insert(t1);
            kiesession.insert(t2);
            kiesession.insert(t3);
            kiesession.insert(t4);

            //Fire
            kiesession.fireAllRules();

            //Get Response
            CombinedAdvice retadv = null;
            String ret = "";
            for (FactHandle handle: kiesession.getFactHandles( new ObjectFilter() {
                        public boolean accept(Object object) {
                            if (CombinedAdvice.class.equals(object.getClass()) && ((CombinedAdvice) object).getRequestid().equalsIgnoreCase(requestid)) {
                                return true;
                            }
                            return false;
                        }
                    })) {
                retadv = (CombinedAdvice) kiesession.getObject(handle);
                //logger.info("------->CombinedAdvice for "+requestid+":" + retadv.getAdvice());
                if (retadv != null) {
                    ret = retadv.getAdvice();
                }
                break;
            }

            long timeMilli = (new Date()).getTime();
            long elapseddTime = System.currentTimeMillis() - startTime;
            //wite to file
            bwriter.write((timeMilli + " Thread: " + index + " Time:" + elapseddTime + " Result: "+ret+"\n"));

        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }//EoM run

}//EoC
