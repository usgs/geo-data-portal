package gov.usgs.cida.gdp.wps.algorithm;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 *
 * @author tkunicki
 */
public abstract class BaseAlgorithm extends AbstractSelfDescribingAlgorithm {

    public BaseAlgorithm() {
            super();
    }
	
    protected FeatureCollection extractFeatureCollection(Map<String, List<IData>> input, String id) {
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

    protected FeatureDataset extractFeatureDataset(Map<String, List<IData>> input, String id) {
        String featureDatasetURIString = extractString(input, id);
        FeatureDataset featureDataset = null;
        try {
            URI featureDatasetURI = new URI(featureDatasetURIString);
            String featureDatasetScheme = featureDatasetURI.getScheme();
            if ("dods".equals(featureDatasetURI.getScheme())) {
                featureDataset = FeatureDatasetFactoryManager.open(
                        FeatureType.ANY,
                        featureDatasetURI.toString(),
                        null,
                        new Formatter(System.err));

            } else if ("http".equals(featureDatasetScheme)) {

            }
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return featureDataset;
    }

//    protected Date extractTime(Map<String, List<IData>> input, String id) {
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//        df.setTimeZone(TimeZone.getTimeZone("UTC"));
//        String timeString = extractString(input, id);
//        Date date = null;
//        if (timeString != null) {
//            try {
//                date = df.parse(timeString);
//            } catch (ParseException ex) {
//                throw new RuntimeException(ex);
//            }
//        }
//        return date;
//    }

	protected List<String> extractStringList(Map<String, List<IData>> input, String id) {
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

    protected List<Date> extractDateList(Map<String, List<IData>> input, String id) {
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

    protected List<URI> extractURIList(Map<String, List<IData>> input, String id) {
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

    protected String extractString(Map<String, List<IData>> input, String id) {
		List<String> stringList = extractStringList(input, id);
        if (stringList == null || stringList.size() < 1) {
            return null;
        }
        if (stringList.size() == 1) {
            return  stringList.get(0);
        }
		throw new RuntimeException("too many arguments for input id " + id);
	}

    protected Date extractDate(Map<String, List<IData>> input, String id) {
		List<Date> dateList = extractDateList(input, id);
        if (dateList == null || dateList.size() < 1) {
            return null;
        }
        if (dateList.size() == 1) {
            return  dateList.get(0);
        }
		throw new RuntimeException("too many arguments for input id " + id);
	}

    protected URI extractURI(Map<String, List<IData>> input, String id) {
		List<URI> uriList = extractURIList(input, id);
        if (uriList == null || uriList.size() < 1) {
            return null;
        }
        if (uriList.size() == 1) {
            return  uriList.get(0);
        }
		throw new RuntimeException("too many arguments for input id " + id);
	}

    protected Range generateTimeRange(GridDatatype GridDatatype, Date timeStart, Date timeEnd) {
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

}
