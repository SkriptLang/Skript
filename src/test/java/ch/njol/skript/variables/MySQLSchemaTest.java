package ch.njol.skript.variables;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class MySQLSchemaTest {

	private static final Map<String, String> OLD_SCHEMA = Map.of("name_hash", "binary(32)",
			"name", "longblob", "type", "varchar(255)", "small_value", "varbinary(32)", "value", "longblob");

	@Test
	public void migrationCopiesCompactValuesBeforeDroppingColumn() throws Exception {
		var control = createStrictControl();
		Connection connection = control.createMock(Connection.class);
		PreparedStatement names = control.createMock(PreparedStatement.class);
		PreparedStatement conflicts = control.createMock(PreparedStatement.class);
		PreparedStatement copy = control.createMock(PreparedStatement.class);
		PreparedStatement alter = control.createMock(PreparedStatement.class);
		ResultSet rows = control.createMock(ResultSet.class);
		expect(connection.prepareStatement("SELECT name FROM `custom_variables`")).andReturn(names);
		names.setQueryTimeout(10);
		expect(names.executeQuery()).andReturn(rows);
		expect(rows.next()).andReturn(true);
		expect(rows.getBytes(1)).andReturn("玩家::😀::VariableName".getBytes(StandardCharsets.UTF_8));
		expect(rows.next()).andReturn(false);
		rows.close();
		names.close();
		expect(connection.prepareStatement("SELECT 1 FROM `custom_variables` WHERE small_value IS NOT NULL AND value IS NOT NULL AND small_value <> value LIMIT 1")).andReturn(conflicts);
		conflicts.setQueryTimeout(10);
		expect(conflicts.executeQuery()).andReturn(rows);
		expect(rows.next()).andReturn(false);
		rows.close();
		conflicts.close();
		expect(connection.prepareStatement("UPDATE `custom_variables` SET value=small_value WHERE value IS NULL AND small_value IS NOT NULL")).andReturn(copy);
		copy.setQueryTimeout(30);
		expect(copy.executeUpdate()).andReturn(1);
		copy.close();
		expect(connection.prepareStatement("ALTER TABLE `custom_variables` MODIFY COLUMN name LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, DROP COLUMN small_value")).andReturn(alter);
		alter.setQueryTimeout(30);
		expect(alter.executeUpdate()).andReturn(0);
		alter.close();
		control.replay();
		MySQLSchema.migrate(connection, "custom_variables", OLD_SCHEMA);
		control.verify();
	}

	@Test
	public void invalidNamesPreventAnySchemaMutation() throws Exception {
		Connection connection = createMock(Connection.class);
		PreparedStatement names = createMock(PreparedStatement.class);
		ResultSet rows = createMock(ResultSet.class);
		expect(connection.prepareStatement("SELECT name FROM `variables`")).andReturn(names);
		names.setQueryTimeout(10);
		expect(names.executeQuery()).andReturn(rows);
		expect(rows.next()).andReturn(true);
		expect(rows.getBytes(1)).andReturn(new byte[]{(byte) 0xff});
		rows.close();
		names.close();
		replay(connection, names, rows);
		assertThrows(SQLException.class, () -> MySQLSchema.migrate(connection, "variables", OLD_SCHEMA));
		verify(connection, names, rows);
	}

	@Test
	public void conflictingPayloadColumnsAreNeverDropped() throws Exception {
		Connection connection = createMock(Connection.class);
		PreparedStatement conflicts = createMock(PreparedStatement.class);
		ResultSet rows = createMock(ResultSet.class);
		expect(connection.prepareStatement("SELECT 1 FROM `variables` WHERE small_value IS NOT NULL AND value IS NOT NULL AND small_value <> value LIMIT 1")).andReturn(conflicts);
		conflicts.setQueryTimeout(10);
		expect(conflicts.executeQuery()).andReturn(rows);
		expect(rows.next()).andReturn(true);
		rows.close();
		conflicts.close();
		replay(connection, conflicts, rows);
		Map<String, String> columns = new java.util.HashMap<>(OLD_SCHEMA);
		columns.put("name", "longtext");
		assertThrows(SQLException.class, () -> MySQLSchema.migrate(connection, "variables", columns));
		verify(connection, conflicts, rows);
	}

	@Test
	public void currentSchemaNeedsNoMigrationAndUnknownSchemaIsRejected() throws Exception {
		Connection connection = createMock(Connection.class);
		replay(connection);
		MySQLSchema.migrate(connection, "variables", Map.of("name_hash", "binary(32)",
				"name", "longtext", "type", "varchar(255)", "value", "longblob"));
		assertThrows(SQLException.class, () -> MySQLSchema.migrate(connection, "variables", Map.of()));
		verify(connection);
		String name = "VariableName::玩家::😀".repeat(1000);
		assertEquals(name, MySQLSchema.decodeName(name.getBytes(StandardCharsets.UTF_8)));
	}
}
