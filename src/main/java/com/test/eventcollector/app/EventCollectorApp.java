package com.test.eventcollector.app;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.RolloverFileOutputStream;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.servlet.ServletContainer;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.servlets.MetricsServlet;
import com.test.eventcollector.rest.resource.LogCollectorRestResourceConfig;

public final class EventCollectorApp {
	private static final Logger LOG = Log.getLogger(EventCollectorApp.class);

	private Server server;

	private Properties props;

	private int serverPort;
	private String appName;

	int idleTimeout = 12_000;
	String workResultsIngesterUrl = null;
	String logFile = null;
	int acceptQueueSize = 100;
	int logRetainDays = 30;
	int connThreadPoolSize = 400;
	boolean startDispatcher = false;
	boolean startGraphite = false;
	int watchdogSleepTime = 60_000;

	private GraphiteReporter graphiteReporter = null;

	public void setup() {
		long now = System.currentTimeMillis();
		LogCollectorRestResourceConfig resourceConfig = new LogCollectorRestResourceConfig();
		props = LogCollectorRestResourceConfig.properties();

		serverPort = getIntProperty("app.port");
		appName = props.getProperty("app.name");
		idleTimeout = getIntProperty("app.idle-timeout");
		logFile = props.getProperty("app.log-file");
		acceptQueueSize = getIntProperty("app.accept-queue-size");
		connThreadPoolSize = getIntProperty("app.connect-pool-size");
		logRetainDays = getIntProperty("app.log-file-retain-days");
		startDispatcher = getBooleanProperty("app.start-dispatcher");
		startGraphite = getBooleanProperty("app.start-graphite");

		workResultsIngesterUrl = props.getProperty("app.work-results-ingester");

		if (!StringUtils.isBlank(logFile))
			try {
				// We are configuring a RolloverFileOutputStream with file name pattern and
				// appending property
				RolloverFileOutputStream os;
				os = new RolloverFileOutputStream(logFile, true, logRetainDays);

				// We are creating a print stream based on our RolloverFileOutputStream
				PrintStream logStream = new PrintStream(os);

				// We are redirecting system out and system error to our print stream.
				System.setOut(logStream);
				System.setErr(logStream);

			} catch (IOException e) {
				e.printStackTrace();
			}
 
		LOG.info("Creating conn pool with size: " + connThreadPoolSize);
		ThreadPool threadPool = new QueuedThreadPool(connThreadPoolSize);
		server = new Server(threadPool);
		LOG.info("Sever created with conn pool size: " + connThreadPoolSize);

		ServerConnector connector = new ServerConnector(server);
		connector.setIdleTimeout(idleTimeout);

		connector.setAcceptQueueSize(acceptQueueSize);
		connector.setPort(serverPort);

		server.addConnector(connector);

		HandlerCollection handlers = new HandlerCollection();

		// here we will hold start page for our web service, should it have to serve
		// some public pages (index.html with some basic info for beginning)
		String webDir = EventCollectorApp.class.getClassLoader().getResource("webapp").toExternalForm();
		WebAppContext webApp = new WebAppContext();
		webApp.setResourceBase(webDir);
		webApp.setContextPath("/" + appName + "/html");
		webApp.setServer(server);
		LOG.info("static contents to be served from directory: " + webDir);

		handlers.addHandler(webApp);

		ServletContextHandler statsServlet = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		statsServlet.setContextPath("/" + appName + "/stats");

		statsServlet.addServlet(new ServletHolder(new MetricsServlet(LogCollectorRestResourceConfig.metrics())), "/*");
		handlers.addHandler(statsServlet);

		// rest services will live under '/' and all classes serving them will
		// be in 'resource' package holding 'v1' prefix with them so that we can build
		// 'v2' later
		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		handler.setContextPath("/" + appName);

		handler.addServlet(new ServletHolder(new ServletContainer(resourceConfig)), "/*");

		handlers.addHandler(handler);
		server.setHandler(handlers);

		LOG.info("Server instantiated, number of connection threads: " + connThreadPoolSize + " Accept queue size: "
				+ acceptQueueSize + " lasted ms: " + (System.currentTimeMillis() - now));
  
		if (startGraphite) {
			startGraphite(resourceConfig);
		}

	}// setup

	private void startGraphite(LogCollectorRestResourceConfig resourceConfig) {
		MetricRegistry registry = LogCollectorRestResourceConfig.metrics();
		Properties properties = LogCollectorRestResourceConfig.properties();

		graphiteReporter = resourceConfig.graphiteReporting(registry, properties);

		graphiteReporter.start(1, TimeUnit.MINUTES);

		LOG.info("Graphite reporting started for registry " + registry.toString() + " reporter: " + graphiteReporter);
	}

	public void start() throws Exception {
		server.start();
		server.dump(System.out);
		// messageReceiver.start();

		LOG.info("Log collection Server started, listening on port " + serverPort + " App context: /" + appName);
		server.join();
	}

	public void finalize() {

		try {
			if (server != null) {
				server.stop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (graphiteReporter != null) {
			graphiteReporter.report();
			graphiteReporter.stop();
			graphiteReporter = null;
		}

		server = null;
	}

	public static void main(String args[]) throws Exception {

		EventCollectorApp theServer = new EventCollectorApp();
		theServer.setup();
		theServer.start();

		LOG.info("EventCollector Server finished ...");
	}
 
	public final static String userConfigDirectory() {
		String uHome = System.getProperty("user.home");
		String sep = System.getProperty("file.separator");
		String dir = uHome + sep + ".lgc";

		return dir;
	}

	private int getIntProperty(String key) {
		return Integer.parseInt(props.getProperty(key));
	}

	private boolean getBooleanProperty(String key) {
		return Boolean.parseBoolean(props.getProperty(key));
	}
}