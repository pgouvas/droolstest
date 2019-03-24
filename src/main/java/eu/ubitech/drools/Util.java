package eu.ubitech.drools;

import eu.ubitech.drools.om.Clazz;
import java.util.Collection;
import java.util.logging.Logger;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.rule.FactHandle;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class Util {

    private static final Logger logger = Logger.getLogger(Util.class.getName());

    public static boolean isClassSubclassOfClass(Clazz test, Clazz parent) {
        if (test.equals(parent)) {
            return true;
        }
        if (!test.equals(parent) && test.getParent() == null) {
            return false;
        } else {
            return isClassSubclassOfClass(test.getParent(), parent);
        }
    }//EoM

    public static Object getObjectFromAgenda(KieSession ksession, Class cl, String objectname) {
        Object obj = null;

        Collection<FactHandle> factHandles
                = ksession.getFactHandles(
                        new ObjectFilter() {
                    public boolean accept(Object object) {

                        if (null != objectname
                                && !objectname.isEmpty()
                                && object.getClass().equals(cl)
                                && ReflectionUtil.getNameOfObject(object).equalsIgnoreCase(objectname)) {
                            return true;
                        }
                        return false;
                    }
                });
        //logger.info("Query for " + objectname + "(" + cl.getName() + ") returned: " + factHandles.size());
        if (!factHandles.isEmpty()) {
            return factHandles.iterator().next();
        }
        return obj;
    } // EoM      

}//EoM
