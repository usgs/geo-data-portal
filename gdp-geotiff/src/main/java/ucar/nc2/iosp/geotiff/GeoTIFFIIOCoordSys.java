package ucar.nc2.iosp.geotiff;

import ucar.nc2.iosp.geotiff.epsg.ProjCS;
import ucar.nc2.iosp.geotiff.epsg.Datum;
import ucar.nc2.iosp.geotiff.cs.GeogCSHandler;
import ucar.nc2.iosp.geotiff.cs.ProjCSHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import ucar.nc2.iosp.geotiff.epsg.GeogCS;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import java.util.Arrays;
import javax.imageio.metadata.IIOMetadata;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.geotiff.cs.CSHandler;
import ucar.nc2.iosp.geotiff.epsg.csv.EPSG;

import static ucar.nc2.iosp.geotiff.GeoTiffIIOMetadataAdapter.*;

public class GeoTIFFIIOCoordSys {

    // harveted for ProJ documentation and libgeotiff geo_normalize.c
    enum ProjCoordTrans {
        CT_TransverseMercator(1, 9807),
        CT_TransvMercator_Modified_Alaska(2),
        CT_ObliqueMercator(3, 9812, 9815),
        CT_ObliqueMercator_Laborde(4, 9813),
        CT_ObliqueMercator_Rosenmund(5, 9814),
        CT_ObliqueMercator_Spherical(6),
        CT_Mercator(7, 9804, 9805, 9841, 1024),
        CT_LambertConfConic_2SP(8, 9802, 9803),
        CT_LambertConfConic_Helmert(9, 9801),
        CT_LambertAzimEqualArea(10, 9820),
        CT_AlbersEqualArea(11, 9822),
        CT_AzimuthalEquidistant(12),
        CT_EquidistantConic(13, 9823, 9824),
        CT_Stereographic(14),
        CT_PolarStereographic(15, 9810),
        CT_ObliqueStereographic(16, 9809),
        CT_Equirectangular(17),
        CT_CassiniSoldner(18, 9806),
        CT_Gnomonic(19),
        CT_MillerCylindrical(20),
        CT_Orthographic(21),
        CT_Polyconic(22),
        CT_Robinson(23),
        CT_Sinusoidal(24),
        CT_VanDerGrinten(25),
        CT_NewZealandMapGrid(26, 9811),
        CT_TransvMercator_SouthOriented(27, 9808),
        CT_CylindricalEqualArea(28),
        Unknown(-1);
        private int tiffCode;
        private int[] epsgCodes;
        ProjCoordTrans(int tiffCode, int... epsgCodes) {
            this.tiffCode = tiffCode;
            this.epsgCodes = epsgCodes == null ? new int[0] : epsgCodes;
        }

        public int getTiffCode() {
            return tiffCode;
        }
        public int[] getEpsgCodes() {
            return epsgCodes;
        }
        public static ProjCoordTrans findByGeoTIFFCode(int tiffCode) {
            if (tiffCode > values().length || tiffCode < 1) {
                return Unknown;
            } else {
                return values()[tiffCode - 1];
            }
        }
        public static ProjCoordTrans findByEPSGCode(int epsgCode) {
            ProjCoordTrans pct = Unknown;
            for (int pctIndex = 0; pctIndex < values().length && pct == Unknown; ++pctIndex) {
                int[] epsgCodes = values()[pctIndex].epsgCodes;
                for (int epsgIndex = 0; epsgIndex < epsgCodes.length && pct == Unknown; ++epsgIndex) {
                    if (epsgCode == epsgCodes[epsgIndex]) {
                        pct = values()[pctIndex];
                    }
                }
            }
            return pct;
        }
    }

    public final static int EPSGNatOriginLat = 8801;
    public final static int EPSGNatOriginLong = 8802;
    public final static int EPSGNatOriginScaleFactor = 8805;
    public final static int EPSGFalseEasting = 8806;
    public final static int EPSGFalseNorthing = 8807;
    public final static int EPSGProjCenterLat = 8811;
    public final static int EPSGProjCenterLong = 8812;
    public final static int EPSGAzimuth = 8813;
    public final static int EPSGAngleRectifiedToSkewedGrid = 8814;
    public final static int EPSGInitialLineScaleFactor = 8815;
    public final static int EPSGProjCenterEasting = 8816;
    public final static int EPSGProjCenterNorthing = 8817;
    public final static int EPSGPseudoStdParallelLat = 8818;
    public final static int EPSGPseudoStdParallelScaleFactor = 8819;
    public final static int EPSGFalseOriginLat = 8821;
    public final static int EPSGFalseOriginLong = 8822;
    public final static int EPSGStdParallel1Lat = 8823;
    public final static int EPSGStdParallel2Lat = 8824;
    public final static int EPSGFalseOriginEasting = 8826;
    public final static int EPSGFalseOriginNorthing = 8827;
    public final static int EPSGSphericalOriginLat = 8828;
    public final static int EPSGSphericalOriginLong = 8829;
    public final static int EPSGInitialLongitude = 8830;
    public final static int EPSGZoneWidth = 8831;

    int width;
    int height;

    double[] pixelScales;
    double[] tiePoints;
    double[] transformation;

    int modelType;
    int geographicType;
    int rasterType;

    CSHandler csHandler;

    public GeoTIFFIIOCoordSys(IIOMetadata metadata, int width, int height) {

        this.width = width;
        this.height = height;

        GeoTiffIIOMetadataAdapter metadataAdapter = new GeoTiffIIOMetadataAdapter(metadata);

        pixelScales = metadataAdapter.getModelPixelScales();
        tiePoints = metadataAdapter.getModelTiePoints();
        transformation = metadataAdapter.getModelTransformation();

        modelType = toInt(metadataAdapter.getGeoKey(GTModelTypeGeoKey));
        geographicType = toInt(metadataAdapter.getGeoKey(GeographicTypeGeoKey));
        rasterType = toInt(metadataAdapter.getGeoKey(GTRasterTypeGeoKey));

        if (modelType != ModelTypeGeocentric) {
            GeogCSHandler geogCSHandler = generateGeogCSHandler(metadataAdapter);
            if (modelType == ModelTypeProjected) {
                csHandler = generateProjCSHandler(metadataAdapter, geogCSHandler);
            } else {
                csHandler = geogCSHandler;
            }
        } else {
            // log don't support geocentric yet.
        }
    }

    private GeogCSHandler generateGeogCSHandler(GeoTiffIIOMetadataAdapter metadata) {
        GeogCS geogCS = null;
        geogCS = EPSG.findGeogCSByCode(geographicType);
        if (geogCS == null) {
            int datumCode = toInt(metadata.getGeoKey(GeogGeodeticDatumGeoKey));
            if (datumCode != Integer.MIN_VALUE) { // no data code
                Datum datum = EPSG.findDatumByCode(datumCode);
                if (datum != null) {
                    geogCS = EPSG.findGeogCSByDatum(datum);
                }
            }
        }
        return (geogCS == null) ?
            null :
            new GeogCSHandler(geogCS);
    }

    private ProjCSHandler generateProjCSHandler(
            GeoTiffIIOMetadataAdapter metadata,
            GeogCSHandler geogCSHandler) {

        ProjCSHandler pcsh = generateProjCSHandlerFromProjectedCSType(
                metadata, geogCSHandler);
//        if (pcsh == null) {
//            pcsh = generateProjCSHandlerFromProjection(
//                  metadata, geogCSHandler);
//        }
        if (pcsh == null) {
            pcsh = generateProjCSHandlerFromProjCoordTrans(
                    metadata, geogCSHandler);
        }
        return pcsh;
    }

    private ProjCSHandler generateProjCSHandlerFromProjectedCSType(
            GeoTiffIIOMetadataAdapter metadata,
            GeogCSHandler geogCSHandler) {

        int projectedCSType = toInt(metadata.getGeoKey(ProjectedCSTypeGeoKey));

        String gridMappingName = null;
        Map<String, Double> pMap = new LinkedHashMap<String, Double>();
        ProjCS projCS = EPSG.findProjCSByCode(projectedCSType);
        if (projCS == null) {
            return null;
        }
        ProjCoordTrans pct = ProjCoordTrans.findByEPSGCode(projCS.getCoordOpMethodCode());
        switch(pct) {
            case CT_AlbersEqualArea:
                gridMappingName = "albers_conical_equal_area";
                pMap.put("standard_parallel1",
                        projCS.getParameterValueByCode(EPSGStdParallel1Lat));
                pMap.put("standard_parallel2",
                        projCS.getParameterValueByCode(EPSGStdParallel2Lat));
                pMap.put("latitude_of_projection_origin",
                        projCS.getParameterValueByCode(EPSGFalseOriginLat));
                pMap.put("longitude_of_central_meridian",
                        projCS.getParameterValueByCode(EPSGFalseOriginLong));
                pMap.put("false_easting",
                        projCS.getParameterValueByCode(EPSGFalseOriginEasting));
                pMap.put("false_northing",
                        projCS.getParameterValueByCode(EPSGFalseOriginNorthing));
                break;
            case CT_LambertAzimEqualArea:
                gridMappingName = "lambert_azimuthal_equal_area";
                pMap.put("latitude_of_projection_origin",
                        projCS.getParameterValueByCode(EPSGNatOriginLat));
                pMap.put("longitude_of_projection_origin",
                        projCS.getParameterValueByCode(EPSGNatOriginLong));
                pMap.put("false_easting",
                        projCS.getParameterValueByCode(EPSGFalseEasting));
                pMap.put("false_northing",
                        projCS.getParameterValueByCode(EPSGFalseNorthing));
                break;
            case CT_LambertConfConic_2SP:
                gridMappingName = "lambert_conformal_conic";
                pMap.put("latitude_of_projection_origin",
                        projCS.getParameterValueByCode(EPSGFalseOriginLat));
                pMap.put("longitude_of_central_meridian",
                        projCS.getParameterValueByCode(EPSGFalseOriginLong));
                pMap.put("standard_parallel1",
                        projCS.getParameterValueByCode(EPSGStdParallel1Lat));
                pMap.put("standard_parallel2",
                        projCS.getParameterValueByCode(EPSGStdParallel2Lat));
                pMap.put("false_easting",
                        projCS.getParameterValueByCode(EPSGFalseOriginEasting));
                pMap.put("false_northing",
                        projCS.getParameterValueByCode(EPSGFalseOriginNorthing));
                break;
            case CT_Mercator:
                gridMappingName = "mercator";
                pMap.put("latitude_of_projection_origin",
                        projCS.getParameterValueByCode(EPSGNatOriginLat));
                pMap.put("longitude_of_projection_origin",
                        projCS.getParameterValueByCode(EPSGNatOriginLong));
                pMap.put("scale_factor_at_projection_origin",
                        projCS.getParameterValueByCode(EPSGNatOriginScaleFactor));
                pMap.put("standard_parallel",
                        projCS.getParameterValueByCode(EPSGStdParallel1Lat));
                pMap.put("false_easting",
                        projCS.getParameterValueByCode(EPSGFalseEasting));
                pMap.put("false_northing",
                        projCS.getParameterValueByCode(EPSGFalseNorthing));
                break;
            case CT_PolarStereographic:
                gridMappingName = "polar_stereographic";
                pMap.put("latitude_of_projection_origin",
                        projCS.getParameterValueByCode(EPSGNatOriginLat));
                pMap.put("straight_vertical_longitude_from_pole",
                        projCS.getParameterValueByCode(EPSGNatOriginLong));
                pMap.put("scale_factor_at_projection_origin",
                        projCS.getParameterValueByCode(EPSGNatOriginScaleFactor));
                pMap.put("false_easting",
                        projCS.getParameterValueByCode(EPSGFalseEasting));
                pMap.put("false_northing",
                        projCS.getParameterValueByCode(EPSGFalseNorthing));
                break;
            case CT_TransverseMercator:
                gridMappingName = "transverse_mercator";
                pMap.put("latitude_of_projection_origin",
                        projCS.getParameterValueByCode(EPSGNatOriginLat));
                pMap.put("longitude_of_central_meridian",
                        projCS.getParameterValueByCode(EPSGNatOriginLong));
                pMap.put("scale_factor_at_central_meridian",
                        projCS.getParameterValueByCode(EPSGNatOriginScaleFactor));
                pMap.put("false_easting",
                        projCS.getParameterValueByCode(EPSGFalseEasting));
                pMap.put("false_northing",
                        projCS.getParameterValueByCode(EPSGFalseNorthing));
                break;
            default:
        }
        return (gridMappingName == null) ?
            null :
            new ProjCSHandler(geogCSHandler, gridMappingName, pMap);
    }

    private ProjCSHandler generateProjCSHandlerFromProjCoordTrans(
            GeoTiffIIOMetadataAdapter metadata,
            GeogCSHandler geogCSHandler) {

        int projCoordTrans = toInt(metadata.getGeoKey(ProjCoordTransGeoKey));

        String gridMappingName = null;
        Map<String, Double> pMap = new LinkedHashMap<String, Double>();
        ProjCoordTrans pct = ProjCoordTrans.findByGeoTIFFCode(projCoordTrans);
        double lat;
        double lon;
        switch(pct) {
            case CT_AlbersEqualArea:
                gridMappingName = "albers_conical_equal_area";
                pMap.put("standard_parallel1",
                        toDouble(metadata.getGeoKey(ProjStdParallel1GeoKey)));
                pMap.put("standard_parallel2",
                        toDouble(metadata.getGeoKey(ProjStdParallel2GeoKey)));
                pMap.put("latitude_of_projection_origin",
                        toDouble(metadata.getGeoKey(ProjNatOriginLatGeoKey)));
                pMap.put("longitude_of_central_meridian",
                        toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey)));
                pMap.put("false_easting",
                        toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                pMap.put("false_northing",
                        toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                break;
            case CT_AzimuthalEquidistant:
                gridMappingName = "azimuthal_equidistant";
                pMap.put("latitude_of_projection_origin",
                        toDouble(metadata.getGeoKey(ProjCenterLatGeoKey)));
                pMap.put("longitude_of_projection_origin",
                        toDouble(metadata.getGeoKey(ProjCenterLongGeoKey)));
                pMap.put("false_easting",
                        toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                pMap.put("false_northing",
                        toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                break;
            case CT_LambertAzimEqualArea:
                gridMappingName = "lambert_azimuthal_equal_area";
                // libgeotiff source and docs say to use ProjCenter but Intergraph
                // samples use ProjNat, eh...
                lat = toDouble(metadata.getGeoKey(ProjCenterLatGeoKey));
                if (Double.isNaN(lat)) {
                    lat = toDouble(metadata.getGeoKey(ProjNatOriginLatGeoKey));
                }
                lon = toDouble(metadata.getGeoKey(ProjCenterLongGeoKey));
                if (Double.isNaN(lon)) {
                    lon = toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey));
                }
                pMap.put("latitude_of_projection_origin", lat);
                pMap.put("longitude_of_projection_origin", lon);
                pMap.put("false_easting",
                        toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                pMap.put("false_northing",
                        toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                break;
            case CT_LambertConfConic_2SP:
                gridMappingName = "lambert_conformal_conic";
                // libgeotiff source and docs say to use ProjFalseOrigin but
                // Intergraph samples use ProjNat, eh...
                lat = toDouble(metadata.getGeoKey(ProjFalseOriginLatGeoKey));
                if (Double.isNaN(lat)) {
                    lat = toDouble(metadata.getGeoKey(ProjNatOriginLatGeoKey));
                }
                lon = toDouble(metadata.getGeoKey(ProjFalseOriginLongGeoKey));
                if (Double.isNaN(lon)) {
                    lon = toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey));
                }
                pMap.put("latitude_of_projection_origin", lat);
                pMap.put("longitude_of_central_meridian", lon);
                pMap.put("standard_parallel1",
                        toDouble(metadata.getGeoKey(ProjStdParallel1GeoKey)));
                pMap.put("standard_parallel2",
                        toDouble(metadata.getGeoKey(ProjStdParallel2GeoKey)));
                pMap.put("false_easting",
                        toDouble(metadata.getGeoKey(ProjFalseOriginEastingGeoKey)));
                pMap.put("false_northing",
                        toDouble(metadata.getGeoKey(ProjFalseOriginNorthingGeoKey)));
                break;
            case CT_CylindricalEqualArea:
                gridMappingName = "lambert_cylindrical_equal_area";
                pMap.put("longitude_of_central_meridian",
                        toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey)));
                pMap.put("standard_parallel",
                        toDouble(metadata.getGeoKey(ProjStdParallel1GeoKey)));
                pMap.put("false_easting",
                        toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                pMap.put("false_northing",
                        toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                break;
            case CT_Mercator:
                gridMappingName = "mercator";
                pMap.put("longitude_of_projection_origin",
                        toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey)));
                pMap.put("standard_parallel",
                        toDouble(metadata.getGeoKey(ProjStdParallel1GeoKey)));
                pMap.put("scale_factor_at_projection_origin",
                        toDouble(metadata.getGeoKey(ProjScaleAtNatOriginGeoKey)));
                pMap.put("false_easting",
                        toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                pMap.put("false_northing",
                        toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                break;
            case CT_Orthographic:
                gridMappingName = "orthographic";
                // libgeotiff source and docs say to use ProjCenter but
                // Intergraph samples use ProjNat, eh...
                lat = toDouble(metadata.getGeoKey(ProjCenterLatGeoKey));
                if (Double.isNaN(lat)) {
                    lat = toDouble(metadata.getGeoKey(ProjNatOriginLatGeoKey));
                }
                lon = toDouble(metadata.getGeoKey(ProjCenterLongGeoKey));
                if (Double.isNaN(lon)) {
                    lon = toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey));
                }
                pMap.put("latitude_of_projection_origin", lat);
                pMap.put("longitude_of_projection_origin", lon);
                pMap.put("false_easting",
                        toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                pMap.put("false_northing",
                        toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                break;
            case CT_PolarStereographic:
                gridMappingName = "polar_stereographic";
                pMap.put("latitude_of_projection_origin",
                        toDouble(metadata.getGeoKey(ProjNatOriginLatGeoKey)));
                pMap.put("straight_vertical_longitude_from_pole",
                        toDouble(metadata.getGeoKey(ProjStraightVertPoleLongGeoKey)));
                pMap.put("scale_factor_at_projection_origin",
                        toDouble(metadata.getGeoKey(ProjScaleAtNatOriginGeoKey)));
                pMap.put("false_easting",
                        toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                pMap.put("false_northing",
                        toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                break;
            case CT_Stereographic:
                gridMappingName = "stereographic";
                pMap.put("latitude_of_projection_origin",
                        toDouble(metadata.getGeoKey(ProjCenterLatGeoKey)));
                pMap.put("longitude_of_projection_origin",
                        toDouble(metadata.getGeoKey(ProjCenterLongGeoKey)));
                pMap.put("scale_factor_at_projection_origin",
                        toDouble(metadata.getGeoKey(ProjScaleAtNatOriginGeoKey)));
                pMap.put("false_easting",
                        toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                pMap.put("false_northing",
                        toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                break;
            case CT_TransverseMercator:
                gridMappingName = "transverse_mercator";
                pMap.put("latitude_of_projection_origin",
                        toDouble(metadata.getGeoKey(ProjNatOriginLatGeoKey)));
                pMap.put("longitude_of_central_meridian",
                        toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey)));
                pMap.put("scale_factor_at_central_meridian",
                        toDouble(metadata.getGeoKey(ProjScaleAtNatOriginGeoKey)));
                pMap.put("false_easting",
                        toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                pMap.put("false_northing",
                        toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                break;
            default:
        }
        return (gridMappingName == null) ?
            null :
            new ProjCSHandler(geogCSHandler, gridMappingName, pMap);
    }



    boolean isSupported() {
        return width > -1 && height > -1 &&
                pixelScales != null &&
                tiePoints != null &&
                csHandler != null;
    }

    public CSHandler getCSHandler() {
        return csHandler;
    }

    private static int toInt(String string) {
        if (string != null && string.length() > 0) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                // error handler on fall through
            }
        }
        return Integer.MIN_VALUE;
    }

    private static double toDouble(String string) {
        if (string != null && string.length() > 0) {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException e) {
                // error handler on fall through
            }
        }
        return Double.NaN;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeoTIFFIIOCoordSys other = (GeoTIFFIIOCoordSys) obj;
        if (this.width != other.width) {
            return false;
        }
        if (this.height != other.height) {
            return false;
        }
        if (!Arrays.equals(this.pixelScales, other.pixelScales)) {
            return false;
        }
        if (!Arrays.equals(this.tiePoints, other.tiePoints)) {
            return false;
        }
        if (!Arrays.equals(this.transformation, other.transformation)) {
            return false;
        }
        if (this.csHandler != other.csHandler && (this.csHandler == null || !this.csHandler.equals(other.csHandler))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.width;
        hash = 17 * hash + this.height;
        hash = 17 * hash + Arrays.hashCode(this.pixelScales);
        hash = 17 * hash + Arrays.hashCode(this.tiePoints);
        hash = 17 * hash + Arrays.hashCode(this.transformation);
        hash = 17 * hash + (this.csHandler != null ? this.csHandler.hashCode() : 0);
        return hash;
    }

    public class Adapter {

        private NetcdfFile ncFile;

        private String dimensionsAsString;
        private String coordinatesAsString;

        private boolean generated;

        public Adapter(NetcdfFile ncFile) {
            this.ncFile = ncFile;
        }

        public synchronized void generate(int index) {
            if (generated) {
                throw new IllegalStateException("Coordinate system already generated for this instance");
            }

            Dimension xDim = ncFile.addDimension(
                    null,new Dimension("x" + index, width));
            Dimension yDim = ncFile.addDimension(
                    null, new Dimension("y" + index, height));

            double tiePointXCenter = rasterType == RasterPixelIsPoint ?
                    tiePoints[3] :
                    tiePoints[3] + pixelScales[0] / 2d;
            double tiePointYCenter = rasterType == RasterPixelIsPoint ?
                    tiePoints[4] :
                    tiePoints[4] - pixelScales[1] / 2d;

            String xName = null;
            String yName = null;
            String xUnits = null;
            String yUnits = null;
            String xStandardName = null;
            String yStandardName = null;
            if (modelType == ModelTypeProjected) {
                xName = "geoX" + index;
                yName = "geoY" + index;
                xUnits = "m";
                yUnits = "m";
                xStandardName = "projection_x_coordinate";
                yStandardName = "projection_y_coordinate";
            } else {
                xName = "lon" + index;
                yName = "lat" + index;
                xUnits = "degrees_east";
                yUnits = "degrees_north";
                xStandardName = "longitude";
                yStandardName = "latitude";
            }

            Variable xVar = new Variable(ncFile, null, null, xName);
            xVar.setDataType(DataType.DOUBLE);
            xVar.setDimensions(xDim.getName());
            xVar.addAttribute(new Attribute("units", xUnits));
            xVar.addAttribute(new Attribute("standard_name", xStandardName));
            xVar.setCachedData(
                    Array.makeArray(
                        DataType.DOUBLE,
                        width,
                        tiePointXCenter,
                        pixelScales[0]),
                    false);
            ncFile.addVariable(null, xVar);

            Variable yVar = new Variable(ncFile, null, null, yName);
            yVar.setDataType(DataType.DOUBLE);
            yVar.setDimensions(yDim.getName());
            yVar.addAttribute(new Attribute("units", yUnits));
            yVar.addAttribute(new Attribute("standard_name", yStandardName));
            yVar.setCachedData(
                    Array.makeArray(
                        DataType.DOUBLE,
                        height,
                        tiePointYCenter,
                        -pixelScales[1]),
                    false);
            ncFile.addVariable(null, yVar);

            /* code below is functional but appears unneeded */
//            if (rasterType == RasterPixelIsArea) {
//                Dimension boundsDim = ncFile.getRootGroup().findDimension("bounds");
//                if (boundsDim == null) {
//                    boundsDim = ncFile.addDimension(
//                        null, new Dimension("bounds", 2));
//                }
//
//                // X Bounds
//                {
//                    Variable xBoundsVar = new Variable(ncFile, null, null, xVar.getName() + "_bounds");
//                    xBoundsVar.setDataType(DataType.DOUBLE);
//                    xBoundsVar.setDimensions(xDim.getName() + " " + boundsDim.getName());
//
//                    xVar.addAttribute(new Attribute("bounds", xBoundsVar.getName()));
//
//                    int xBoundsCount = width * 2;
//                    double[] xBounds = new double[xBoundsCount];
//                    double xBound = tiePoints[3];
//                    xBounds[0] = xBound;
//                    for (int xBoundsIndex = 1; xBoundsIndex < xBoundsCount - 1; /* incremented in body */ ) {
//                        xBound += pixelScales[0];
//                        xBounds[xBoundsIndex++] = xBound;
//                        xBounds[xBoundsIndex++] = xBound;
//                    }
//                    xBound += pixelScales[0];
//                    xBounds[xBoundsCount - 1] = xBound;
//                    xBoundsVar.setCachedData(
//                            Array.factory(
//                                DataType.DOUBLE,
//                                new int[] { width, 2 },
//                                xBounds),
//                            false);
//                    ncFile.addVariable(null, xBoundsVar);
//                }
//
//                // Y Bounds
//                {
//                    Variable yBoundsVar = new Variable(ncFile, null, null, yVar.getName() + "_bounds");
//                    yBoundsVar.setDataType(DataType.DOUBLE);
//                    yBoundsVar.setDimensions(yDim.getName() + " " + boundsDim.getName());
//
//                    yVar.addAttribute(new Attribute("bounds", yBoundsVar.getName()));
//
//                    int yBoundsCount = height * 2;
//                    double[] yBounds = new double[yBoundsCount];
//                    double yBound = tiePoints[4];
//                    yBounds[0] = yBound;
//                    for (int yBoundsIndex = 1; yBoundsIndex < yBoundsCount - 1; /* incremented in body */ ) {
//                        yBound -= pixelScales[1];
//                        yBounds[yBoundsIndex++] = yBound;
//                        yBounds[yBoundsIndex++] = yBound;
//                    }
//                    yBound -= pixelScales[1];
//                    yBounds[yBoundsCount - 1] = yBound;
//                    yBoundsVar.setCachedData(
//                            Array.factory(
//                                DataType.DOUBLE,
//                                new int[] { height, 2 },
//                                yBounds),
//                            false);
//                    ncFile.addVariable(null, yBoundsVar);
//                }
//            }

            dimensionsAsString = yDim.getName() + " " + xDim.getName();
            coordinatesAsString = yVar.getName() + " " + xVar.getName();

            generated = true;
        }

        public synchronized String getDimensionsAsString() {
            if (!generated) {
                throw new IllegalStateException("Coordinate system not generated for this instance");
            }
            return dimensionsAsString;
        }

        public synchronized String getCoordinatesAsString() {
            if (!generated) {
                throw new IllegalStateException("Coordinate system not generated for this instance");
            }
            return coordinatesAsString;
        }

    }

}
