package gov.usgs.cida.gdp.dataaccess.servlet;

import gov.usgs.cida.gdp.dataaccess.WCSCoverageInfo;
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
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class FileSelectionServlet
 */
public class WCSServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
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
        Long start = Long.valueOf(new Date().getTime());
        String command = request.getParameter("command");

        if ("calculatewcscoverageinfo".equals(command)) {
            String shapefileName = request.getParameter("shapefile");
            String crs = request.getParameter("crs");
            String lowerCorner = request.getParameter("lowercorner");
            String upperCorner = request.getParameter("uppercorner");
            String gridOffsets = request.getParameter("gridoffsets");
            String dataTypeString = request.getParameter("datatype");

            String shapefilePath = "";
            // Set up the shapefile
            String appTempDir = System.getProperty("applicationTempDir");
            String userDir = System.getProperty("applicationUserSpaceDir");

            //TODO- Don't search through all shapefiles.
            AvailableFilesBean afb = AvailableFilesBean.getAvailableFilesBean(appTempDir, userDir);
            List<ShapeFileSetBean> shapeBeanList = afb.getShapeSetList();
            File shapeFile = null;
            for (ShapeFileSetBean sfsb : shapeBeanList) {
                if (shapefileName.equals(sfsb.getName())) {
                    shapeFile = sfsb.getShapeFile();
                    shapefilePath = shapeFile.getAbsolutePath();
                }
            }

            String lowerCornerNums[] = lowerCorner.split(" ");
            String upperCornerNums[] = upperCorner.split(" ");
            double x1 = Double.parseDouble(lowerCornerNums[0]);
            double y1 = Double.parseDouble(lowerCornerNums[1]);
            double x2 = Double.parseDouble(upperCornerNums[0]);
            double y2 = Double.parseDouble(upperCornerNums[1]);

            WCSCoverageInfoBean bean = null;
            try {
                bean = WCSCoverageInfo.calculateWCSCoverageInfo(shapefilePath, x1, y1, x2, y2, crs, gridOffsets, dataTypeString);
            } catch (NoSuchAuthorityCodeException ex) {
                LoggerFactory.getLogger(WCSServlet.class.getName()).error(null, ex);
                sendErrorReply(response, start, ErrorBean.ERR_CANNOT_COMPARE_GRID_AND_GEOM);
            } catch (FactoryException ex) {
                LoggerFactory.getLogger(WCSServlet.class.getName()).error(null, ex);
                sendErrorReply(response, start, ErrorBean.ERR_CANNOT_COMPARE_GRID_AND_GEOM);
            } catch (TransformException ex) {
                LoggerFactory.getLogger(WCSServlet.class.getName()).error(null, ex);
                sendErrorReply(response, start, ErrorBean.ERR_CANNOT_COMPARE_GRID_AND_GEOM);
            }

            sendWCSInfoReply(response, start, bean);
        }
    }

    void sendErrorReply(HttpServletResponse response, long start, int error) throws IOException {
        XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(error));
        XmlUtils.sendXml(xmlReply, start, response);
    }

    void sendWCSInfoReply(HttpServletResponse response, long start, WCSCoverageInfoBean bean) throws IOException {
        XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_OK, bean);
        XmlUtils.sendXml(xmlReply, start, response);
    }
}
