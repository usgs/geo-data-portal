package gov.usgs.cida.gdp.wps.completion;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author isuftin
 */
public class EmailCheckTaskTest {

	private Server server;

	@Before
	public void startTest() throws Exception {
		this.server = new Server(0);

		this.server.setHandler(new AbstractHandler() {
			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
				baseRequest.setHandled(true);
				response.setContentType("text/html;charset=utf8");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("I printed a line");
			}
		});

		this.server.start();
	}

	@After
	public void endTest() throws Exception {
		this.server.stop();
		this.server.join();
	}

	@Test
	public void errorOutCheckTask() throws Exception {
		int port = server.getConnectors()[0].getLocalPort();
		EmailCheckTask task = new EmailCheckTask("http://localhost:" + port + "/", "test@test.test.com", null, true, false, 5);
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(1));
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(2));
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(3));
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(4));
		task.run();
		assertTrue(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(5));
	}

	@Test
	public void neverErrorOutCheckTask() throws Exception {
		int port = server.getConnectors()[0].getLocalPort();
		EmailCheckTask task = new EmailCheckTask("http://localhost:" + port + "/", "test@test.test.com", null, true, false, -1);
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(0));
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(0));
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(0));
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(0));
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(0));
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(0));
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(0));
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(0));
		task.run();
		assertFalse(task.isCancelled());
		assertEquals(task.getErrorCount(), Integer.valueOf(0));
	}
}
