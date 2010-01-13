package gov.usgs.gdp.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class FileUploadServlet
 */
public class FileUploadServlet extends HttpServlet {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(FileUploadServlet.class);
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FileUploadServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		BufferedInputStream buf = null;
		ServletOutputStream stream = null;
		try {
			stream = response.getOutputStream();
			String file = (request.getParameter("file") == null) ? "" : request.getParameter("file");
			File sendFile = new File(file);
			response.addHeader("Content-Disposition", "attachment; filename=" + file);
			response.setContentLength((int) sendFile.length());
			FileInputStream input = new FileInputStream(file);
			
			// read from the file; write to the ServletOutputStream
			int readBytes = 0;
			buf = new BufferedInputStream(input);
			while ((readBytes = buf.read()) != -1) stream.write(readBytes);
		} catch (IOException ioe) {
			log.debug(ioe.getMessage());
			throw new ServletException(ioe.getMessage());
		} finally {
			if (stream != null)
				stream.close();
			if (buf != null)
				buf.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
