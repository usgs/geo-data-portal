package gov.usgs.cida.blacklist;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jwalker
 */
public class BlacklistServlet extends HttpServlet {

	private BlacklistInterface singleton = BlacklistFactory.getActiveBlacklist();
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
		String command = request.getParameter("command");
		if ("getblacklist".equalsIgnoreCase(command)) {
			printBlacklist(response);
			return;
		}
		else if("remove".equalsIgnoreCase(command)) {
			Map<String, String[]> map = request.getParameterMap();
			for (String str : map.keySet()) {
				if (map.get(str)[0].endsWith(";true")) {
					singleton.remove(str);
				}
			}
			printBlacklist(response);
			return;
		}
    }

	/**
	 * Used for management page, need to allow removal from blacklist
	 * @param response response object from calling servlet
	 * @throws IOException
	 */
	private void printBlacklist(HttpServletResponse response) throws IOException {
		Properties outputBlacklist = singleton.outputBlacklist();
		response.setContentType("text/xml");
		outputBlacklist.storeToXML(response.getOutputStream(), null);
		response.flushBuffer();
		return;
	}

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Handles blacklist management";
    }// </editor-fold>

}
