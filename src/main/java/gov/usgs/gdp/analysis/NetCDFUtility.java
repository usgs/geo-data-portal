package gov.usgs.gdp.analysis;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvDataset;
import thredds.catalog.ServiceType;

public class NetCDFUtility {
	
	public static List<InvAccess> getOpenDapResources(InvCatalog catalog) {
		if (catalog == null) return null;
		List<InvAccess> result = new LinkedList<InvAccess>();

        for (InvDataset dataset : catalog.getDatasets()) {
            result.addAll(NetCDFUtility.getOpendapResourcesAux(dataset));
        }
        return result;
	}

	public static Collection<InvAccess> getOpendapResourcesAux(
			InvDataset dataset) {
		if (dataset == null) return null;
		List<InvAccess> result = new LinkedList<InvAccess>();

        for (InvAccess resource : dataset.getAccess()) {
            if (resource.getService().getServiceType() == ServiceType.OPENDAP) {
            	result.add(resource);
            }
        }

        for (InvDataset nestedDataset : dataset.getDatasets()) {
        	result.addAll(getOpendapResourcesAux(nestedDataset));
        }

        return result;
	}

}
