package gov.usgs.gdp.servlet;

import gov.usgs.cida.gdp.webapp.RouterServlet;
import gov.usgs.cida.gdp.webapp.bean.AckBean;
import gov.usgs.gdp.bean.AvailableFilesBean;
import gov.usgs.cida.gdp.webapp.bean.ErrorBean;
import gov.usgs.cida.gdp.webapp.bean.MessageBean;
import gov.usgs.cida.gdp.webapp.bean.XmlReplyBean;
import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class FileSelectionServlet
 */
public class FileSelectionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
				RouterServlet.sendXml(xmlReply, start, response);
				return;
			}

			if (afb == null || afb.getExampleFileList() == null
					|| afb.getExampleFileList().isEmpty()
					|| afb.getShapeSetList() == null
					|| afb.getShapeSetList().isEmpty()) {
				// Couldn't pull any files. Send an error to the caller.
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new MessageBean("Could not find any files to work with."));
				RouterServlet.sendXml(xmlReply, start, response);
				return;
			}

			xmlReply = new XmlReplyBean(AckBean.ACK_OK, afb);
			RouterServlet.sendXml(xmlReply, start, response);
		}
	}
}
