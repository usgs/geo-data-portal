<div id="area-of-interest">
    <div class="step-header hidden">Specify Area of Interest</div>
    <div class="step-content hidden">
        <div id="service-tooltips" class="hidden">
            <!-- put tooltips up here to prevent them from cluttering
                 up the structure below -->
            <div id="waters-tooltip-text">
                This services uses the EPA Waters web service to retrieve a
                watershed polygon from the NHD+ dataset above the point
                selected on the map. Give your waters analysis shape a unique
                name and select a point near the outlet of the watershed you
                want to define. The Waters web service will be queried and the
                data will be loaded as a web feature service for analysis and a
                web map service for visualization.  The name you give will
                appear in the feature types list as waters:name. If you click a
                new point with the waters shape name set to the name of an
                existing waters feature type, the existing one will be
                overwritten. Note that the NHD+ hydrography is not accurate in
                all places so use this service cautiously.
            </div>
            <div id="upload-tooltip-text">
                Select a zipped file containing (at a minimum) the .shp, .shx,
                .prj, and .dbf components of your shapefile. There is more
                information about uploading shapefiles at the
                <a href="https://my.usgs.gov/confluence/display/GeoDataPortal/Geo+Data+Portal+User+Interface+FAQ">
                    Geo Data Portal FAQ.
                </a>
            </div>
            <div id="draw-feature-tooltip-text">
                Select to draw your area of interest. Click on the map to specify
                each vertex of a polygon. On the last vertex, double click to
                finish the polygon. Give it a name and click the submit button
                to save the geometry and select it for processing.
            </div>
        </div>
        <div class="centered">
        <table id="services-table">
            <tr>
                <td id="services-cell">
                    <span id="upload-button-container" style="padding-right: 15px;">
                        <a class="tooltip">
                            <img src="images/question-mark.png" alt="informational question mark" />
                        </a>
                        <span id="uploadButton" class="button aoi-services-button toggle">Upload Shapefile</span>
                    </span>
                    <span id="waters-button-container" style="padding-right: 15px;">
                        or
                        <a class="tooltip">
                            <img src="images/question-mark.png" alt="informational question mark" />
                        </a>
                        <span id="watersButton" class="button aoi-services-button toggle">EPA WATERS Service</span>
                    </span>
                    <span id="draw-feature-button-container">
                        or
                        <a class="tooltip">
                            <img src="images/question-mark.png" alt="informational question mark" />
                        </a>
                        <span id="drawFeatureButton" class="button aoi-services-button toggle">Draw a Polygon</span>
                    </span>
                </td>
            </tr>
            <tr>
                <td>
                    <div id="services-input-cells">
                        <span id="featuretype-name-input-cell">
                            <label>Polygon Name: </label>
                            <input id="featuretypeName" type="text" />
                        </span>
                        <span id="submit-input-cell">
                            <input type="button" id="submitDrawFeatureButton" class="button" />
                        </span>
                        <span id="clear-input-cell">
                            <input type="button" class="button" id="clearDrawFeatureButton" />
                        </span>
                        <span id="upload-input-cell">
                            <span id="fileInputButton" ></span>
                        </span>
                    </div>
                </td>
            </tr>
        </table>

        <table id="aoi-table">
            <tr>
                <td id="available-featuretypes-cell">
                    <div id="available-featuretypes">
                        <span>Available Areas of Interest:</span>
                        <a class="tooltip" title="Select an area of interest
                            from the list of uploaded shapefiles, drawn polygons,
                            and samples. After selecting, you can download the area
                            of interest with the link below the list.">
                            <img src="images/question-mark.png" alt="informational question mark"/>
                        </a>
                        <span id="clear-aoi-button"><img src="images/blue-undo-arrow.png" class="clear-button" title="Click to clear" /></span>
                        <br />
                        <select id="aoi-selectbox" size="6">
                            <%-- Automatically Generated --%>
                        </select>
                    </div>
                </td>
                <td id="available-attributes-cell">
                    <div id="available-attributes" class="hidden">
                        <span>Available Attributes:</span>
                        <a class="tooltip" title="Select the attribute to use
                            to group and label your output. These attributes are
                            synonymous with the column headers from a shapefile's
                            attribute table.">
                            <img src="images/question-mark.png" alt="informational question mark"/>
                        </a><br />
                        <select id="available-attributes-selectbox" size="6">
                            <%-- Automatically Generated --%>
                        </select>
                    </div>
                </td>
                <td id="available-attribute-values-cell">
                    <div id="available-attribute-values" class="hidden">
                        <span>Available Attribute Values:</span>
                        <a class="tooltip" title="Select attribute values of
                            interest. Only polygons with those attribute values
                            will be used for analysis. If more than one polygon in
                            your feature set have the same attribute value, they
                            will be grouped in the analysis output. If the
                            algorithm you use returns raw data, the attribute
                            values are not used, but data will only be returned for
                            the area covered by polygons with those attribute
                            values.">
                            <img src="images/question-mark.png" alt="informational question mark"/>
                        </a><br />
                        <select id="available-attribute-values-selectbox" size="6" multiple="multiple">
                            <%-- Automatically Generated --%>
                        </select>
                    </div>
                </td>
            </tr>
        </table>
        </div>
    </div>
</div>
