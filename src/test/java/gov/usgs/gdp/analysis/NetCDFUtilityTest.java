package gov.usgs.gdp.analysis;

import gov.usgs.cida.gdp.dataaccess.helper.NetCDFUtility;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.junit.Test;
import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;
import thredds.catalog.ServiceType;
import static org.junit.Assert.*;

public class NetCDFUtilityTest {
    @Test
    public void testGetDatasetHandles() throws Exception {
        URI catalogURI = NetCDFUtilityTest.class.getResource("multi_catalog_all.xml").toURI();
        InvCatalogFactory factory = new InvCatalogFactory("default", true);
        InvCatalog catalog = factory.readXML(catalogURI);

        StringBuilder errorBuilder = new StringBuilder();
        if (!catalog.check(errorBuilder)) {
            throw new IOException(errorBuilder.toString());
        }

        List<InvAccess> handlesFromCatalog = NetCDFUtility.getDatasetHandles(catalog, ServiceType.OPENDAP);
        assertEquals(handlesFromCatalog.size(), 2);
        assertEquals(handlesFromCatalog.get(0).getDataset().getName(), "NCEP WaveWatch III:  Atlantic (4 min grid)");
        assertEquals(handlesFromCatalog.get(1).getDataset().getName(), "NCEP WaveWatch III:  Atlantic (10 min grid)");

        List<InvDataset> topLevelDatasets = catalog.getDatasets();
        assertEquals(topLevelDatasets.size(), 1);
        assertEquals(topLevelDatasets.get(0).getName(), "NOAA-WW3");
        List<InvAccess> handlesFromTopLevelDataset =
                NetCDFUtility.getDatasetHandles(topLevelDatasets.get(0), ServiceType.OPENDAP);

        // Assert that the handles obtained from the catalog and from the top-level dataset are the same.
        assertEquals(handlesFromCatalog, handlesFromTopLevelDataset);
    }
}
