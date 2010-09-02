/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.utilities.bean;

import java.util.Date;
import javax.management.RuntimeErrorException;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author isuftin
 */
public class ErrorEnumTest {

    public ErrorEnumTest() {
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
    public void createErrorEnumWithInteger() {
        assertEquals(ErrorEnum.ERR_NO_COMMAND.getErrorMessage(), "No Command Has Been Provided - To list all available commands, use command=commandlist");
    }

    @Test
    public void errorEnumSetGetErrMessage() {
        ErrorEnum result = ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND;
        result.setErrorMessage("test");
        assertEquals(result.getErrorMessage(), "test");
    }

    @Test
    public void errorEnumSetGetException() {
        ErrorEnum result = ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND;
        java.lang.Error err = new java.lang.Error("test");
        result.setException(new RuntimeErrorException(err, "test"));
        assertEquals(result.getException().getMessage(), "test");
    }

    @Test
    public void errorEnumSetGetErrorCreated() {
        Date now = new Date();
        long nowDate = now.getTime();
        ErrorEnum result = ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND;

        result.setErrorCreated(now);
        assertEquals(result.getErrorCreated().getTime(), nowDate);
    }

    @Test
    public void errorEnumSetGetErrorClass() {
        ErrorEnum result = ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND;

        result.setErrorClass("test");
        assertEquals(result.getErrorClass(), "test");
    }

    @Test
    public void errorEnumToString() {
        ErrorEnum result = ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND;

        assertNotNull(result.toString());
        assertFalse(result.toString().isEmpty());
    }

    @Test
    public void errorEnumSetGetErrorNumber() {
        ErrorEnum result = ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND;

        result.setErrorNumber(1);
        assertTrue(result.getErrorNumber() == 1);
    }
}
