package gov.usgs.cida.gdp.wps.completion;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author jwalker
 */
public class CompletionContextListener implements ServletContextListener {

	private CheckProcessCompletion complete = null;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		complete = CheckProcessCompletion.getInstance();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (complete != null) complete.destroy();
	}
}
