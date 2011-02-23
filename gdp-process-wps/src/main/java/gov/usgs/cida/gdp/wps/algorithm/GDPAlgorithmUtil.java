package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.wps.util.WCSUtil;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 *
 * @author tkunicki
 */
public abstract class GDPAlgorithmUtil extends AbstractSelfDescribingAlgorithm {

    public GDPAlgorithmUtil() {
            super();
    }
	
    public static FeatureCollection extractFeatureCollection(Map<String, List<IData>> input, String id) {
            GTVectorDataBinding vectorDataBinding = null;
            List<IData> iDataList = input.get(id);
            if (iDataList != null && iDataList.size() == 1) {
                    IData iData = iDataList.get(0);
                    if (iData instanceof GTVectorDataBinding) {
                            vectorDataBinding = (GTVectorDataBinding) iData;
                    }
            }
            return vectorDataBinding.getPayload();
    }

	public static List<String> extractStringList(Map<String, List<IData>> input, String id) {
		List<String> stringList = new ArrayList<String>();
		List<IData> iDataList = input.get(id);
		if (iDataList != null) {
			for(IData data : iDataList) {
				if (data instanceof LiteralStringBinding) {
					stringList.add(((LiteralStringBinding)data).getPayload());
				}
			}

		}
		return stringList;
	}

    public static List<Date> extractDateList(Map<String, List<IData>> input, String id) {
		List<Date> dateList = new ArrayList<Date>();
		List<IData> iDataList = input.get(id);
		if (iDataList != null) {
			for(IData data : iDataList) {
				if (data instanceof LiteralDateTimeBinding) {
					dateList.add(((LiteralDateTimeBinding)data).getPayload());
				}
			}

		}
		return dateList;
	}

    public static List<URI> extractURIList(Map<String, List<IData>> input, String id) {
		List<URI> dateList = new ArrayList<URI>();
		List<IData> iDataList = input.get(id);
		if (iDataList != null) {
			for(IData data : iDataList) {
				if (data instanceof LiteralAnyURIBinding) {
					dateList.add(((LiteralAnyURIBinding)data).getPayload());
				}
			}

		}
		return dateList;
	}

    public static String extractString(Map<String, List<IData>> input, String id) {
		List<String> stringList = extractStringList(input, id);
        if (stringList == null || stringList.size() < 1) {
            return null;
        }
        if (stringList.size() == 1) {
            return  stringList.get(0);
        }
		throw new RuntimeException("too many arguments for input id " + id);
	}

    public static Date extractDate(Map<String, List<IData>> input, String id) {
		List<Date> dateList = extractDateList(input, id);
        if (dateList == null || dateList.size() < 1) {
            return null;
        }
        if (dateList.size() == 1) {
            return  dateList.get(0);
        }
		throw new RuntimeException("too many arguments for input id " + id);
	}

    public static URI extractURI(Map<String, List<IData>> input, String id) {
		List<URI> uriList = extractURIList(input, id);
        if (uriList == null || uriList.size() < 1) {
            return null;
        }
        if (uriList.size() == 1) {
            return  uriList.get(0);
        }
		throw new RuntimeException("too many arguments for input id " + id);
	}

    public static GridDatatype generateGridDataType(URI datasetURI, String datasetId, ReferencedEnvelope featureBounds) {
        GridDatatype gridDatatype = null;
        try {
            FeatureDataset featureDataset = null;
            String featureDatasetScheme = datasetURI.getScheme();
            if ("dods".equals(datasetURI.getScheme())) {
                featureDataset = FeatureDatasetFactoryManager.open(
                        FeatureType.GRID,
                        datasetURI.toString(),
                        null,
                        new Formatter(System.err));
                if (featureDataset instanceof GridDataset) {
                    gridDatatype =  ((GridDataset)featureDataset).findGridDatatype(datasetId);
                } else {
                    throw new RuntimeException("Unable to open dataset at " + datasetURI + " with identifier " + datasetId);
                }
            } else if ("http".equals(featureDatasetScheme)) {
                File tiffFile = WCSUtil.generateTIFFFile(datasetURI, datasetId, featureBounds);
                featureDataset = FeatureDatasetFactoryManager.open(
                        FeatureType.GRID,
                        tiffFile.getCanonicalPath(),
                        null,
                        new Formatter(System.err));
                if (featureDataset instanceof GridDataset) {
                    gridDatatype = ((GridDataset)featureDataset).findGridDatatype("I0B0");
                } else {
                    throw new RuntimeException("Unable to open dataset at " + datasetURI + " with identifier " + datasetId);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return gridDatatype;
    }

    public static Range generateTimeRange(GridDatatype GridDatatype, Date timeStart, Date timeEnd) {
        CoordinateAxis1DTime timeAxis = GridDatatype.getCoordinateSystem().getTimeAxis1D();
        Range timeRange = null;
        if (timeAxis != null) {
            int timeStartIndex = timeStart != null ?
                timeAxis.findTimeIndexFromDate(timeStart) :
                0;
            int timeEndIndex = timeEnd != null ?
                timeAxis.findTimeIndexFromDate(timeEnd) :
                timeAxis.getShape(0) - 1;
            try {
                timeRange = new Range(timeStartIndex, timeEndIndex);
            } catch (InvalidRangeException e) {
                throw new RuntimeException("Unable to generate time range.", e);
            }
        }
        return timeRange;
    }

    public static <T extends Enum<T>> List<T> convertStringToEnumList(Class<T> enumType, List<String> stringList) {
        List<T> enumList = new ArrayList<T>();
        for (String string : stringList) {
            enumList.add(Enum.valueOf(enumType, string));
        }
        return enumList;
    }

    public static <T extends Enum<T>> String[] convertEnumToStringArray(Class<T> enumType) {
        String[] strings = null;
        T[] constants = enumType.getEnumConstants();
        if (constants != null && constants.length > 0) {
            strings = new String[constants.length];
            for (int index = 0; index < constants.length; ++index) {
                strings[index] = constants[index].name();
            }
        }
        return strings;
    }

}
