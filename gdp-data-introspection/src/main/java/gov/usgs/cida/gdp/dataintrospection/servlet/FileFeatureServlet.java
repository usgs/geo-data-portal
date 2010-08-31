package gov.usgs.cida.gdp.dataintrospection.servlet;

import gov.usgs.cida.gdp.dataintrospection.bean.Attribute;
import gov.usgs.cida.gdp.dataintrospection.bean.Feature;
import gov.usgs.cida.gdp.utilities.FileHelper;
import gov.usgs.cida.gdp.utilities.XmlUtils;
import gov.usgs.cida.gdp.utilities.bean.Acknowledgement;
import gov.usgs.cida.gdp.utilities.bean.Error;
import gov.usgs.cida.gdp.utilities.bean.Files;
import gov.usgs.cida.gdp.utilities.bean.ShapeFileSet;
import gov.usgs.cida.gdp.utilities.bean.XmlReply;
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
		XmlReply xmlReply = null;
		if ("listfeatures".equals(command)) {
			log.debug("User has chosen to list shapefile features");
			String shapefile = request.getParameter("shapefile");
			String attribute = request.getParameter("attribute");
			if (attribute == null || "".equals(attribute)
					|| shapefile == null || "".equals(shapefile)) {
				xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_MISSING_PARAM));
				XmlUtils.sendXml(xmlReply, start, response);
				return;
			}

			String userDirectory = request.getParameter("userdirectory");
			if (userDirectory != null && !"".equals(userDirectory)) {
				if (!FileHelper.doesDirectoryOrFileExist(userDirectory)) userDirectory = "";
			}

			List<Files> filesBeanList = Files.getFilesBeanSetList(System.getProperty("applicationTempDir"), userDirectory);
			if (filesBeanList == null) {
				xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_FILE_NOT_FOUND));
				XmlUtils.sendXml(xmlReply, start,response);
				return;
			}
			ShapeFileSet shapeFileSetBean = ShapeFileSet.getShapeFileSetBeanFromFilesBeanList(filesBeanList, shapefile);
			shapeFileSetBean.setChosenAttribute(attribute);
			// Pull Feature Lists
			List<String> features = null;
            try {
				features = ShapeFileSet.getFeatureListFromBean(shapeFileSetBean);
			} catch (IOException e) {
				xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_FEATURES_NOT_FOUND));
				XmlUtils.sendXml(xmlReply, start,response);
				return;
			}

			if (features != null && !features.isEmpty()) {
				Feature featureBean = new Feature(features);
				featureBean.setFilesetName(shapefile);
				xmlReply = new XmlReply(Acknowledgement.ACK_OK, featureBean);
				XmlUtils.sendXml(xmlReply, start,response);
				return;
			}
			xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_FEATURES_NOT_FOUND));
			XmlUtils.sendXml(xmlReply, start,response);
			return;
		}
        if ("listattributes".equals(command)) {
			log.debug("User has chosen to list shapefile attributes");
			String shapefile = request.getParameter("shapefile");
			String userDirectory = request.getParameter("userdirectory");

			if (userDirectory == null || !FileHelper.doesDirectoryOrFileExist(userDirectory)) userDirectory = "";

			List<Files> filesBeanList = Files.getFilesBeanSetList(System.getProperty("applicationTempDir"), userDirectory);
			if (filesBeanList == null) {
				xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_FILE_NOT_FOUND));
				XmlUtils.sendXml(xmlReply, start, response);
				return;
			}
			ShapeFileSet shapeFileSetBean = ShapeFileSet.getShapeFileSetBeanFromFilesBeanList(filesBeanList, shapefile);

			List<String> attributeList = ShapeFileSet.getAttributeListFromBean(shapeFileSetBean);
			if (attributeList == null || attributeList.isEmpty()) {
				xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_ATTRIBUTES_NOT_FOUND));
				XmlUtils.sendXml(xmlReply,start, response);
				return;
			}

			Attribute attributeBean = new Attribute(attributeList);
			attributeBean.setFilesetName(shapefile);
			xmlReply = new XmlReply(Acknowledgement.ACK_OK, attributeBean);
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
