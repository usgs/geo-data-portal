package gov.usgs.geotools.util;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Application Lifecycle Listener implementation class
 * WeakCollectionCleanerCleaner
 * 
 */
public class WeakCollectionCleanerCleaner implements ServletContextListener {

    public final static String CLASS_WeakCollectionCleaner = "org.geotools.util.WeakCollectionCleaner";
    public final static String FIELD_DEFAULT = "DEFAULT";
    public final static String FIELD_referenceQueue = "referenceQueue";

    private final static String LOG_MSG_WARNING = "Unable to stop WeakCollectionCleaner thread gracefully";
    private final static String LOG_MSG_SEVERE = "Unable to stop WeakCollectionCleaner thread, probable PermGen leak...";
    private final static String LOG_MSG_INFO_GRACEFUL = "Successfully stopped WeakCollectionCleaner, gracefully";
    private final static String LOG_MSG_INFO_FORCE = "Successfully stopped WeakCollectionCleaner with brute force";

    /**
     * Default constructor.
     */
    public WeakCollectionCleanerCleaner() {
        // DO NOT REMOVE: default constructor must be explicitly declared for
        // container to instantiate.
    }

    /**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // nothing to do...
    }

    /**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            // NOTE: If the thread we are trying to kill hasn't started
            // (class never loaded and initialized) we are are going to start
            // it with reflection calls, shucks... JVM Specification 12.4.1
            // No way that I know of to test whether a class has been
            // initialized by JVM so that we could punt...

            // Pull out class fields we need to manipulate.
            Field defaultField = fieldForName(CLASS_WeakCollectionCleaner,
                    FIELD_DEFAULT);
            Field referenceQueueField = fieldForName(
                    CLASS_WeakCollectionCleaner, FIELD_referenceQueue);

            // get handle to singleton instance
            Object cleanerObject = defaultField.get(null);

            if (cleanerObject != null && cleanerObject instanceof Thread) {

                Thread cleanerThread = (Thread) cleanerObject;

                // Make sure we're only cleaning up an instance associated with
                // the this app's classload.
                // 1) If one has GeoTools installed in the container libs
                // directory, stopping the cleaner thread would be *bad*
                // for subsequent app deploys...
                // 2) If the cleaner isn't associated with this classloader it's
                // not going to address the PermGen leak so why mess with it...
                if (cleanerThread.getClass().getClassLoader() ==
                        Thread.currentThread().getContextClassLoader()) {

                    // heavy exception catching so we can attempt a fallback
                    // method
                    try {
                        // First try a gracefull stop (will result in log
                        // output)
                        // the cleaner thread tests for (referenceQueue != null)
                        // as a continuation condition. This is good...
                        referenceQueueField.set(cleanerObject, null);

                        // interrupt clean thread as it sleeps for 15s between
                        // sweeps
                        cleanerThread.interrupt();
                        cleanerThread.join(500);
                    } catch (InterruptedException ex) {
                        // swallow, we are exiting and need to clean up...
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.WARNING, LOG_MSG_WARNING, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(getClass().getName()).log( Level.WARNING, LOG_MSG_WARNING, ex);
                    }
                    if (referenceQueueField.get(cleanerObject) != null || cleanerThread.isAlive()) {

                        // This is bad form, but what else are we to do?
                        cleanerThread.stop();

                        try {
                            cleanerThread.join(500);
                        } catch (InterruptedException ex) {
                            // swallow, we are exiting and need to clean up...
                        }

                        if (cleanerThread.isAlive()) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, LOG_MSG_SEVERE);
                        } else {
                            Logger.getLogger(getClass().getName()).log(Level.INFO, LOG_MSG_INFO_FORCE);
                        }

                    } else {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, LOG_MSG_INFO_GRACEFUL);
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    LOG_MSG_SEVERE, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    LOG_MSG_SEVERE, ex);
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    LOG_MSG_SEVERE, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    LOG_MSG_SEVERE, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    LOG_MSG_SEVERE, ex);
        }
    }

    static Field fieldForName(String className, String fieldName)
            throws ClassNotFoundException, NoSuchFieldException {
        Class<?> clazz = Class.forName(className);
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
