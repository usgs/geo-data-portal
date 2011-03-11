package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.measure.quantity.Angle;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.transform.AbstractMathTransform;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.crs.DefaultDerivedCRS;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.ReferenceSystem;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.ProjectionRect;
import ucar.unidata.util.Parameter;

/**
 *
 * @author tkunicki
 */
public class CRSUtility {

	public static CoordinateReferenceSystem getCRSFromGridDatatype(GridDatatype gd) {
		return getCRSFromGridCoordSystem(gd.getCoordinateSystem());
	}

	public static CoordinateReferenceSystem getCRSFromGridCoordSystem(GridCoordSystem gcs) {

		ProjectionImpl projection = gcs.getProjection();
		if (projection == null) {
			return DefaultGeographicCRS.WGS84;
		}
	
		Map<String, Parameter> parameterMap = new HashMap<String, Parameter>();
		for (Parameter p : projection.getProjectionParameters()) {
			parameterMap.put(p.getName(), p);
		}

		GeodeticDatum datum = generateGeodeticDatum(parameterMap);

		Parameter gridMappingNameParameter = parameterMap.get("grid_mapping_name");

		String gridMappingName = gridMappingNameParameter == null ?
				"LatLon" :
				gridMappingNameParameter.getStringValue();


		GeographicCRS geographicCRS = (datum == DefaultGeodeticDatum.WGS84) ?
				DefaultGeographicCRS.WGS84 :
				new DefaultGeographicCRS(
					"CF-Derived Geographic CRS",
					datum,
					DefaultEllipsoidalCS.GEODETIC_2D);

		if ("LatLon".equals(gridMappingName) || "longitude_latitude".equals(gridMappingName)) {

            ProjectionRect projectionRect = gcs.getBoundingBox();
            boolean longitude360 = projectionRect.getMaxX() > 180;
            if (longitude360) {
                
                Map<String, Object> crsPoperties = new HashMap<String, Object>();
                
                crsPoperties.put(IdentifiedObject.NAME_KEY, "CRS LON [0,360]");

                ExtentImpl extent = new ExtentImpl();
                List<GeographicExtent> extentList = new ArrayList<GeographicExtent>();
                extentList.add(new GeographicBoundingBoxImpl(0, 360, -90, 90));
                extent.setGeographicElements(extentList);
                crsPoperties.put(ReferenceSystem.DOMAIN_OF_VALIDITY_KEY, extent);

                return new DefaultDerivedCRS(
                    "CF-Derived CRS LON [0,360]",
                    geographicCRS,
                    LongitudeDegreesTransform,
                    DefaultEllipsoidalCS.GEODETIC_2D);
            } else {
                return geographicCRS;
            }
		}

		try {

			DefaultMathTransformFactory transformFactory =
						new DefaultMathTransformFactory();

			ParameterValueGroup parameterGroup = null;
		
			if ("albers_conical_equal_area".equals(gridMappingName)) {

				parameterGroup =
						transformFactory.getDefaultParameters("Albers_Conic_Equal_Area");
				ParameterAdapter adapter = new ParameterAdapter(parameterMap, parameterGroup);
				adapter.transferNumeric("longitude_of_central_meridian", "longitude_of_center");
				adapter.transferNumeric("latitude_of_projection_origin", "latitude_of_center");
				adapter.transferNumeric("false_easting", "false_easting");
				adapter.transferNumeric("false_northing", "false_northing");
				adapter.transferStandardParallels();
				adapter.transferEllipsoid(datum.getEllipsoid());

			} else if ("azimuthal_equidistant".equals(gridMappingName)) {

				throw new RuntimeException("azimuthal_equidistant projection is not supported");

			} else if ("lambert_azimuthal_equal_area".equals(gridMappingName)) {

				parameterGroup =
						transformFactory.getDefaultParameters("Lambert_Azimuthal_Equal_Area");
				ParameterAdapter adapter = new ParameterAdapter(parameterMap, parameterGroup);
				adapter.transferNumeric("longitude_of_projection_origin", "longitude_of_center");
				adapter.transferNumeric("latitude_of_projection_origin", "latitude_of_center");
				adapter.transferNumeric("false_easting", "false_easting");
				adapter.transferNumeric("false_northing", "false_northing");
				adapter.transferEllipsoid(datum.getEllipsoid());

			} else if ("lambert_conformal_conic".equals(gridMappingName)) {

				parameterGroup =
						transformFactory.getDefaultParameters("Lambert_Conformal_Conic_2SP");
				ParameterAdapter adapter = new ParameterAdapter(parameterMap, parameterGroup);
				adapter.transferNumeric("longitude_of_central_meridian", "central_meridian");
				adapter.transferNumeric("latitude_of_projection_origin", "latitude_of_origin");
				adapter.transferNumeric("false_easting", "false_easting");
				adapter.transferNumeric("false_northing", "false_northing");
				adapter.transferStandardParallels();
				adapter.transferEllipsoid(datum.getEllipsoid());

			} else if ("lambert_cylindrical_equal_area".equals(gridMappingName)) {

				throw new RuntimeException("lambert_cylindrical_equal_area projection is not supported");

			} else if ("mercator".equals(gridMappingName)) {

				// presence of "standard_parallel" indicates Mercator_2SP while
				// "scale_factor_at_projection_origin" indicates Mercator_1SP
				parameterGroup = parameterMap.containsKey("standard_parallel") ?
						transformFactory.getDefaultParameters("Mercator_2SP") :
						transformFactory.getDefaultParameters("Mercator_1SP");
				ParameterAdapter adapter = new ParameterAdapter(parameterMap, parameterGroup);
				adapter.transferNumeric("longitude_of_projection_origin", "central_meridian");
				// Mercator_1SP only
				adapter.transferNumeric("scale_factor_at_projection_origin", "scale_factor");
				adapter.transferNumeric("false_easting", "false_easting");
				adapter.transferNumeric("false_northing", "false_northing");
				// Mercator_2SP only, only expect one standard parallel to exists
				adapter.transferStandardParallels();
				adapter.transferEllipsoid(datum.getEllipsoid());

			} else if ("orthographic".equals(gridMappingName)) {

				parameterGroup =
						transformFactory.getDefaultParameters("Orthographic");
				ParameterAdapter adapter = new ParameterAdapter(parameterMap, parameterGroup);
				adapter.transferNumeric("longitude_of_projection_origin", "central_meridian");
				adapter.transferNumeric("latitude_of_projection_origin", "latitude_of_origin");
				adapter.transferNumeric("false_easting", "false_easting");
				adapter.transferNumeric("false_northing", "false_northing");
				adapter.transferEllipsoid(datum.getEllipsoid());

			} else if ("polar_stereographic".equals(gridMappingName)) {

				parameterGroup =
						transformFactory.getDefaultParameters("Polar_Stereographic");
				ParameterAdapter adapter = new ParameterAdapter(parameterMap, parameterGroup);
				adapter.transferNumeric("straight_vertical_longitude_from_pole", "central_meridian");
				adapter.transferNumeric("latitude_of_projection_origin", "latitude_of_origin");
				adapter.transferNumeric("scale_factor_at_projection_origin", "scale_factor");
				adapter.transferNumeric("false_easting", "false_easting");
				adapter.transferNumeric("false_northing", "false_northing");
				adapter.transferStandardParallels();
				adapter.transferEllipsoid(datum.getEllipsoid());

			} else if ("rotated_latitude_longitude".equals(gridMappingName)) {

				throw new RuntimeException("rotated_latitude_longitude projection is not supported");

			} else if ("stereographic".equals(gridMappingName)) {

				parameterGroup =
						transformFactory.getDefaultParameters("Stereographic");
				ParameterAdapter adapter = new ParameterAdapter(parameterMap, parameterGroup);
				adapter.transferNumeric("longitude_of_projection_origin", "central_meridian");
				adapter.transferNumeric("latitude_of_projection_origin", "latitude_of_origin");
				adapter.transferNumeric("scale_factor_at_projection_origin", "scale_factor");
				adapter.transferNumeric("false_easting", "false_easting");
				adapter.transferNumeric("false_northing", "false_northing");
				adapter.transferEllipsoid(datum.getEllipsoid());

			} else if ("transverse_mercator".equals(gridMappingName)) {

				parameterGroup =
						transformFactory.getDefaultParameters("Transverse_Mercator");
				ParameterAdapter adapter = new ParameterAdapter(parameterMap, parameterGroup);
				adapter.transferNumeric("scale_factor_at_central_meridian", "scale_factor");
				adapter.transferNumeric("longitude_of_central_meridian", "central_meridian");
				adapter.transferNumeric("latitude_of_projection_origin", "latitude_of_origin");
				adapter.transferNumeric("false_easting", "false_easting");
				adapter.transferNumeric("false_northing", "false_northing");
				adapter.transferEllipsoid(datum.getEllipsoid());

			} else if ("vertical_perspective".equals(gridMappingName)) {

				throw new RuntimeException("vertical_perspective projection is not supported");

			}

			if (parameterGroup == null) {
				throw new RuntimeException("unknown grid_mapping_name");
			}

			Unit axisUnit = generateAxisUnit(gcs);

			CartesianCS cartesianCS = DefaultCartesianCS.PROJECTED.usingUnit(axisUnit);

			/* DOESN'T WORK - settings axis units is not enough, must also perform operation
			 * in transform.  see below...
			MathTransform transform = transformFactory.createParameterizedTransform(parameterGroup);
			ProjectedCRS projectedCRS = new DefaultProjectedCRS(
					"CF-Derived Projected CRS",
					geographicCRS,
					transform,
					cartesianCS);
			 */

			/*  WORKAROUND - PART 1:  This call will generate ConcatenatedTransform
			 *  instance with ability to scale values as needed.
			 */
			MathTransform transform = transformFactory.createBaseToDerived(
						geographicCRS,
						parameterGroup,
						cartesianCS);

			/*  WORKAROUND - PART 2: Wrap ConcatenatedTransform instance so
			 *  that MathTransform.getParameterDescriptors() returns something
			 *  other than null...
			 */
			if (! (transform instanceof ConcatenatedTransform)) {
				throw new RuntimeException("Expected instance of ConcatenatedTransform");
			}
			MathTransform wrapped = new ConcatenatedTransformAdapter((ConcatenatedTransform)transform);

			/*  WORKAROUND - PART 3: Create ProjectedCRS instance around
			 *  wrapped ConcatenatedTransform instance with same axis used
			 *  to generate ConcatenatedTransform
			 */
			ProjectedCRS projectedCRS = new DefaultProjectedCRS(
				"CF-Derived Projected CRS",
				geographicCRS,
				wrapped,
				cartesianCS);

			return projectedCRS;

		} catch (NoSuchIdentifierException e) {
			throw new RuntimeException(e);
		} catch (FactoryException e) {
			throw new RuntimeException(e);
		}
	}

	private static Unit generateAxisUnit(GridCoordSystem gcs) {
		CoordinateAxis xAxis = gcs.getXHorizAxis();
		CoordinateAxis yAxis = gcs.getYHorizAxis();
		String xUnits = xAxis.getUnitsString();
		String yUnits = yAxis.getUnitsString();
		if (xUnits == null || yUnits == null) {
			throw new RuntimeException("one or more axes missing units.");
		}
		if (!xUnits.equals(yUnits)) {
			throw new RuntimeException("axis units mismatch.");
		}
		return Unit.valueOf(xUnits);
	}

	private static DefaultEllipsoid generateEllipsoid(Map<String, Parameter> parameterMap) {
		Parameter earthRadiusParameter = parameterMap.get("earth_radius");
		Parameter semiMajorAxisParameter =  parameterMap.get("semi_major_axis");
		Parameter semiMinorAxisParameter = parameterMap.get("semi_minor_axis");
		Parameter inverseFlatteningParameter = parameterMap.get("inverse_flattening");

		double earthRadius = earthRadiusParameter == null ? Double.NaN : earthRadiusParameter.getNumericValue();
		double semiMajorAxis = semiMajorAxisParameter == null ? Double.NaN : semiMajorAxisParameter.getNumericValue();
		double semiMinorAxis = semiMinorAxisParameter == null ? Double.NaN : semiMinorAxisParameter.getNumericValue();
		double inverseFlattening = inverseFlatteningParameter == null ? Double.NaN : inverseFlatteningParameter.getNumericValue();

		if (earthRadius == earthRadius) {
			return DefaultEllipsoid.createEllipsoid("CF-Derived Sphere", earthRadius, earthRadius, SI.METER);
		}
		if (semiMajorAxis == semiMajorAxis && semiMinorAxis == semiMinorAxis) {
			return DefaultEllipsoid.createEllipsoid("CF-Derived Ellipsoid", semiMajorAxis, semiMinorAxis, SI.METER);
		}
		if (semiMajorAxis == semiMajorAxis && inverseFlattening == inverseFlattening) {
			return DefaultEllipsoid.createFlattenedSphere("CF-Derived Ellipsoid", semiMajorAxis, inverseFlattening, SI.METER);
		}
		if (semiMinorAxis == semiMinorAxis && inverseFlattening == inverseFlattening) {
			semiMajorAxis = semiMinorAxis / ( 1d - 1d / inverseFlattening );
			return DefaultEllipsoid.createEllipsoid("CF-Derived Ellipsoid", semiMajorAxis, semiMinorAxis, SI.METER);
		}
		return DefaultEllipsoid.WGS84;
	}

	private static DefaultPrimeMeridian generatePrimeMeridian(Map<String, Parameter> parameterMap) {
		Parameter primeMeridianParameter =  parameterMap.get("longitude_of_prime_meridian");
		double primeMeridian = primeMeridianParameter == null ? Double.NaN : primeMeridianParameter.getNumericValue();
		if (primeMeridian == primeMeridian) {
			return new DefaultPrimeMeridian("CF-Derived Prime Meridian", primeMeridian, Angle.UNIT);
		}
		return DefaultPrimeMeridian.GREENWICH;
	}

	private static DefaultGeodeticDatum generateGeodeticDatum(Map<String, Parameter> parameterMap) {
		DefaultEllipsoid ellipsoid = generateEllipsoid(parameterMap);
		DefaultPrimeMeridian primeMeridian = generatePrimeMeridian(parameterMap);
		if (ellipsoid == DefaultEllipsoid.WGS84 && primeMeridian == DefaultPrimeMeridian.GREENWICH) {
			return DefaultGeodeticDatum.WGS84;
		}
		return new DefaultGeodeticDatum(
				"CF-Derived Datum",
				generateEllipsoid(parameterMap),
				generatePrimeMeridian(parameterMap));
	}

	private static class ParameterAdapter {

		private Map<String, Parameter> parameterMap;
		private ParameterValueGroup parameterValueGroup;

		public ParameterAdapter(
				Map<String, Parameter> parameterMap,
				ParameterValueGroup parameterValueGroup) {
			this.parameterMap = parameterMap;
			this.parameterValueGroup = parameterValueGroup;
		}

		public void transferNumeric(String in, String out) {
			Parameter parameter = parameterMap.get(in);
			if (parameter != null) {
				double value = parameter.getNumericValue();
				parameterValueGroup.parameter(out).setValue(value);
			}
		}

		public void transferStandardParallels() {
			Parameter parameter = parameterMap.get("standard_parallel");
			if (parameter != null) {
				double[] values = parameter.getNumericValues();
				parameterValueGroup.parameter("standard_parallel_1").setValue(values[0]);
				if (values.length > 1) {
					parameterValueGroup.parameter("standard_parallel_2").setValue(values[1]);
				}
			}
		}

		public void transferEllipsoid(Ellipsoid ellipsoid) {
			if (ellipsoid != null) {
				parameterValueGroup.parameter("semi_major").setValue(ellipsoid.getSemiMajorAxis());
				parameterValueGroup.parameter("semi_minor").setValue(ellipsoid.getSemiMinorAxis());
			}
		}

	}

    private final static ParameterDescriptorGroup EMPTY_PARAMETER_DESCRIPTORS =
				new DefaultParameterDescriptorGroup("", new GeneralParameterDescriptor[0] );

	private static class ConcatenatedTransformAdapter extends ConcatenatedTransform {

		public ConcatenatedTransformAdapter(ConcatenatedTransform delegate) {
			super(delegate.transform1, delegate.transform2);
		}

		@Override
		public ParameterDescriptorGroup getParameterDescriptors() {
			if (transform1 instanceof AbstractMathTransform) {
				// seems to preserve WKT produced by ProjectedCRS, but is it valid???
				return ((AbstractMathTransform)transform1).getParameterDescriptors();
			}
			return EMPTY_PARAMETER_DESCRIPTORS;
		}
	}

    // Transform longitude from [-180...180] to [0...360]
    private final static MathTransform LongitudeDegreesTransform = new AbstractMathTransform() {

        @Override public int getSourceDimensions() { return 2; }
        @Override public int getTargetDimensions() { return 2; }

        @Override
        public ParameterDescriptorGroup getParameterDescriptors() {
			return EMPTY_PARAMETER_DESCRIPTORS;
		}

        @Override
        public MathTransform inverse() throws NoninvertibleTransformException {
            return LongitudeDegreesInverseTransform;
        }

        @Override
        public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) throws TransformException {
            int count = numPts * 2;
            for (int index = 0; index < count; index += 2) {
                double lon = srcPts[srcOff + index];
                dstPts[dstOff + index] = 180.0 + Math.IEEEremainder(lon - 180.0, 360.0);
                dstPts[dstOff + index + 1] = srcPts[srcOff + index + 1];
            }
        }
    };

    // Transform longitude from [0...360] to [-180...180]
    private final static MathTransform LongitudeDegreesInverseTransform = new AbstractMathTransform() {

        @Override public int getSourceDimensions() { return 2; }
        @Override public int getTargetDimensions() { return 2; }

        @Override
		public ParameterDescriptorGroup getParameterDescriptors() {
			return EMPTY_PARAMETER_DESCRIPTORS;
		}

        @Override
        public MathTransform inverse() throws NoninvertibleTransformException {
            return LongitudeDegreesTransform;
        }

        @Override
        public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) throws TransformException {
            int count = numPts * 2;
            for (int index = srcOff; index < count; index += 2) {
                double lon = srcPts[srcOff + index];
                dstPts[dstOff + index] = Math.IEEEremainder(lon + 180.0, 360.0) - 180.0;
                dstPts[dstOff + index + 1] = srcPts[srcOff + index + 1];
            }
        }
    };

}
