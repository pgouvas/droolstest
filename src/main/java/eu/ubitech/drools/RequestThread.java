package eu.ubitech.drools;

import eu.ubitech.drools.om.Clazz;
import java.io.BufferedWriter;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

public class RequestThread implements Runnable {

    private static final Logger logger = Logger.getLogger(RequestThread.class.getName());

    private KieSession kiesession;
    private int index;
    private BufferedWriter bwriter;

    public RequestThread(int index, KieSession kiession, BufferedWriter bwriter) {
        this.index = index;
        this.kiesession = kiession;
        this.bwriter = bwriter;
    }//EoM

    @Override
    public void run() {
        //logger.info("Initiating "+index);
        try {
            long startTime = System.currentTimeMillis();
            Clazz area = new Clazz("Area"+index, null);        
            kiesession.insert(area);
            Collection<FactHandle> factHandles = kiesession.getFactHandles();
            int size = factHandles.size();
            if (!factHandles.isEmpty())  kiesession.delete(factHandles.iterator().next());
            kiesession.fireAllRules();
            long timeMilli = (new Date()).getTime();
            long estimatedTime = System.currentTimeMillis() - startTime;        
            
            bwriter.write((timeMilli+" Thread: " + index + " Fact size:"+size+" Time:" + estimatedTime + "\n"));

        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

}//EoC
