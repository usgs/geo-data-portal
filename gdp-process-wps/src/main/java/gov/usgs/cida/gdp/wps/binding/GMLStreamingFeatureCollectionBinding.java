package gov.usgs.cida.gdp.wps.binding;

import gov.usgs.cida.gdp.wps.parser.GMLStreamingFeatureCollection;
import org.n52.wps.io.data.IComplexData;

/**
 *
 * @author tkunicki
 */
public class GMLStreamingFeatureCollectionBinding implements IComplexData {

    private final GMLStreamingFeatureCollection featureCollection;
    
    public GMLStreamingFeatureCollectionBinding(GMLStreamingFeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
    }
    
    @Override
    public void dispose() {
        featureCollection.dispose();
    }

    @Override
    public GMLStreamingFeatureCollection getPayload() {
        return featureCollection;
    }

    @Override
    public Class getSupportedClass() {
        return GMLStreamingFeatureCollection.class;
    }
    
}
