package gov.usgs.cida.gdp.utilities.bean;

import java.util.Date;
import static org.junit.Assert.*;


import org.junit.Test;

public class ErrorTest {
    @Test
    public void testInitializeWithInteger() {
        Error errBean = new Error(Integer.valueOf(0));
        assertNotNull(errBean.getErrorMessage());
        assertEquals(errBean.getErrorMessage(), "No Command Has Been Provided - To list all available commands, use command=commandlist");
    }

    @Test
    public void testInitializeWithIntegerAndStacktrace() {
        RuntimeException ex = new ArrayStoreException();

        Error errBean = new Error(Integer.valueOf(0), ex);
        assertNotNull(errBean.getErrorMessage());
        assertEquals(errBean.getErrorMessage(), "No Command Has Been Provided - To list all available commands, use command=commandlist");
        assertNotNull(errBean.getException());
        assertEquals(errBean.getException().getClass(), ArrayStoreException.class);
    }

    @Test
    public void testInitializeWithString() {
        Error errBean = new Error("test");
        assertNotNull(errBean.getErrorMessage());
        assertEquals(errBean.getErrorMessage(), "test");
    }

    @Test
    public void testSetGetMessage() {
        Error errBean = new Error();
        errBean.setErrorMessage("test");
        assertNotNull(errBean.getErrorMessage());
        assertEquals(errBean.getErrorMessage(), "test");
    }

    @Test
    public void testSetGetException() {
        RuntimeException ex = new ArrayStoreException();
        Error errBean = new Error();
        errBean.setException(ex);
        assertNotNull(errBean.getException());
        assertEquals(errBean.getException().getClass(), ArrayStoreException.class);
    }

    @Test
    public void testSetGetErrorCreated() {
        Error errBean = new Error();
        Date date = new Date();
        long longDate = date.getTime();
        errBean.setErrorCreated(date);
        assertNotNull(errBean.getErrorCreated());
        assertEquals(errBean.getErrorCreated().getTime(), longDate);
    }

        @Test
    public void testSetGetErrorClassParam() {
        Error errBean = new Error();
        errBean.setErrorClass("test");
        assertNotNull(errBean.getErrorClass());
        assertEquals(errBean.getErrorClass(), "test");
    }

        @Test
        public void testToString() {
            Error errBean = new Error();
        errBean.setErrorClass("test");
        assertNotNull(errBean.toString());
        assertNotSame("", errBean.toString());
        }

                @Test
        public void testSetGetErrorNumber() {
            Error errBean = new Error();
        errBean.setErrorNumber(Integer.MIN_VALUE);
        assertNotNull(errBean.getErrorNumber());
        assertTrue(errBean.getErrorNumber() == Integer.MIN_VALUE);
        }
}
