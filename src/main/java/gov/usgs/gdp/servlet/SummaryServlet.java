package gov.usgs.gdp.servlet;

import gov.usgs.cida.gdp.webapp.RouterServlet;
import gov.usgs.cida.gdp.webapp.bean.AckBean;
import gov.usgs.gdp.bean.CommandListBean;
import gov.usgs.cida.gdp.webapp.bean.MessageBean;
import gov.usgs.gdp.bean.ShapeFileSetBean;
import gov.usgs.gdp.bean.SummaryBean;
import gov.usgs.cida.gdp.webapp.bean.XmlReplyBean;
import gov.usgs.gdp.interfaces.geotools.AnalyzeFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class SummaryServlet
 */
public class SummaryServlet extends HttpServlet {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(SummaryServlet.class);
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SummaryServlet() {
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
		String action = (request.getParameter("action") == null) ? "" : request.getParameter("action").toLowerCase();
		String command = (request.getParameter("command") == null) ? "" : request.getParameter("command").toLowerCase();
		MessageBean errorBean = new MessageBean();
		MessageBean messageBean = new MessageBean();

		List<SummaryBean> summaryResults = null;
		if ("summarize".equals(action)) {
			summaryResults = summarize(request);
			if (!summaryResults.isEmpty()) {
				request.setAttribute("summaryBeanList", summaryResults);
				messageBean.addMessage("Selected files have been summarized.");
			} else {
				request.setAttribute("summaryBeanList", null);
				errorBean.addMessage("Unable to summarize selected files");
			}
		}

		if ("commandlist".equals(command)) {
			XmlReplyBean xmlReply = null;
			CommandListBean commandList = CommandListBean.getCommandListBean();
			xmlReply = new XmlReplyBean(AckBean.ACK_OK, commandList);
			RouterServlet.sendXml(xmlReply, start, response);
			return;
		}

		request.setAttribute("errorBean", errorBean);
		request.setAttribute("messageBean", messageBean);
		log.debug("Summary result");
		RequestDispatcher rd = request
				.getRequestDispatcher("/jsp/showSummaryResults.jsp");
		rd.forward(request, response);
	}

	/**
	 * Returns a List of type SummaryBean containing summaries of selected files
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<SummaryBean> summarize(HttpServletRequest request) {
		List<SummaryBean> result = new ArrayList<SummaryBean>();
		List<ShapeFileSetBean> shapeFileSetBeanList = new ArrayList<ShapeFileSetBean>();
		Object shapeFileSetBeanListInterim = request.getSession().getAttribute(
				"shapeFileSetBeanList");
		if (shapeFileSetBeanListInterim instanceof List<?>) {
			shapeFileSetBeanList = (List<ShapeFileSetBean>) shapeFileSetBeanListInterim;
		}

		String[] checkboxItems = request.getParameterValues("fileName");
		if (checkboxItems == null)
			return result;

		// For each set that the user wants to analyze
		for (String checkboxItem : checkboxItems) {
			ShapeFileSetBean workingShapeFileSet = null;

			// Pull the correct ShapeFileSetBean
			for (int sfsbCounter = 0; sfsbCounter < shapeFileSetBeanList.size(); sfsbCounter++) {
				String shapeFileSetBeanName = shapeFileSetBeanList.get(
						sfsbCounter).getName();
				if (shapeFileSetBeanName.toLowerCase().equals(
						checkboxItem.toLowerCase())) {
					workingShapeFileSet = shapeFileSetBeanList.get(sfsbCounter);
				}
			}

			// This should never happen but...
			if (workingShapeFileSet == null)
				return null;

			List<String> summaryResults = new ArrayList<String>();
			try {
				summaryResults = AnalyzeFile
						.getFileSummary(workingShapeFileSet);
			} catch (IOException e) {
				log.debug(e.getMessage());
			}

			SummaryBean summaryBean = new SummaryBean(checkboxItem,
					summaryResults);
			result.add(summaryBean);
		}
		return result;
	}

}
