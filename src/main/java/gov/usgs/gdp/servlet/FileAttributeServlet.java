package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.AckBean;
import gov.usgs.gdp.bean.AttributeBean;
import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.FilesBean;
import gov.usgs.gdp.bean.OutputFileTypeBean;
import gov.usgs.gdp.bean.ShapeFileSetBean;
import gov.usgs.gdp.bean.XmlReplyBean;
import gov.usgs.gdp.helper.CookieHelper;
import gov.usgs.gdp.helper.FileHelper;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class FileAttributeServlet
 */
public class FileAttributeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(FileAttributeServlet.class);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileAttributeServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String command = request.getParameter("command");
		XmlReplyBean xmlReply = null;
		
		if ("outputtypelist".equals(command)) {
			log.debug("User has chosen to get output file type list");
			List<String> availableFileTypes = FileHelper.getOutputFileTypesAvailable();
			if (availableFileTypes == null || availableFileTypes.isEmpty()) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_OUTFILES_UNAVAILABLE));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}
			
			OutputFileTypeBean oftb = new OutputFileTypeBean(availableFileTypes);
			xmlReply = new XmlReplyBean(AckBean.ACK_OK, oftb);
			RouterServlet.sendXml(xmlReply, response);
			return;
		}
		
		if ("listattributes".equals(command)) {
			log.debug("User has chosen to list shapefile attributes");
			String shapefile = request.getParameter("shapefile");
			Cookie userDirectoryCookie = CookieHelper.getCookie(request, "userDirectory");			
			String userDirectory = "";
			if (userDirectoryCookie != null) {
				if (FileHelper.doesDirectoryOrFileExist(userDirectoryCookie.getValue())) {
					userDirectory = userDirectoryCookie.getValue();
				}
			}
			
			List<FilesBean> filesBeanList = FilesBean.getFilesBeanSetList(System.getProperty("applicationTempDir"), userDirectory);
			if (filesBeanList == null) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_NOT_FOUND));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}
			ShapeFileSetBean shapeFileSetBean = ShapeFileSetBean.getShapeFileSetBeanFromFilesBeanList(filesBeanList, shapefile);
			
			List<String> attributeList = ShapeFileSetBean.getAttributeListFromBean(shapeFileSetBean);
			if (attributeList == null || attributeList.isEmpty()) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_ATTRIBUTES_NOT_FOUND));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}
			
			AttributeBean attributeBean = new AttributeBean(attributeList);
			attributeBean.setFilesetName(shapefile);
			xmlReply = new XmlReplyBean(AckBean.ACK_OK, attributeBean);
			RouterServlet.sendXml(xmlReply, response);
			return;
			
		}
		
	}

}
