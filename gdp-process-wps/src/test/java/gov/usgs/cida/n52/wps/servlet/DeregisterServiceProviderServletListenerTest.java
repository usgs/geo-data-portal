/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.n52.wps.servlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.spi.ServiceRegistry;
import org.junit.Test;

/**
 *
 * @author tkunicki
 */
public class DeregisterServiceProviderServletListenerTest {
    
    public DeregisterServiceProviderServletListenerTest() {
    }

    @Test
    public void testSomeMethod() {
        IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
        
        DeregisterServiceProviderServletListener l = new DeregisterServiceProviderServletListener();
        
        List<ImageInputStreamSpi> iBefore = convertToList(ServiceRegistry.lookupProviders(ImageInputStreamSpi.class, getClass().getClassLoader()));
        List<ImageOutputStreamSpi> oBefore = convertToList(ServiceRegistry.lookupProviders(ImageOutputStreamSpi.class, getClass().getClassLoader()));
        
        List<ImageInputStreamSpi> iiBefore = convertToList(iioRegistry.getServiceProviders(ImageInputStreamSpi.class, false));
        List<ImageOutputStreamSpi> ooBefore = convertToList(iioRegistry.getServiceProviders(ImageOutputStreamSpi.class, false));
        
        l.contextDestroyed(null);
       
        List<ImageInputStreamSpi> iiAfter = convertToList(iioRegistry.getServiceProviders(ImageInputStreamSpi.class, false));
        List<ImageOutputStreamSpi> ooAfter = convertToList(iioRegistry.getServiceProviders(ImageOutputStreamSpi.class, false));
        
        List<ImageInputStreamSpi> iAfter = convertToList(ServiceRegistry.lookupProviders(ImageInputStreamSpi.class, getClass().getClassLoader()));
        List<ImageOutputStreamSpi> oAfter = convertToList(ServiceRegistry.lookupProviders(ImageOutputStreamSpi.class, getClass().getClassLoader()));
        
        
        
        
    }
    
    public <T> List<T> convertToList(Iterator<T> iterator) {
        ArrayList<T> list = new ArrayList<T>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        list.trimToSize();
        return list;
    }
}
