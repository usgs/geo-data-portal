package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.AckBean;
import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.XmlReplyBean;
import gov.usgs.gdp.helper.CookieHelper;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
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
import org.opengis.referencing.operation.TransformException;

/**
 * Servlet implementation class FileSelectionServlet
 */
public class WCSServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int MAX_COVERAGE_SIZE = 100000000; // 100 MiB
	
	private static org.apache.log4j.Logger log = Logger.getLogger(WCSServlet.class);
       
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
            String dataType = request.getParameter("datatype");
            
            String shapefilePath = CookieHelper.getShapefilePath(request, shapefileName);
            
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

    		ReferencedEnvelope gridBounds, transformedShapefileBounds;
    		CoordinateReferenceSystem gridCRS;
    		
			try {
				gridCRS = CRS.decode(crs);
	    		gridBounds = new ReferencedEnvelope(y1, y2, x1, x2, gridCRS);
				
				transformedShapefileBounds = 
					featureSource.getBounds().transform(gridCRS, false);
			} catch (TransformException e1) {
				sendFailReply(response, "Unable to compare grid and geometry bounds");
				return;
			} catch (FactoryException e1) {
				sendFailReply(response, "Unable to compare grid and geometry bounds");
				return;
			}
    		
    		// Explicitly cast to BoundingBox because there are 
			// ambiguous 'contains' methods
    		if (!gridBounds.contains((BoundingBox) transformedShapefileBounds)) {
    			sendFailReply(response, "Grid does not fully cover geometry");
    			return;
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
			if      ("Float32".equals(dataType)) dataTypeSize = 4;
			//else if () {}
			else {
				log.info("Unrecognized wcs data type: " + dataType);
				dataTypeSize = 8;
			}
    		
			double size = (transformedShapefileBounds.getHeight() / yOffset) *
						  (transformedShapefileBounds.getWidth()  / xOffset) *
						  dataTypeSize;
			
			if (size > MAX_COVERAGE_SIZE) {
				float percent = (float) size / MAX_COVERAGE_SIZE * 100 - 100;
				String percentString = String.format("%1$.1f", percent);
				
				sendFailReply(response, "Coverage exceeds size limit by " + 
						percentString + "%");
			}
			
			sendOkReply(response);
		}
	}
	
	void sendFailReply(HttpServletResponse response, String message) throws IOException {
		XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(message));
		RouterServlet.sendXml(xmlReply, new Date().getTime(), response);
	}
	
	void sendOkReply(HttpServletResponse response) throws IOException {
		XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_OK);
		RouterServlet.sendXml(xmlReply, new Date().getTime(), response);
	}
}
