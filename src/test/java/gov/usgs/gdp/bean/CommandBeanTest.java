/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.gdp.bean;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class CommandBeanTest {

    public CommandBeanTest() {
    }

    /**
     * Test of getCommandList method, of class CommandBean.
     */
    @Test
    public void testGetCommandList() {
        List<CommandBean> instance = CommandBean.getCommandList();
        assertTrue(instance.size() > 0);
    }

    /**
     * Test of toXml method, of class CommandBean.
     */
    @Test
    public void testToXml() {        
        List<CommandBean> instance = CommandBean.getCommandList();
        assertNotSame("", instance.get(0).toXml());
    }

}