package ch.njol.skript.variables;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class MySQLSchemaTest {

	@Test
	public void initializesCurrentSchema() throws Exception {
		for (String table : new String[]{"skript_variables_v1", "skript_vars", "variables_test"})
			checkSchema(false, table);
	}

	@Test
	public void rejectsIncompatibleTableWithoutAlteringIt() throws Exception {
		checkSchema(true, "variables_test");
	}

	private void checkSchema(boolean incompatible, String table) throws Exception {
		Connection connection = createMock(Connection.class);
		PreparedStatement create = createMock(PreparedStatement.class);
		PreparedStatement engine = createMock(PreparedStatement.class);
		PreparedStatement columns = createMock(PreparedStatement.class);
		ResultSet engineRows = createMock(ResultSet.class);
		ResultSet columnRows = createMock(ResultSet.class);
		expect(connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + table + "` (name LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, type VARCHAR(255) NOT NULL, hash BINARY(32) PRIMARY KEY, value LONGBLOB) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin")).andReturn(create);
		create.setQueryTimeout(30);
		expect(create.executeUpdate()).andReturn(0);
		create.close();
		expect(connection.prepareStatement("SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?")).andReturn(engine);
		engine.setQueryTimeout(10);
		engine.setString(1, table);
		expect(engine.executeQuery()).andReturn(engineRows);
		expect(engineRows.next()).andReturn(true);
		expect(engineRows.getString(1)).andReturn("InnoDB");
		engineRows.close();
		engine.close();
		expect(connection.prepareStatement("SELECT COLUMN_NAME,COLUMN_TYPE,COLUMN_KEY,CHARACTER_SET_NAME,COLLATION_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?")).andReturn(columns);
		columns.setQueryTimeout(10);
		columns.setString(1, table);
		expect(columns.executeQuery()).andReturn(columnRows);
		String[][] definitions = {{"name", "longtext", ""}, {"type", "varchar(255)", ""},
				{"hash", "binary(32)", "PRI"}, {"value", incompatible ? "varchar(255)" : "longblob", ""}};
		for (String[] definition : definitions) {
			expect(columnRows.next()).andReturn(true);
			expect(columnRows.getString(1)).andReturn(definition[0]);
			expect(columnRows.getString(2)).andReturn(definition[1]);
			expect(columnRows.getString(3)).andReturn(definition[2]);
			if (definition[0].equals("name")) {
				expect(columnRows.getString(4)).andReturn("utf8mb4");
				expect(columnRows.getString(5)).andReturn("utf8mb4_bin");
			}
		}
		expect(columnRows.next()).andReturn(false);
		columnRows.close();
		columns.close();
		replay(connection, create, engine, columns, engineRows, columnRows);
		if (incompatible) {
			var failure = assertThrows(MySQLSchema.ValidationException.class,
					() -> MySQLSchema.initialize(connection, table));
			assertEquals("MySQL variable table must contain exactly: name LONGTEXT, type VARCHAR(255), hash BINARY(32), value LONGBLOB",
					failure.getMessage());
		} else
			MySQLSchema.initialize(connection, table);
		verify(connection, create, engine, columns, engineRows, columnRows);
	}
}
