package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import org.skriptlang.skript.addon.SkriptAddon;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Saves variables to a table on a MySQL or MariaDB server. The driver comes with Paper, so nothing has to be downloaded.
 * <p>
 * Several servers can share one table, so this storage supports monitoring. While it monitors changes, deleted
 * variables are kept as tombstones for a while instead of being removed right away, each row records the server that
 * saved it, and rows are indexed by version, so servers watching the table can see each other's changes and deletions.
 * Without monitoring, the table is treated as this server's alone, which makes each saved row about a third cheaper
 * for the database. Sharing a table only shows other servers' changes eventually; it doesn't make variables safe to
 * change from several servers at once.
 * <p>
 * Names longer than {@value #MAX_NAME_LENGTH} characters can't be saved, since that's as long as an InnoDB key can be.
 * Such variables are reported and kept in memory until the server stops.
 */
public class MySQLStorage extends JdbcStorage {

	private static final int MAX_NAME_LENGTH = 768; // VARCHAR(768)
	private static final long PACKET_HEADROOM = 16 * 1024; // for the name and the rest of the statement

	private String host;
	private Integer port;
	private String database;
	private String user;
	private String password;

	private static final Pattern VERSION = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)");

	private String collation = "";
	private boolean aliasUpsert;

	private long maxValueLength = Long.MAX_VALUE;
	private long maxStatementBytes = Long.MAX_VALUE;

	private static final int ROWS_PER_STATEMENT = 1000;

	/**
	 * @param registeringAddon the addon that registered this storage's type
	 * @param type             the type name the config picked this storage with, such as {@code mysql}
	 */
	public MySQLStorage(SkriptAddon registeringAddon, String type) {
		super(registeringAddon, type);
	}

	@Override
	protected boolean configureConnection(SectionNode node) {
		host = getConfigValue(node, "host");
		database = getConfigValue(node, "database");
		user = getConfigValue(node, "user");
		port = getConfigOptional(node, "port", Integer.class, 3306);
		password = getConfigOptional(node, "password", String.class, "");
		if (host == null || database == null || user == null || port == null || password == null)
			return false;
		if (!(1 <= port && port <= 65535)) {
			Skript.error("The port of the database '" + getName() + "' must be between 1 and 65535, not " + port);
			return false;
		}
		return true;
	}

	@Override
	protected String tableKey(String table) {
		return (host + ":" + port + "/" + database + "." + table).toLowerCase(Locale.ROOT);
	}

	/**
	 * @return True, since several servers can share one table
	 */
	@Override
	protected boolean supportsMonitoring() {
		return true;
	}

	@Override
	protected Connection connect() throws SQLException {
		Properties properties = new Properties();
		properties.setProperty("user", user);
		properties.setProperty("password", password);
		properties.setProperty("characterEncoding", "UTF-8");
		properties.setProperty("connectTimeout", "10000");
		properties.setProperty("socketTimeout", "60000");
		properties.setProperty("tcpKeepAlive", "true");
		return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, properties);
	}

	@Override
	protected void setUp(Connection connection) throws SQLException {
		// pick a collation that compares names exactly, without ignoring trailing spaces
		String found = null;
		try (Statement statement = connection.createStatement();
		     ResultSet result = statement.executeQuery("SELECT COLLATION_NAME FROM information_schema.COLLATIONS "
			     + "WHERE COLLATION_NAME IN ('utf8mb4_0900_bin', 'utf8mb4_nopad_bin')")) {
			while (result.next()) {
				String name = result.getString(1);
				if (found == null || name.equals("utf8mb4_0900_bin"))
					found = name;
			}
		}
		if (found == null)
			throw new SQLException("The database '" + getName() + "' needs MySQL 8.0 or MariaDB 10.2.2 or newer, "
				+ "since older versions can't compare variable names exactly");
		collation = found;

		// MariaDB never supported 'AS new', and MySQL only from 8.0.19
		String version = connection.getMetaData().getDatabaseProductVersion();
		if (version.toLowerCase(Locale.ROOT).contains("mariadb")) {
			aliasUpsert = false;
		} else {
			Matcher matcher = VERSION.matcher(version);
			aliasUpsert = matcher.find() && atLeast(matcher, 8, 0, 19);
		}

		// packet lengths
		try (Statement statement = connection.createStatement();
		     ResultSet result = statement.executeQuery("SELECT @@max_allowed_packet")) {
			if (!result.next())
				throw new SQLException("The database '" + getName() + "' didn't say how big a packet it accepts");
			long packet = result.getLong(1);
			if (packet <= PACKET_HEADROOM)
				throw new SQLException("The database '" + getName() + "' only accepts packets of " + packet + " bytes, "
					+ "which is too small to save variables. Set its 'max_allowed_packet' to at least 1 MB");
			// the driver sends values as hex, two characters per byte, and we need to leave room for the name and the statement
			maxValueLength = (packet - PACKET_HEADROOM) / 2;
			// a statement of several rows has to fit too. A row with the longest value fits alone, its name in the headroom
			maxStatementBytes = packet - PACKET_HEADROOM;
		}
	}

	/**
	 * Adds the version index that polling needs, if this server monitors changes and the table doesn't have it yet.
	 * MySQL has no {@code CREATE INDEX IF NOT EXISTS}, hence the check. An index this server doesn't need is only
	 * warned about, not dropped, since another server sharing the table may poll with it.
	 */
	@Override
	protected void schemaCreated(Connection connection) throws SQLException {
		boolean indexed;
		try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM information_schema.STATISTICS "
				+ "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = 'version_index'")) {
			statement.setString(1, table());
			try (ResultSet result = statement.executeQuery()) {
				indexed = result.next();
			}
		}
		if (monitorable() && !indexed) {
			try (Statement statement = connection.createStatement()) {
				statement.execute("ALTER TABLE " + table() + " ADD INDEX version_index (version)");
			}
		} else if (!monitorable() && indexed) {
			Skript.warning("The table '" + table() + "' of the database '" + getName() + "' has an index for monitoring "
				+ "changes, but this server doesn't monitor them. If no server does, removing it makes saving faster: "
				+ "ALTER TABLE " + table() + " DROP INDEX version_index");
		}
	}

	private static boolean atLeast(Matcher version, int major, int minor, int patch) {
		int[] actual = {
			Integer.parseInt(version.group(1)),
			Integer.parseInt(version.group(2)),
			Integer.parseInt(version.group(3))
		};
		int[] wanted = {major, minor, patch};
		return Arrays.compare(actual, wanted) >= 0;
	}

	@Override
	protected List<String> schemaSql(String table, String metaTable) {
		return List.of(
			// binary, no-pad collation: names compare exactly, so 'a' and 'a ' are different variables,
			// and a list's names sort together, which the range query for clears relies on
			"CREATE TABLE IF NOT EXISTS " + table + " ("
				+ "name VARCHAR(" + MAX_NAME_LENGTH + ") CHARACTER SET utf8mb4 COLLATE " + collation + " NOT NULL PRIMARY KEY, "
				+ "type VARCHAR(255) CHARACTER SET utf8mb4 COLLATE " + collation + ", "
				+ "value LONGBLOB, "
				+ "version BIGINT NOT NULL, "
				+ "server CHAR(36) NOT NULL"
				+ ") ENGINE=InnoDB", // the version index is added by schemaCreated, only while monitoring
			"CREATE TABLE IF NOT EXISTS " + metaTable + " (id INT NOT NULL PRIMARY KEY, version BIGINT NOT NULL) ENGINE=InnoDB",
			"INSERT IGNORE INTO " + metaTable + " (id, version) VALUES (0, 0)");
	}

	@Override
	protected String upsertSql(String table, int rows) {
		String insert = "INSERT INTO " + table + " (name, type, value, version, server) VALUES " + upsertValues(rows);
		if (aliasUpsert) // MySQL 8.0.19+: VALUES() in the update is deprecated there
			return insert + " AS new ON DUPLICATE KEY UPDATE "
				+ "type = new.type, value = new.value, version = new.version, server = new.server";
		// MariaDB, and MySQL before 8.0.19
		return insert + " ON DUPLICATE KEY UPDATE "
			+ "type = VALUES(type), value = VALUES(value), version = VALUES(version), server = VALUES(server)";
	}

	@Override
	protected boolean requiresFile() {
		return false;
	}

	@Override
	protected int loadFetchSize() {
		return Integer.MIN_VALUE; // the MySQL driver streams rows only for this value; otherwise it buffers the whole table
	}

	@Override
	protected int maxNameLength() {
		return MAX_NAME_LENGTH;
	}

	@Override
	protected long maxValueLength() {
		return maxValueLength;
	}

	/**
	 * @return {@value #ROWS_PER_STATEMENT}: the driver's own batching copies every row's parameters and escapes each string twice,
	 * which made about 3 KB of garbage per row saved, and without its rewriting sends each delete on its own. Building
	 * statements of several rows here makes about a third of the garbage, and deletes about 15 times faster
	 */
	@Override
	protected int rowsPerStatement() {
		return ROWS_PER_STATEMENT;
	}

	@Override
	protected long maxStatementBytes() {
		return maxStatementBytes;
	}

}
