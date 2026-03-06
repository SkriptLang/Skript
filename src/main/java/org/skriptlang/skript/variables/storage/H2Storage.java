package org.skriptlang.skript.variables.storage;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.JdbcStorage;
import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * H2 storage for Skript variables.
 */
@SuppressWarnings("SqlSourceToSinkFlow")
public class H2Storage extends JdbcStorage {

	public H2Storage(SkriptAddon source, String type) {
		super(source, type);
	}

	@Override
	protected @Nullable HikariConfig configuration(SectionNode sectionNode) {
		if (file == null)
			return null;
		assert file.getName().endsWith(".mv.db");

		HikariConfig configuration = new HikariConfig();
		configuration.setPoolName("H2-Pool");
		configuration.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
		configuration.setConnectionTestQuery("VALUES 1");

		String url = "";
		if (sectionNode.get("memory", "false").equalsIgnoreCase("true"))
			url += "mem:";
		url += "file:" + file.getAbsolutePath();
		url = url.substring(0, url.length() - ".mv.db".length());

		String h2Settings = ";COMPRESS=TRUE" +
			";RETENTION_TIME=1000" +
			";CACHE_SIZE=32768" +
			";TRACE_LEVEL_FILE=0" +
			";TRACE_LEVEL_SYSTEM_OUT=0" +
			";DB_CLOSE_ON_EXIT=FALSE";

		configuration.addDataSourceProperty("URL", "jdbc:h2:" + url + h2Settings);
		configuration.addDataSourceProperty("user", sectionNode.get("user", ""));
		configuration.addDataSourceProperty("password", sectionNode.get("password", ""));
		configuration.addDataSourceProperty("description", sectionNode.get("description", ""));
		return configuration;
	}

	@Override
	protected boolean requiresFile() {
		return true;
	}

	@Override
	protected File getFile(String fileName) {
		if (!fileName.endsWith(".mv.db"))
			fileName = fileName + ".mv.db"; // H2 automatically appends '.mv.db' to the file from url
		return new File(fileName);
	}

	// language=H2
	@Override
	protected String createTableQuery() {
		return "CREATE TABLE IF NOT EXISTS " + table + " (" +
			"`name`         VARCHAR(" + MAX_VARIABLE_NAME_LENGTH + ")  NOT NULL  PRIMARY KEY," +
			"`type`         VARCHAR(" + MAX_CLASS_CODENAME_LENGTH + ")," +
			"`value`        BINARY LARGE OBJECT(" + MAX_VALUE_SIZE + ")" +
			");";
	}

	@Override
	protected PreparedStatement readSingleQuery(Connection connection) throws SQLException {
		return connection.prepareStatement("SELECT `type`, `value` FROM " + table + " WHERE `name` = ?");
	}

	@Override
	protected PreparedStatement readListQuery(Connection connection) throws SQLException {
		return connection.prepareStatement("SELECT `name`, `type`, `value` FROM " + table + " WHERE `name` LIKE ?");
	}

	@Override
	protected PreparedStatement writeSingleQuery(Connection connection) throws SQLException {
		return connection.prepareStatement("MERGE INTO " + table + " (`name`, `type`, `value`) KEY(`name`) VALUES (?, ?, ?)");
	}

	@Override
	protected PreparedStatement writeMultipleQuery(Connection connection) throws SQLException {
		return writeSingleQuery(connection);
	}

	@Override
	protected PreparedStatement deleteSingleQuery(Connection connection) throws SQLException {
		return connection.prepareStatement("DELETE FROM " + table + " WHERE `name` = ?");
	}

	@Override
	protected PreparedStatement deleteListQuery(Connection connection) throws SQLException {
		return connection.prepareStatement("DELETE FROM " + table + " WHERE `name` LIKE ?");
	}

	@Override
	protected void afterSave(Connection conn) throws SQLException {
		try (Statement stmt = conn.createStatement()) {
			stmt.execute("CHECKPOINT");
		}
	}

	@Override
	protected void closeDatabase() throws SQLException {
		if (database != null) {
			try (Connection conn = database.getConnection();
				 Statement stmt = conn.createStatement()) {
				stmt.execute("SHUTDOWN");
			} catch (SQLException exception) {
				// 90121: Database is already closed
				// This happens because we just executed SHUTDOWN, so the DB just closed
				// and try with resources tries to close the connection again
				// We can safely ignore this.
				if (exception.getErrorCode() != 90121) {
					throw exception;
				}
			}
			if (!database.isClosed())
				database.close();
		}
	}

}
