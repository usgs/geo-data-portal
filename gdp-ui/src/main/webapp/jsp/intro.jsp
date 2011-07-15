<div id="intro">
    <h1>Geo Data Portal</h1>
    <div>
        The Geo Data Portal (GDP) is a tool built to support environmental process
        modeling by providing more efficient data access and GIS manipulation. The GDP is in development and
        will gain additional functionality as time goes on. The current focus is on
        data access for models using irregularly shaped modeling units, most
        commonly representing watersheds. These units can be uploaded in a 
        <a href="http://en.wikipedia.org/wiki/Shapefile" target="_new">Shapefile</a> or automatically delineated with the EPA's WATERS web service. For this initial release, data accessed through the GDP must be served via a Unidata THematic Realtime
        Environmental Distributed Data Service
        <a href="http://www.unidata.ucar.edu/projects/THREDDS/" target="_new">(THREDDS) Data Server (TDS)</a> .
        <br /><br />
        OpenDAP / NetCDF-CF is a broadly supported data format and services arch.... are supported by the TDS architecture and numerous data
        sets are currently available via this server technology. The two broad data
        types supported by the geo-data portal are grid and station. Both data
        types can have a time dimension. Station data from within the spatial
        domain of the modeling units in question can be extracted to common
        delimited text with the option to return one or more parameters measured at
        each station.
        <br /><br />
        The GDP returns spatially weighted statistics for gridded time series data.
        Gridded data from the bounding box of the modeling units in question and in
        the time range specified is sent to the GDP server. Weighted statistics of
        the grid are calculated for each polygon in the modeling unit shape file
        treating the grid cells as polygon geometry. This allows accurate
        calculation of weighted statistics regardless of grid resolution and
        polygon size. A variety of statistics are available via common delimited
        text.
    </div>
</div>
