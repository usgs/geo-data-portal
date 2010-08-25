package gov.usgs.cida.gdp.filemanagement.servlet;

import gov.usgs.cida.gdp.filemanagement.bean.AttributeBean;
import gov.usgs.cida.gdp.utilities.FileHelper;
import gov.usgs.cida.gdp.utilities.XmlUtils;
import gov.usgs.cida.gdp.utilities.bean.AckBean;
import gov.usgs.cida.gdp.utilities.bean.AvailableFilesBean;
import gov.usgs.cida.gdp.utilities.bean.ErrorBean;
import gov.usgs.cida.gdp.utilities.bean.FilesBean;
import gov.usgs.cida.gdp.utilities.bean.MessageBean;
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
 * Servlet implementation class FileSelectionServlet
 */
public class FileSelectionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static org.slf4j.Logger log = LoggerFactory.getLogger(FileSelectionServlet.class);


	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FileSelectionServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Long start = Long.valueOf(new Date().getTime());

		String command = (request.getParameter("command") == null) ? "" : request.getParameter("command");

		XmlReplyBean xmlReply = null;

		if ("listfiles".equals(command)) {
			String userDirectory = request.getParameter("userdirectory") == null ? "": request.getParameter("userdirectory");
			String userSpaceDir = System.getProperty("applicationUserSpaceDir");
			String tempDir = System.getProperty("applicationTempDir");
			// Test to see if the directory does exist. If so,
			// update the time on those files to today to escape the
			// timed deletion process
			if (userDirectory != null && !"".equals(userSpaceDir + userDirectory)) {
				if (FileHelper.doesDirectoryOrFileExist(userSpaceDir + userDirectory)) {
					FileHelper.updateTimestamp(userSpaceDir + userDirectory, false); // Update the timestamp
				} else {
					userDirectory = "";
				}
			}
			
			AvailableFilesBean afb = null;
			try {
				afb = AvailableFilesBean.getAvailableFilesBean(tempDir, userSpaceDir + userDirectory);
			} catch (IllegalArgumentException e) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_LIST, e));
				XmlUtils.sendXml(xmlReply, start, response);
				return;
			}

			if (afb == null || afb.getExampleFileList() == null
					|| afb.getExampleFileList().isEmpty()
					|| afb.getShapeSetList() == null
					|| afb.getShapeSetList().isEmpty()) {
				// Couldn't pull any files. Send an error to the caller.
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new MessageBean("Could not find any files to work with."));
				XmlUtils.sendXml(xmlReply, start, response);
				return;
			}

			xmlReply = new XmlReplyBean(AckBean.ACK_OK, afb);
			XmlUtils.sendXml(xmlReply, start, response);
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
}
