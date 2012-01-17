package ucar.nc2.iosp.geotiff.epsg.geotools;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.measure.unit.Unit;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.factory.epsg.ThreadedEpsgFactory;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.VerticalDatum;
import ucar.nc2.iosp.geotiff.epsg.GTDatum;
import ucar.nc2.iosp.geotiff.epsg.EPSGFactory;
import ucar.nc2.iosp.geotiff.epsg.GTEllipsoid;
import ucar.nc2.iosp.geotiff.epsg.GTGeogCS;
import ucar.nc2.iosp.geotiff.epsg.GTPrimeMeridian;
import ucar.nc2.iosp.geotiff.epsg.GTProjCS;
import ucar.nc2.iosp.geotiff.epsg.GTUnitOfMeasure;
import ucar.nc2.iosp.geotiff.epsg.csv.CSVEPSGFactory;

/**
 *
 * @author tkunicki
 */
public class GeoToolsEPSGFactory implements EPSGFactory {

    private static GeoToolsEPSGFactory instance;
    
    public synchronized static GeoToolsEPSGFactory getInstance() {
        if (instance == null) {
            instance = new GeoToolsEPSGFactory();
        }
        return instance;
    }
    
    ThreadedEpsgFactory delegateFactory; 
    
    private GeoToolsEPSGFactory() {
        // OUCH!
        delegateFactory = (ThreadedEpsgFactory) ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null);
    }
    
    @Override
    public GTGeogCS findGeogCSByCode(int code) {
        try {
            GeographicCRS geographicCRS = delegateFactory.createGeographicCRS(Integer.toString(code));
            if (geographicCRS != null) {
                return new GeogCSAdapter(geographicCRS);
            }
        } catch (FactoryException ex) {
            System.out.println(ex);
        }
        return null;
    }

    @Override
    public GTGeogCS findGeogCSByDatum(GTDatum datum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GTProjCS findProjCSByCode(int code) {
        try {
            ProjectedCRS projectedCRS = delegateFactory.createProjectedCRS(Integer.toString(code));
            if (projectedCRS != null) {
                return new ProjCSAdapter(projectedCRS);
            }
        } catch (FactoryException ex) {
            System.out.println(ex);
        }
        return null;
    }

    @Override
    public GTDatum findDatumByCode(int code) {
        try {
            Datum datum = delegateFactory.createDatum(Integer.toString(code));
            if (datum != null) {
                return new DatumAdapter(datum);
            }
        } catch (FactoryException ex) {
            // TODO log
        }
        return null;
    }

    @Override
    public GTEllipsoid findEllipsoidByCode(int code) {
        try {
            Ellipsoid ellipsoid = delegateFactory.createEllipsoid(Integer.toString(code));
            if (ellipsoid != null) {
                return new EllipsoidAdapter(ellipsoid);
            }
        } catch (FactoryException ex) {
            // TODO log
        }
        return null;
    }

    @Override
    public GTPrimeMeridian findPrimeMeridianByCode(int code) {
        try {
            PrimeMeridian primeMeridian = delegateFactory.createPrimeMeridian(Integer.toString(code));
            if (primeMeridian != null) {
                return new PrimeMeridianAdapter(primeMeridian);
            }
        } catch (FactoryException ex) {
            // TODO log
        }
        return null;
    }

    @Override
    public GTUnitOfMeasure findUnitOfMeasureByCode(int code) {
        try {
            Unit unit = delegateFactory.createUnit(Integer.toString(code));
            if (unit != null) {
                return new UnitOfMeasureAdapter(unit);
            }
        } catch (FactoryException ex) {
            // TODO log
        }
        return null;
    }
    
    private class IndentifiedObjectAdapter<I extends IdentifiedObject> {
        
        final protected I wrappedInstance;
        
        private IndentifiedObjectAdapter(I wrappedInstance) {
            this.wrappedInstance = wrappedInstance;
        }
        
        public int getCode() {
            return getEPSGCodeFromIdentifiedObject(wrappedInstance);
        }
        
        public String getName() {
            return wrappedInstance.getName().getCode();
        }
        
        protected int getEPSGCodeFromIdentifiedObject(IdentifiedObject identifiedObject) {
            int code = -1;
            Set<ReferenceIdentifier> identifierSet = identifiedObject.getIdentifiers();
            if (identifierSet != null) {
                Iterator<ReferenceIdentifier> identifierIterator = identifiedObject.getIdentifiers().iterator();
                while(identifierIterator.hasNext() && code < 0) {
                    ReferenceIdentifier identifier = identifierIterator.next();
                    if ("EPSG".equals(identifier.getCodeSpace())) {
                        code = Integer.parseInt(identifier.getCode());
                    }
                }
            }
            return code;
        }
    }
    
    public class GeogCSAdapter extends IndentifiedObjectAdapter<GeographicCRS> implements GTGeogCS {

        
        private GeogCSAdapter(GeographicCRS geographicCRS) {
            super(geographicCRS);
        }

        @Override
        public GTDatum getDatum() {
            return new DatumAdapter(wrappedInstance.getDatum());
        }

        @Override
        public GTUnitOfMeasure getUnitOfMeasure() {
            return new UnitOfMeasureAdapter(wrappedInstance.getCoordinateSystem().getAxis(0).getUnit());
        }

        @Override
        public GTEllipsoid getEllipsoid() {
            return new EllipsoidAdapter(wrappedInstance.getDatum().getEllipsoid());
        }

        @Override
        public GTPrimeMeridian getPrimeMeridian() {
            return new PrimeMeridianAdapter(wrappedInstance.getDatum().getPrimeMeridian());
        }
    }
    
    public class ProjCSAdapter  extends IndentifiedObjectAdapter<ProjectedCRS> implements GTProjCS {
        
        private ProjCSAdapter(ProjectedCRS projectedCRS) {
            super(projectedCRS);
        }

        @Override
        public GTUnitOfMeasure getUnitOfMeasure() {
            return new UnitOfMeasureAdapter(wrappedInstance.getCoordinateSystem().getAxis(0).getUnit());
        }

        @Override
        public GTGeogCS getSourceGeogCS() {
            return new GeogCSAdapter(wrappedInstance.getBaseCRS());
        }

        @Override
        public int getCoordOpCode() {
            return getEPSGCodeFromIdentifiedObject(wrappedInstance.getConversionFromBase());
        }

        @Override
        public int getCoordOpMethodCode() {
            return getEPSGCodeFromIdentifiedObject(wrappedInstance.getConversionFromBase().getMethod());
        }

        @Override
        public int getParameter1Code() { return getEPSGParameterCode(0); }

        @Override
        public GTUnitOfMeasure getParameter1UnitOfMeasure() { return getParameterUnit(0); }

        @Override
        public double getParameter1Value() { return getParameterValue(0); }

        @Override
        public int getParameter2Code() { return getEPSGParameterCode(1); }

        @Override
        public GTUnitOfMeasure getParameter2UnitOfMeasure() { return getParameterUnit(1); }

        @Override
        public double getParameter2Value() { return getParameterValue(1); }

        @Override
        public int getParameter3Code() { return getEPSGParameterCode(2); }

        @Override
        public GTUnitOfMeasure getParameter3UnitOfMeasure() { return getParameterUnit(2); }

        @Override
        public double getParameter3Value() { return getParameterValue(2); }

        @Override
        public int getParameter4Code() { return getEPSGParameterCode(3); }

        @Override
        public GTUnitOfMeasure getParameter4UnitOfMeasure() { return getParameterUnit(3); }

        @Override
        public double getParameter4Value() { return getParameterValue(3); }

        @Override
        public int getParameter5Code() { return getEPSGParameterCode(4); }

        @Override
        public GTUnitOfMeasure getParameter5UnitOfMeasure() { return getParameterUnit(4); }

        @Override
        public double getParameter5Value() { return getParameterValue(4); }

        @Override
        public int getParameter6Code() { return getEPSGParameterCode(5); }

        @Override
        public GTUnitOfMeasure getParameter6UnitOfMeasure() { return getParameterUnit(5); }

        @Override
        public double getParameter6Value() { return getParameterValue(5); }

        @Override
        public int getParameter7Code() { return getEPSGParameterCode(6); }

        @Override
        public GTUnitOfMeasure getParameter7UnitOfMeasure() { return getParameterUnit(6); }

        @Override
        public double getParameter7Value() { return getParameterValue(6); }

        @Override
        public double getParameterValueByCode(int code) {
            for (int i = 0; i < 7; ++i) {
                if (getEPSGParameterCode(i) == code) {
                    return getParameterValue(i);
                }
            }
            return MISSING_VALUE;
        }
        
        private int getEPSGParameterCode(int index) {
            List<GeneralParameterDescriptor> descriptorGroup = wrappedInstance.getConversionFromBase().getMethod().getParameters().descriptors();
            if (index < descriptorGroup.size()) {
                return getEPSGCodeFromIdentifiedObject(descriptorGroup.get(index));
            }
            return MISSING_CODE;
        }
        
        private double getParameterValue(int index) {
            List<GeneralParameterDescriptor> descriptorGroup = wrappedInstance.getConversionFromBase().getMethod().getParameters().descriptors();
            if (index < descriptorGroup.size()) {
                return wrappedInstance.getConversionFromBase().getParameterValues().parameter(descriptorGroup.get(index).getName().getCode()).doubleValue();
            }
            return MISSING_VALUE;
        }
        
        private UnitOfMeasureAdapter getParameterUnit(int index) {
            List<GeneralParameterDescriptor> descriptorGroup = wrappedInstance.getConversionFromBase().getMethod().getParameters().descriptors();
            if (index < descriptorGroup.size()) {
                return new UnitOfMeasureAdapter(wrappedInstance.getConversionFromBase().getParameterValues().parameter(descriptorGroup.get(index).getName().getCode()).getUnit());
            }
            return null;
        }
    }
    
    public class DatumAdapter extends IndentifiedObjectAdapter<Datum> implements GTDatum {
        
        private DatumAdapter(Datum datum) {
            super(datum);
        }

        @Override
        public Type getType() {
            if (wrappedInstance instanceof GeodeticDatum) {
                return Type.geodetic;
            } else if (wrappedInstance instanceof EngineeringDatum) {
                return Type.engineering;
            } else if (wrappedInstance instanceof VerticalDatum) {
                return Type.vertical;
            }
            return null;
        }

        @Override
        public GTEllipsoid getEllipsoid() {
            if (wrappedInstance instanceof GeodeticDatum) {
                return new EllipsoidAdapter(((GeodeticDatum)wrappedInstance).getEllipsoid());
            }
            return null;
        }

        @Override
        public GTPrimeMeridian getPrimeMeridian() {
            if (wrappedInstance instanceof GeodeticDatum) {
                return new PrimeMeridianAdapter(((GeodeticDatum)wrappedInstance).getPrimeMeridian());
            }
            return null;
        }
    }
    
    private class EllipsoidAdapter extends IndentifiedObjectAdapter<Ellipsoid> implements GTEllipsoid {
        
        private EllipsoidAdapter(Ellipsoid ellipsoid) {
            super(ellipsoid);
        }

        @Override
        public double getSemiMajorAxis() {
            return wrappedInstance.getSemiMajorAxis();
        }

        @Override
        public double getSemiMinorAxis() {
            return wrappedInstance.getSemiMinorAxis();
        }

        @Override
        public double getInverseFlattening() {
            return wrappedInstance.getInverseFlattening();
        }

        @Override
        public GTUnitOfMeasure getUnitOfMeasure() {
            return new UnitOfMeasureAdapter(wrappedInstance.getAxisUnit());
        }
    }
    
    private class PrimeMeridianAdapter extends IndentifiedObjectAdapter<PrimeMeridian> implements GTPrimeMeridian {
                
        private PrimeMeridianAdapter(PrimeMeridian primeMeridian) {
            super(primeMeridian);
        }

        @Override
        public double getLongitude() {
            return wrappedInstance.getGreenwichLongitude();
        }

        @Override
        public GTUnitOfMeasure getUnitOfMeasure() {
            return new UnitOfMeasureAdapter(wrappedInstance.getAngularUnit());
        }
    }
    
    private class UnitOfMeasureAdapter  /* extends IndentifiedObjectAdapter<Unit> */ implements GTUnitOfMeasure {
        
        private final Unit unit;
        
        private UnitOfMeasureAdapter(Unit unit) {
            this.unit = unit;
        }

        @Override
        public int getCode() {
            return -1;
        }

        @Override
        public String getName() {
            return unit.toString();
        }

        @Override
        public String getType() {
            return unit.getDimension().toString();
        }

        @Override
        public GTUnitOfMeasure getTargetUnitOfMeasure() {
            // TODO: What does this return if it is already standard unit?
            return new UnitOfMeasureAdapter(unit.getStandardUnit());
        }
        
        @Override
        public Unit<?> getUnit() {
            return unit;
        }
    }
    
    public static void main(String[] args) {
//        GTGeogCS geogCS = getInstance().findGeogCSByCode(4326);
//        System.out.println(geogCS.getCode());
//        System.out.println(geogCS.getName());
//        System.out.println(geogCS.getEllipsoid().getCode());
//        System.out.println(geogCS.getPrimeMeridian().getCode());
//        System.out.println(geogCS.getUnitOfMeasure().getCode
        System.out.println("*** CSV ***");
        GTProjCS projCS0 = CSVEPSGFactory.getInstance().findProjCSByCode(2288);
        System.out.println(projCS0.getCode());
        System.out.println(projCS0.getName());
        System.out.println(projCS0.getCoordOpCode());
        System.out.println(projCS0.getCoordOpMethodCode());
        System.out.println(projCS0.getParameter1Code());
        System.out.println(projCS0.getParameterValueByCode(projCS0.getParameter1Code()));
        System.out.println(projCS0.getParameter2Code());
        System.out.println(projCS0.getParameterValueByCode(projCS0.getParameter2Code()));
        System.out.println(projCS0.getParameter3Code());
        System.out.println(projCS0.getParameterValueByCode(projCS0.getParameter3Code()));
        System.out.println(projCS0.getParameter4Code());
        System.out.println(projCS0.getParameterValueByCode(projCS0.getParameter4Code()));
        System.out.println(projCS0.getParameter5Code());
        System.out.println(projCS0.getParameterValueByCode(projCS0.getParameter5Code()));
        System.out.println(projCS0.getParameter6Code());
        System.out.println(projCS0.getParameterValueByCode(projCS0.getParameter6Code()));
        System.out.println(projCS0.getParameter7Code());
        System.out.println(projCS0.getParameterValueByCode(projCS0.getParameter7Code()));
        System.out.println("*** GeoTools ***");
        GTProjCS projCS1 = getInstance().findProjCSByCode(2288);
        System.out.println(projCS1.getCode());
        System.out.println(projCS1.getName());
        System.out.println(projCS1.getCoordOpCode());
        System.out.println(projCS1.getCoordOpMethodCode());
        System.out.println(projCS1.getParameter1Code());
        System.out.println(projCS1.getParameterValueByCode(projCS1.getParameter1Code()));
        System.out.println(projCS1.getParameter2Code());
        System.out.println(projCS1.getParameterValueByCode(projCS1.getParameter2Code()));
        System.out.println(projCS1.getParameter3Code());
        System.out.println(projCS1.getParameterValueByCode(projCS1.getParameter3Code()));
        System.out.println(projCS1.getParameter4Code());
        System.out.println(projCS1.getParameterValueByCode(projCS1.getParameter4Code()));
        System.out.println(projCS1.getParameter5Code());
        System.out.println(projCS1.getParameterValueByCode(projCS1.getParameter5Code()));
        System.out.println(projCS1.getParameter6Code());
        System.out.println(projCS1.getParameterValueByCode(projCS1.getParameter6Code()));
        System.out.println(projCS1.getParameter7Code());
        System.out.println(projCS1.getParameterValueByCode(projCS1.getParameter7Code()));
    }
}
