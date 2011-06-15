/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.transactionbbox;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.wfs.*;
import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;


public class BboxTransactionListener implements TransactionListener {
    
    Catalog catalog;

    public BboxTransactionListener(Catalog catalog) {
        this.catalog = catalog;
    }
    
    /**
     * Check/alter feature collections and filters before a change hits the
     * datastores
     */
    public void dataStoreChange(TransactionEvent event) throws WFSException {

        if (event.getType() == TransactionEventType.PRE_INSERT) {

            LayerInfo layerInfo = catalog.getLayerByName(new NameImpl(event.getLayerName()));
            ResourceInfo resourceInfo = layerInfo.getResource();

            ReferencedEnvelope bounds = event.getAffectedFeatures().getBounds();

            // TODO: Convert native bounds to lat lon. Right now they're the same
            // thing, but if we ever draw polygons in a projected CRS, we'll
            // need to project these bounds as well.
            resourceInfo.setLatLonBoundingBox(bounds);
            resourceInfo.setNativeBoundingBox(bounds);

            catalog.save(resourceInfo);
        }
    }
}
