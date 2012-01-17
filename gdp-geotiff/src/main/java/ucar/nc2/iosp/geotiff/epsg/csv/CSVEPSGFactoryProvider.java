package ucar.nc2.iosp.geotiff.epsg.csv;

import ucar.nc2.iosp.geotiff.epsg.EPSGFactory;
import ucar.nc2.iosp.geotiff.epsg.EPSGFactoryProvider;

/**
 *
 * @author tkunicki
 */
public class CSVEPSGFactoryProvider implements EPSGFactoryProvider {

    @Override
    public EPSGFactory getEPSGFactory() {
        return CSVEPSGFactory.getInstance();
    }
    
}
