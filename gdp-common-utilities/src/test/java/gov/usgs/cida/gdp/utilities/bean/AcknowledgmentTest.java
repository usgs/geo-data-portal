package gov.usgs.cida.gdp.utilities.bean;

import static org.junit.Assert.*;

import org.junit.Test;

import com.vividsolutions.jts.util.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.LoggerFactory;

public class AcknowledgmentTest {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(AcknowledgmentTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    @Test
    public void testSetStatusToOne() {
        Acknowledgement ackBean = new Acknowledgement(1);
        assertEquals("OK", ackBean.getStatus());
        ackBean.setStatus("FAIL");
        assertEquals("FAIL", ackBean.getStatus());

    }

    @Test
    public void testSetStatusToTwo() {
        Acknowledgement ackBean = new Acknowledgement(2);
        assertEquals("FAIL", ackBean.getStatus());
    }

    @Test
    public void testInvalidStatus() {
        try {
            Acknowledgement ackBean = new Acknowledgement(3);
        } catch (IllegalArgumentException e) {
            return;
        }

        Assert.shouldNeverReachHere();
    }
}
