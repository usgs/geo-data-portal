package ucar.nc2.iosp.geotiff.epsg.csv;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.CsvToBean;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ucar.nc2.iosp.geotiff.epsg.GTDatum;
import ucar.nc2.iosp.geotiff.epsg.GTDatum.Type;
import ucar.nc2.iosp.geotiff.epsg.EPSGFactory;
import ucar.nc2.iosp.geotiff.epsg.GTEllipsoid;
import ucar.nc2.iosp.geotiff.epsg.GTGeogCS;
import ucar.nc2.iosp.geotiff.epsg.GTPrimeMeridian;
import ucar.nc2.iosp.geotiff.epsg.GTProjCS;
import ucar.nc2.iosp.geotiff.epsg.GTUnitOfMeasure;
import ucar.nc2.iosp.geotiff.epsg.csv.CSVFilteredMappingStrategy.Filter;

/**
 *
 * @author tkunicki
 */
public class CSVEPSGFactory implements EPSGFactory {

    private final static String RESOURCE_GCS = "/ucar/nc2/iosp/geotiff/epsg/csv/gcs.csv";
    private final static String RESOURCE_PCS = "/ucar/nc2/iosp/geotiff/epsg/csv/pcs.csv";
    private final static String RESOURCE_DATUM = "/ucar/nc2/iosp/geotiff/epsg/csv/datum.csv";
    private final static String RESOURCE_ELLIPSOID = "/ucar/nc2/iosp/geotiff/epsg/csv/ellipsoid.csv";
    private final static String RESOURCE_PRIMEMERIDIAN = "/ucar/nc2/iosp/geotiff/epsg/csv/prime_meridian.csv";
    private final static String RESOURCE_UNITOFMEASURE = "/ucar/nc2/iosp/geotiff/epsg/csv/unit_of_measure.csv";

    private static CSVEPSGFactory instance;
    public synchronized static CSVEPSGFactory getInstance() {
        if (instance == null) {
            instance = new CSVEPSGFactory();
        }
        return instance;
    }
    
    private Map<Integer, GeogCSEntry> geogCSMap;
    private Map<Integer, ProjCSEntry> projCSMap;
    private Map<Integer, DatumEntry> datumMap;
    private Map<Integer, EllipsoidEntry> ellipsoidMap;
    private Map<Integer, PrimeMeridianEntry> primeMeridianMap;
    private Map<Integer, UnitOfMeasureEntry> unitOfMeasureMap;

    private CSVEPSGFactory() {
        
    }
    
    @Override
    public synchronized GeogCSEntry findGeogCSByCode(int code) {
        if (geogCSMap == null) {
            geogCSMap = new LinkedHashMap<Integer, GeogCSEntry>();

            Map<String, String> cm = new HashMap<String, String>();
            cm.put("COORD_REF_SYS_CODE", "code");
            cm.put("COORD_REF_SYS_NAME", "name");
            cm.put("DATUM_CODE", "datumCode");
//            cm.put("GREENWICH_DATUM", "greenwichDatumCode");
            cm.put("UOM_CODE", "unitOfMeasureCode");
            cm.put("ELLIPSOID_CODE", "ellipsoidCode");
            cm.put("PRIME_MERIDIAN_CODE", "primeMeridianCode");

            loadBeansFromCSV(GeogCSEntry.class, geogCSMap, cm, null, RESOURCE_GCS);
        }
        return geogCSMap.get(code);
    }

    @Override
    public synchronized GeogCSEntry findGeogCSByDatum(GTDatum datum) {
        GeogCSEntry geogCS = null;
        if (datum.getType() == Type.geodetic) {
            // Try quick estimate, datumCode - 2000. This should usually work
            // except for the archaic datums.
            // NOTE: This is also used to load the geogCSMap if it hasn't
            // been loaded...
            geogCS = findGeogCSByCode(datum.getCode() - 2000);

            if (geogCS == null || geogCS.getDatum().getCode() != datum.getCode()) {
                geogCS = null;
                Iterator<GeogCSEntry> geogCSIterator = geogCSMap.values().iterator();
                while (geogCSIterator.hasNext() && geogCS == null) {
                    GeogCSEntry next = geogCSIterator.next();
                    if (next.getDatum().getCode() == datum.getCode()) {
                        geogCS = next;
                    }
                }
            }
        }
        return geogCS;
    }

    @Override
    public synchronized ProjCSEntry findProjCSByCode(int code) {
        if (projCSMap == null) {
            projCSMap = new LinkedHashMap<Integer, ProjCSEntry>();

            Map<String, String> cm = new HashMap<String, String>();
            cm.put("COORD_REF_SYS_CODE", "code");
            cm.put("COORD_REF_SYS_NAME", "name");
            cm.put("UOM_CODE", "unitOfMeasureCode");
            cm.put("SOURCE_GEOGCRS_CODE", "sourceGeogCSCode");
            cm.put("COORD_OP_CODE", "coordOpCode");
            cm.put("COORD_OP_METHOD_CODE", "coordOpMethodCode");
            cm.put("PARAMETER_CODE_1", "parameter1Code");
            cm.put("PARAMETER_UOM_1", "parameter1UnitOfMeasureCode");
            cm.put("PARAMETER_VALUE_1", "parameter1Value");
            cm.put("PARAMETER_CODE_2", "parameter2Code");
            cm.put("PARAMETER_UOM_2", "parameter2UnitOfMeasureCode");
            cm.put("PARAMETER_VALUE_2", "parameter2Value");
            cm.put("PARAMETER_CODE_3", "parameter3Code");
            cm.put("PARAMETER_UOM_3", "parameter3UnitOfMeasureCode");
            cm.put("PARAMETER_VALUE_3", "parameter3Value");
            cm.put("PARAMETER_CODE_4", "parameter4Code");
            cm.put("PARAMETER_UOM_4", "parameter4UnitOfMeasureCode");
            cm.put("PARAMETER_VALUE_4", "parameter4Value");
            cm.put("PARAMETER_CODE_5", "parameter5Code");
            cm.put("PARAMETER_UOM_5", "parameter5UnitOfMeasureCode");
            cm.put("PARAMETER_VALUE_5", "parameter5Value");
            cm.put("PARAMETER_CODE_6", "parameter6Code");
            cm.put("PARAMETER_UOM_6", "parameter6UnitOfMeasureCode");
            cm.put("PARAMETER_VALUE_6", "parameter6Value");
            cm.put("PARAMETER_CODE_7", "parameter7Code");
            cm.put("PARAMETER_UOM_7", "parameter7UnitOfMeasureCode");
            cm.put("PARAMETER_VALUE_7", "parameter7Value");

            loadBeansFromCSV(ProjCSEntry.class, projCSMap, cm, null, RESOURCE_PCS);
        }
        return projCSMap.get(code);
    }

    @Override
    public synchronized DatumEntry findDatumByCode(int code) {
        if (datumMap == null) {
            datumMap = new LinkedHashMap<Integer, DatumEntry>();

            Map<String, String> cm = new HashMap<String, String>();
            cm.put("datum_code", "code");
            cm.put("datum_name", "name");
            cm.put("datum_type", "typeAsString");
            cm.put("ellipsoid_code", "ellipsoidCode");
            cm.put("prime_meridian_code", "primeMeridianCode");

            loadBeansFromCSV(DatumEntry.class, datumMap, cm, null, RESOURCE_DATUM);
        }
        return datumMap.get(code);
    }

    @Override
    public synchronized EllipsoidEntry findEllipsoidByCode(int code) {
        if (ellipsoidMap == null) {
            ellipsoidMap = new LinkedHashMap<Integer, EllipsoidEntry>();

            Map<String, String> cm = new HashMap<String, String>();
            cm.put("ellipsoid_code", "code");
            cm.put("ellipsoid_name", "name");
            cm.put("semi_major_axis", "semiMajorAxis");
            cm.put("semi_minor_axis", "semiMinorAxis");
            cm.put("inv_flattening", "inverseFlattening");
            cm.put("uom_code", "unitOfMeasureCode");

            loadBeansFromCSV(EllipsoidEntry.class, ellipsoidMap, cm, null, RESOURCE_ELLIPSOID);
        }
        return ellipsoidMap.get(code);
    }

    @Override
    public synchronized PrimeMeridianEntry findPrimeMeridianByCode(int code) {
        if (primeMeridianMap == null) {
            primeMeridianMap = new LinkedHashMap<Integer, PrimeMeridianEntry>();

            Map<String, String> cm = new HashMap<String, String>();
            cm.put("PRIME_MERIDIAN_CODE", "code");
            cm.put("PRIME_MERIDIAN_NAME", "name");
            cm.put("GREENWICH_LONGITUDE", "longitude");
            cm.put("UOM_CODE", "unitOfMeasureCode");

            loadBeansFromCSV(PrimeMeridianEntry.class, primeMeridianMap, cm, null, RESOURCE_PRIMEMERIDIAN);
        }
        return primeMeridianMap.get(code);
    }

    @Override
    public synchronized UnitOfMeasureEntry findUnitOfMeasureByCode(int code) {
        if (unitOfMeasureMap == null) {
            unitOfMeasureMap = new LinkedHashMap<Integer, UnitOfMeasureEntry>();

            Map<String, String> cm = new HashMap<String, String>();
            cm.put("uom_code", "code");
            cm.put("unit_of_measure_name", "name");
            cm.put("unit_of_measure_type", "type");
            cm.put("target_uom_code", "targetUnitOfMeasureCode");
            cm.put("factor_b", "factorB");
            cm.put("factor_c", "factorC");

            loadBeansFromCSV(UnitOfMeasureEntry.class, unitOfMeasureMap, cm, null, RESOURCE_UNITOFMEASURE);
        }
        return unitOfMeasureMap.get(code);
    }
    
    private InputStream getResourceAsStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }

    private synchronized <T extends CSVEntry> void loadBeansFromCSV (
            Class<T> beanClass,
            Map<Integer, T> beanMap,
            Map<String, String> columnMap,
            Filter filter,
            String  csvResourceName) {

            InputStream is = null;
            try {
                is = getResourceAsStream(csvResourceName);
                loadBeansFromCSV(beanClass, beanMap, columnMap, filter, is);
            } finally {
                if (is != null) {
                    try { is.close(); } catch (IOException e) { }
                }
            }
    }
    
    private synchronized <T extends CSVEntry> void loadBeansFromCSV (
            Class<T> beanClass,
            Map<Integer, T> beanMap,
            Map<String, String> columnMap,
            Filter filter,
            InputStream inputStream) {

        CSVFilteredMappingStrategy ms =
                new CSVFilteredMappingStrategy();
        ms.setType(beanClass);
        ms.setColumnMapping(columnMap);
        ms.setFilter(filter);

        CsvToBean csv = new CSVFilteredToBean();

        List<T> list = csv.parse(ms, new CSVReader(
                new BufferedReader(
                    new InputStreamReader(inputStream))));

        for (T hd : list) {
            beanMap.put(hd.getCode(), hd);
        }

    }
    
    public static void main(String[] args) {
        GTGeogCS geogCS = getInstance().findGeogCSByCode(4326);
        System.out.println(geogCS);
    }

}
