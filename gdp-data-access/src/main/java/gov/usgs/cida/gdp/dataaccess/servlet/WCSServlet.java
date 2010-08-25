package gov.usgs.cida.gdp.dataaccess.servlet;


import gov.usgs.cida.gdp.dataaccess.CoverageMetaData;
import gov.usgs.cida.gdp.dataaccess.bean.WCSCoverageInfoBean;
import gov.usgs.cida.gdp.utilities.XmlUtils;
import gov.usgs.cida.gdp.utilities.bean.AckBean;
import gov.usgs.cida.gdp.utilities.bean.AvailableFilesBean;
import gov.usgs.cida.gdp.utilities.bean.ErrorBean;
import gov.usgs.cida.gdp.utilities.bean.ShapeFileSetBean;
import gov.usgs.cida.gdp.utilities.bean.XmlReplyBean;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.TransformException;

/**
 * Servlet implementation class FileSelectionServlet
 */
public class WCSServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int MAX_COVERAGE_SIZE = 64 << 20; // 64 MB
	
	private static org.slf4j.Logger log = LoggerFactory.getLogger(WCSServlet.class);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WCSServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String command = request.getParameter("command");
		
		if ("calculatewcscoverageinfo".equals(command)) {
			String shapefileName = request.getParameter("shapefile");
            String crs = request.getParameter("crs");
            String lowerCorner = request.getParameter("lowercorner");
            String upperCorner = request.getParameter("uppercorner");
            String gridOffsets = request.getParameter("gridoffsets");
            String dataTypeString = request.getParameter("datatype");
            
            String shapefilePath = "";//CookieHelper.getShapefilePath(request, shapefileName);
            // Set up the shapefile
            String appTempDir = System.getProperty("applicationTempDir");
            String userDir = System.getProperty("applicationUserSpaceDir");

            AvailableFilesBean afb = AvailableFilesBean.getAvailableFilesBean(appTempDir, userDir);
            List<ShapeFileSetBean> shapeBeanList = afb.getShapeSetList();
            File shapeFile = null;
            for (ShapeFileSetBean sfsb : shapeBeanList) {
                if (shapefileName.equals(sfsb.getName())) {
                    shapeFile = sfsb.getShapeFile();
                    shapefilePath = shapeFile.getAbsolutePath();
                }
            }
            
            FileDataStore shapeFileDataStore;
    		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = null;
    		
            shapeFileDataStore = FileDataStoreFinder.getDataStore(new File(shapefilePath));
    		featureSource = shapeFileDataStore.getFeatureSource();
    		
    		String lowerCornerNums[] = lowerCorner.split(" ");
			String upperCornerNums[] = upperCorner.split(" ");
			double x1 = Double.parseDouble(lowerCornerNums[0]);
			double y1 = Double.parseDouble(lowerCornerNums[1]);
			double x2 = Double.parseDouble(upperCornerNums[0]);
			double y2 = Double.parseDouble(upperCornerNums[1]);

    		ReferencedEnvelope gridBounds, featureXBounds;
    		CoordinateReferenceSystem gridCRS;
    		
			try {

				gridCRS = CRS.decode(crs);
                AxisDirection ad0 = gridCRS.getCoordinateSystem().getAxis(0).getDirection();
                AxisDirection ad1 = gridCRS.getCoordinateSystem().getAxis(1).getDirection();
                boolean swapXY =
                        (ad0 == AxisDirection.NORTH || ad0 == AxisDirection.SOUTH) &&
                        (ad1 == AxisDirection.EAST  || ad1 == AxisDirection.WEST);

                gridBounds = swapXY ?
                    new ReferencedEnvelope(y1, y2, x1, x2, gridCRS) :
                    new ReferencedEnvelope(x1, x2, y1, y2, gridCRS);

                featureXBounds =
					featureSource.getBounds().transform(gridCRS, true);

			} catch (TransformException e1) {
				sendErrorReply(response, ErrorBean.ERR_CANNOT_COMPARE_GRID_AND_GEOM);
				return;
			} catch (FactoryException e1) {
				sendErrorReply(response, ErrorBean.ERR_CANNOT_COMPARE_GRID_AND_GEOM);
				return;
			}
			
			boolean fullyCovers;
			int minResamplingFactor;
			String units, boundingBox;
    		
    		// Explicitly cast to BoundingBox because there are
			// ambiguous 'contains' methods
    		if (!gridBounds.contains((BoundingBox) featureXBounds)) {
    			fullyCovers = false;
    		} else {
    			fullyCovers = true;
    		}
    		

    		/////// Estimate size of coverage request /////////
    		
    		String gridOffsetNums[] = gridOffsets.split(" ");
			double xOffset = Math.abs(Double.parseDouble(gridOffsetNums[0]));
			double yOffset = Math.abs(Double.parseDouble(gridOffsetNums[1]));
			
			// Size of data type in bytes
			int dataTypeSize;
			
			// We can't find the spec for what possible data types exist, so...
			// we have to check, and default to the max size (8) if we come
			// across an unrecognized type
            CoverageMetaData.DataType dataType = CoverageMetaData.findCoverageDataType(dataTypeString);
            if (dataType == CoverageMetaData.UnknownDataType) {
				log.info("Unrecognized wcs data type: " + dataType);
				dataTypeSize = 8;
            } else {
                dataTypeSize = dataType.getSizeBytes();
            }
    		
			double size = (featureXBounds.getHeight() / yOffset) *
						  (featureXBounds.getWidth()  / xOffset) *
						  dataTypeSize;
			
			if (size > MAX_COVERAGE_SIZE) {
				float factor = (float) size / MAX_COVERAGE_SIZE;
				
				minResamplingFactor = (int) Math.round(Math.ceil(factor));
			} else {
				minResamplingFactor = 1; // Coverage size is ok as is
			}
			
			units = "blah";
			boundingBox = Double.toString(featureXBounds.getMinX()) + "," +
						  Double.toString(featureXBounds.getMinY()) + "," +
						  Double.toString(featureXBounds.getMaxX()) + "," +
						  Double.toString(featureXBounds.getMaxY());
			
			
			WCSCoverageInfoBean bean = new WCSCoverageInfoBean(minResamplingFactor,
					fullyCovers, units, boundingBox);
			
			sendWCSInfoReply(response, bean);
		}
	}
	
	void sendErrorReply(HttpServletResponse response, int error) throws IOException {
		XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(error));
		XmlUtils.sendXml(xmlReply, new Date().getTime(), response);
	}
	
	void sendWCSInfoReply(HttpServletResponse response, WCSCoverageInfoBean bean) throws IOException {
		XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_OK, bean);
		XmlUtils.sendXml(xmlReply, new Date().getTime(), response);
	}
}
