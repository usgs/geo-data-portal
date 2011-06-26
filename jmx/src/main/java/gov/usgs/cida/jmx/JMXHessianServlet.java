package gov.usgs.cida.jmx;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx4j.tools.remote.caucho.hessian.HessianServlet;

/**
 *
 * @author tkunicki
 */
public class JMXHessianServlet extends HessianServlet {
    
    private JMXConnectorServerRegistry serverRegistry;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        serverRegistry.initializeJMXConnectorServer(request);
        super.service(request, response);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        serverRegistry = new JMXConnectorServerRegistry("hessian");
    }
    
    @Override
    public void destroy() {
        super.destroy();
        serverRegistry.destory();
    }
    
}
