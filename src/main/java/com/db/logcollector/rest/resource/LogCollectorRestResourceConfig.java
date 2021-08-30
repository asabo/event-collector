package com.db.logcollector.rest.resource;

import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BeanParam;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.PickledGraphite;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.db.logcollector.manager.EventManager;
import com.db.logcollector.manager.FileEventManagerImpl;
import com.db.logcollector.util.PropertiesReader;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *
 * @author ante
 */
public final class LogCollectorRestResourceConfig extends ResourceConfig {
	private static final Logger LOG = Log.getLogger(LogCollectorRestResourceConfig.class);
	
	public LogCollectorRestResourceConfig() {

		// where the resource classes are

		packages("com.db.logcollector.rest.resource");

		setApplicationName("log-collector");
 
		register(new AbstractBinder() {
			@Override
			protected void configure() {
 
				bind(CustomObjectMapper.class).to(ObjectMapper.class).in(Singleton.class);
			  
				bind(FileEventManagerImpl.class).to(EventManager.class).in(Singleton.class);
				bind(properties()).to(Properties.class);
				bind(metrics()).to(MetricRegistry.class);
				bind(healthChecks()).to(HealthCheckRegistry.class);
				bind(executorService()).to(ExecutorService.class);
				bind(schedulerService()).to(ScheduledExecutorService.class);
			}
		});
		
	}

	@BeanParam
	@Resource
	@Inject
	@Singleton
	public CustomObjectMapper jacksonObjectMapper() {
		return new CustomObjectMapper();
	}

	  
	private int getIntProperty(String key) {
		return Integer.parseInt(props.getProperty(key));
	}
	
	private boolean getBooleanProperty(String key) {
		return Boolean.parseBoolean(props.getProperty(key));
	}
	
	private static Properties props = null; 
	
	@Resource
	@Singleton
	public static final Properties properties() {
		if (props == null) {
		String propFileStr = "log-collector.config";
		props =  PropertiesReader.getInstance().getProperties(propFileStr);
		}
		
		return props;
	}
	
	private static ExecutorService service = Executors.newCachedThreadPool();
	
	@Resource
	@Singleton
	public static final ExecutorService executorService() {
		return service;
	}
	
	private static ScheduledExecutorService schedulerService = Executors.newScheduledThreadPool(1);
	
	@Resource
	@Singleton
	public static final ScheduledExecutorService schedulerService() {
		return schedulerService;
	}
	
	
    private static MetricRegistry metrics = null;
    
	@Resource
	@Singleton
	public static final MetricRegistry metrics() {
		if (metrics == null) {
		 metrics = new MetricRegistry();
		}
		
		return metrics;
	}
	
	public GraphiteReporter graphiteReporting(MetricRegistry registry, Properties props) {
		String graphiteHost = props.getProperty("app.graphite-server");
		int graphitePort = Integer.parseInt(props.getProperty("app.graphite-port"));
		String graphitePrefix = props.getProperty("app.graphite-prefix");
		
		final PickledGraphite graphite = new PickledGraphite(new InetSocketAddress(graphiteHost, graphitePort));
		LOG.info("Graphite instantiated on " + graphiteHost + ": " + graphitePort + " graphite: " + graphite);
	       
		final GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
		                                                  .prefixedWith(graphitePrefix)
		                                                  .convertRatesTo(TimeUnit.SECONDS)
		                                                  .convertDurationsTo(TimeUnit.MILLISECONDS)
		                                                  .filter(MetricFilter.ALL)
		                                                  .build(graphite);
		return reporter;
	}

	private static HealthCheckRegistry healthChecks = null;

	@Resource
	@Singleton
	public final HealthCheckRegistry healthChecks() {
		if (healthChecks == null) {
			healthChecks = new HealthCheckRegistry();
		}
		
		return healthChecks;
	}
	 
	@BeanParam
	public SerializationConfig serializationConfig() {
		return jacksonObjectMapper().getSerializationConfig();
	}

}

class CustomObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = -8464818847000721314L;

	public CustomObjectMapper() {
		this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		this.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		this.configure(SerializationFeature.INDENT_OUTPUT, true);
	}
}