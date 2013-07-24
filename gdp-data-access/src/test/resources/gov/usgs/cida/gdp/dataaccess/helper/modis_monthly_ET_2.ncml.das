Attributes {
    lon {
        String units "degrees_east";
        String standard_name "longitude";
    }
    lat {
        String units "degrees_north";
        String standard_name "latitude";
    }
    crs {
        String grid_mapping_name "latitude_longitude";
        Float64 longitude_of_prime_meridian 0.0;
        Float64 semi_major_axis 6378137.0;
        Float64 inverse_flattening 298.257223563;
    }
    time {
        String units "calendar months since 2000-01-01 00:00";
        String _CoordinateAxisType "Time";
    }
    et {
        String grid_mapping "crs";
        String coordinates "time lat lon";
        Int16 missing_value -9999;
        String units "mm";
    }
    NC_GLOBAL {
        String Conventions "CF-1.5";
        String Metadata_Conventions "Unidata Dataset Discovery v1.0";
        String title "Conterminous U.S. actual evapotranspiration data";
        String summary "Actual ET (ETa) is produced using the operational Simplified Surface Energy Balance (SSEBop) model (Senay and others, 2013) for the period 2000 to present. The SSEBop setup is based on the Simplified Surface Energy Balance (SSEB) approach (Senay and others, 2007, 2011) with unique parameterization for operational applications. It combines ET fractions generated from remotely sensed MODIS thermal imagery, acquired every 8 days, with reference ET using a thermal index approach. The unique feature of the SSEBop parameterization is that it uses pre-defined, seasonally dynamic, boundary conditions that are unique to each pixel for the ìhot/dryî and ìcold/wetî reference points.";
        String keywords "";
        String keywords_vocabulary "GCMD Science Keywords";
        String id "/pub/dcp/archive/bcca/...";
        String naming_authority "gdo-dcp.ucllnl.org";
        String cdm_data_type "Grid";
        String date_created "February, 2013";
        String date_modified "February, 2013";
        String creator_name "Stefanie Bohms";
        String creator_url "http://eros.usgs.gov/";
        String creator_email "sbohms@usgs.gov";
        String publisher_name "David Blodgett";
        String publisher_url "http://cida.usgs.gov/";
        String publisher_email "dblodgett@usgs.gov";
        String institution "U.S. Geological Survey";
        String date_issued "February, 2013";
        String project "WaterSMART National Water Census";
        String processing_level "Refer to documentation.";
        String acknowledgement "Senay, G.B., M. Budde, J.P. Verdin, and A.M. Melesse, 2007. A coupled remote sensing and simplified surface energy balance approach to estimate actual evapotranspiration from irrigated fields. Special issue: Remote sensing of natural resources and the environment. SENSORS, 1, 979-1000.  Senay, G.B., M. Budde, J.P. Verdin, 2011. Enhancing the Simplified Surface Energy Balance (SSEB) approach for estimating landscape ET: Validation with the METRIC model. Agricultural Water Management, 98: 606-618. Senay, G.B., S. Bohms, R. Singh, P.A. Gowda,  N.M. Velpuri, H. Alemu and J.P. Verdin, 2013. Operational evapotranspiration mapping using remote sensing and weather datasets: A new parameterization for the SSEB approach. Journal of American Water Resources Research. Accepted.";
        String contributors "Senay, G.B., S. Bohms, R. Singh, P.A. Gowda,  N.M. Velpuri, H. Alemu, J.P. Verdin,  M. Budde and A.M. Melesse.";
        String geospatial_lat_min "24";
        String geospatial_lat_max "49.5";
        String geospatial_lon_min "-126";
        String geospatial_lon_max "-66.5";
        String time_coverage_start "2007-06-16T12:00";
        String time_coverage_end "2011-06-16T12:00";
        String time_coverage_resolution "P1Y";
        String license "Freely available";
    }
}
