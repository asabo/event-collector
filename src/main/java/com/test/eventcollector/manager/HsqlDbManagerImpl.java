package com.test.eventcollector.manager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.test.eventcollector.app.dto.EventStats;

public class HsqlDbManagerImpl implements HsqlDbManager {
	private static final Logger LOG = Log.getLogger(HsqlDbManagerImpl.class);
	
	static {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
			LOG.warn("Exception registering hsqldb driver", e);
		}
	}

	private static boolean tableExists = false;

	@Inject
	public HsqlDbManagerImpl() {
		if (!tableExists) {
			try (Connection con = getDbConnection()) {
				tableExists = createTableIfNonExistent(con);
			} catch (SQLException e) {
				LOG.warn("Exception while trying to create table", e);
			}
		}
	}

	@Override
	public boolean storeEventStats(EventStats eventStats) {

		final String sql = "insert into events (id, duration, event_type, host, alert) values (?,?,?,?,?)";

		try (Connection conn = getDbConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, eventStats.getId());
			ps.setInt(2, eventStats.getDuration());
			ps.setString(3, eventStats.getType());
			ps.setString(4, eventStats.getHost());
			ps.setBoolean(5, eventStats.isAlert());

			int res = ps.executeUpdate();
			LOG.info("Event stats for event: " + eventStats.getId() + " successfully stored, got back: " + res);
		} catch (SQLException e) {
			LOG.warn("Exception while storing event data", e);
			return false;
		}

		return true;
	}
	
	@Override
	public EventStats getEventStats(String statsId) {

		final String sql = "select id, duration, event_type, host, alert from events where id = ?";

		try (Connection conn = getDbConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, statsId);

			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
				EventStats es = new EventStats();
				es.setId(rs.getString(1));
				es.setDuration(rs.getInt(2));
				es.setType(rs.getString(3));
				es.setHost(rs.getString(4));
				es.setAlert(rs.getBoolean(5));
				
				return es;
			} else 
				return null;
		} catch (SQLException e) {
			LOG.warn("Exception while storing event data", e);
			return null;
		}
	}

	private static Connection getDbConnection() {
		Connection connection = null;

		try {
			connection = DriverManager.getConnection("jdbc:hsqldb:file:eventCollector", "SA", "");
		} catch (SQLException e) {
			LOG.warn("Exception while getting JDBC connection", e);
		}

		return connection;
	}

	private boolean createTableIfNonExistent(Connection con) {

		ResultSet tables = null;
		PreparedStatement ps = null;
		try {
			DatabaseMetaData dbm = con.getMetaData();
			tables = dbm.getTables(null, null, "EVENTS", null);

			if (tables.next()) {
			 tableExists = true;
			} else {
				ps = con.prepareStatement(
						"CREATE TABLE EVENTS (ID VARCHAR(50) PRIMARY KEY, DURATION INT NOT NULL, EVENT_TYPE VARCHAR(50), HOST VARCHAR(50), ALERT BOOLEAN)");
				ps.executeUpdate();
				ps.close();
				LOG.info("Table successfully created");
			}
		} catch (SQLException e) {
			LOG.warn("Exception while creating table", e);
			return false;
		}  
		return true;
	}

}
