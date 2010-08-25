package gov.usgs.cida.gdp.dataintrospection.servlet;

import gov.usgs.cida.gdp.dataintrospection.bean.AttributeBean;
import gov.usgs.cida.gdp.dataintrospection.bean.FeatureBean;
import gov.usgs.cida.gdp.utilities.FileHelper;
import gov.usgs.cida.gdp.utilities.XmlUtils;
import gov.usgs.cida.gdp.utilities.bean.AckBean;
import gov.usgs.cida.gdp.utilities.bean.ErrorBean;
import gov.usgs.cida.gdp.utilities.bean.FilesBean;
import gov.usgs.cida.gdp.utilities.bean.ShapeFileSetBean;
import gov.usgs.cida.gdp.utilities.bean.XmlReplyBean;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class FileFeatureServlet
 */
public class FileFeatureServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static org.slf4j.Logger log = LoggerFactory.getLogger(FileFeatureServlet.class);

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Long start = Long.valueOf(new Date().getTime());
		String command = request.getParameter("command");
		XmlReplyBean xmlReply = null;
		if ("listfeatures".equals(command)) {
			log.debug("User has chosen to list shapefile features");
			String shapefile = request.getParameter("shapefile");
			String attribute = request.getParameter("attribute");
			if (attribute == null || "".equals(attribute)
					|| shapefile == null || "".equals(shapefile)) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_MISSING_PARAM));
				XmlUtils.sendXml(xmlReply, start, response);
				return;
			}

			String userDirectory = request.getParameter("userdirectory");
			if (userDirectory != null && !"".equals(userDirectory)) {
				if (!FileHelper.doesDirectoryOrFileExist(userDirectory)) userDirectory = "";
			}

			List<FilesBean> filesBeanList = FilesBean.getFilesBeanSetList(System.getProperty("applicationTempDir"), userDirectory);
			if (filesBeanList == null) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_NOT_FOUND));
				XmlUtils.sendXml(xmlReply, start,response);
				return;
			}
			ShapeFileSetBean shapeFileSetBean = ShapeFileSetBean.getShapeFileSetBeanFromFilesBeanList(filesBeanList, shapefile);
			shapeFileSetBean.setChosenAttribute(attribute);
			// Pull Feature Lists
			List<String> features = null;
            try {
				features = ShapeFileSetBean.getFeatureListFromBean(shapeFileSetBean);
			} catch (IOException e) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FEATURES_NOT_FOUND));
				XmlUtils.sendXml(xmlReply, start,response);
				return;
			}

			if (features != null && !features.isEmpty()) {
				FeatureBean featureBean = new FeatureBean(features);
				featureBean.setFilesetName(shapefile);
				xmlReply = new XmlReplyBean(AckBean.ACK_OK, featureBean);
				XmlUtils.sendXml(xmlReply, start,response);
				return;
			}
			xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FEATURES_NOT_FOUND));
			XmlUtils.sendXml(xmlReply, start,response);
			return;
		}
        if ("listattributes".equals(command)) {
			log.debug("User has chosen to list shapefile attributes");
			String shapefile = request.getParameter("shapefile");
			String userDirectory = request.getParameter("userdirectory");

			if (userDirectory == null || !FileHelper.doesDirectoryOrFileExist(userDirectory)) userDirectory = "";

			List<FilesBean> filesBeanList = FilesBean.getFilesBeanSetList(System.getProperty("applicationTempDir"), userDirectory);
			if (filesBeanList == null) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_NOT_FOUND));
				XmlUtils.sendXml(xmlReply, start, response);
				return;
			}
			ShapeFileSetBean shapeFileSetBean = ShapeFileSetBean.getShapeFileSetBeanFromFilesBeanList(filesBeanList, shapefile);

			List<String> attributeList = ShapeFileSetBean.getAttributeListFromBean(shapeFileSetBean);
			if (attributeList == null || attributeList.isEmpty()) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_ATTRIBUTES_NOT_FOUND));
				XmlUtils.sendXml(xmlReply,start, response);
				return;
			}

			AttributeBean attributeBean = new AttributeBean(attributeList);
			attributeBean.setFilesetName(shapefile);
			xmlReply = new XmlReplyBean(AckBean.ACK_OK, attributeBean);
			XmlUtils.sendXml(xmlReply,start, response);
			return;
		}
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

}
