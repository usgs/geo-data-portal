/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.geotools.util;

import java.lang.reflect.Field;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author tkunicki
 */
public class WeakCollectionCleanerCleanerTest {

    // this value is actually hardcoded as a literal in the WeakCollectionCleaner impl.
    public final static String ThreadName_WeakCollectionCleaner = "WeakCollectionCleaner";

    public WeakCollectionCleanerCleanerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testWeakCollectionCleanerSignature() {
        /*  Verify that the WeakCollectionCleaner fields we
         *  rely upon and are available via reflection.
         */
        findWeakCollectionDEFAULTField();
        findWeakColletionCleanerRequestQueueField();
    }

    private void findWeakCollectionDEFAULTField() {
        findFieldByName(WeakCollectionCleanerCleaner.FIELD_DEFAULT);
    }

    private void findWeakColletionCleanerRequestQueueField() {
        findFieldByName(WeakCollectionCleanerCleaner.FIELD_referenceQueue);
    }

    private void findFieldByName(String name) {
        boolean exception = true;
        Field field = null;
        try {
            field = WeakCollectionCleanerCleaner.fieldForName(
                    WeakCollectionCleanerCleaner.CLASS_WeakCollectionCleaner,
                    name);
            exception = false;
        } catch (Exception e) {
            exception = true;
        }
        assertFalse(exception);
        assertNotNull(field);
    }

    /**
     * Test of contextDestroyed method, of class WeakCollectionCleanerCleaner.
     */
    @Test
    public void testContextDestroyed() {
        boolean exception = true;
        try {
            Class.forName(WeakCollectionCleanerCleaner.CLASS_WeakCollectionCleaner);
            exception = false;
        } catch (ClassNotFoundException ex) {
            exception = true;
        }
        assertFalse("Exception loading " + WeakCollectionCleanerCleaner.CLASS_WeakCollectionCleaner, exception);

        boolean active = isThreadActive(ThreadName_WeakCollectionCleaner);
        assertTrue("WeakCollectionCleaner thread is not active?  Implementation change?", active);
        WeakCollectionCleanerCleaner instance = new WeakCollectionCleanerCleaner();
        instance.contextDestroyed(null);
        active = isThreadActive(ThreadName_WeakCollectionCleaner);
        assertFalse("WeakCollectionCleaner thread is still active", active);
    }

    private boolean isThreadActive(String threadName) {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        while (threadGroup.getParent() != null) {
            threadGroup = threadGroup.getParent();
        }
        Thread[] threads = new Thread[128];
        int threadCount = threadGroup.enumerate(threads, true);
        // more a a failure in the test, not the code being tested...
        assertTrue("Didn't allocate enough storage for threads during test", threadCount < threads.length);
        boolean found = false;
        for (int threadIndex = 0; threadIndex < threadCount && !found; ++threadIndex) {
            found = (threads[threadIndex].getName().equals(threadName) && threads[threadIndex].isAlive());
        }
        return found;
    }
}