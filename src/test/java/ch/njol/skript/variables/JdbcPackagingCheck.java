package ch.njol.skript.variables;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.jar.JarFile;
import javax.sql.DataSource;

/** Checks that the distributable uses externally supplied JDBC drivers, as it does on Paper. */
public class JdbcPackagingCheck {

	public static void main(String[] args) throws Exception {
		Path artifact = Path.of(args[0]);
		try (JarFile jar = new JarFile(artifact.toFile())) {
			if (jar.stream().anyMatch(entry -> entry.getName().startsWith("lib/PatPeter/")))
				throw new AssertionError("External SQL plugin classes packaged");
			if (jar.stream().anyMatch(entry -> entry.getName().startsWith("org/sqlite/")
					|| entry.getName().startsWith("com/mysql/")
					|| entry.getName().startsWith("ch/njol/skript/dependencies/mysql/")))
				throw new AssertionError("JDBC drivers must be supplied by Paper, not packaged");
			if (jar.getEntry("META-INF/services/java.sql.Driver") != null)
				throw new AssertionError("Eager driver discovery enabled");
		}
		try (URLClassLoader loader = new URLClassLoader(new URL[]{artifact.toUri().toURL()},
				JdbcPackagingCheck.class.getClassLoader())) {
			Class<?> pool = loader.loadClass("ch.njol.skript.variables.MySQLConnectionPool");
			var create = pool.getDeclaredMethod("dataSource", String.class, int.class,
					String.class, String.class, String.class, String.class);
			create.setAccessible(true);
			Class<?> cleanup = loader.loadClass("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
			boolean cleanupWasAlive = (boolean) cleanup.getMethod("isAlive").invoke(null);
			Object source = create.invoke(null, "localhost", 3306, "test", "test", "password", "VERIFY_IDENTITY");
			if (source.getClass().getClassLoader() != loader.getParent())
				throw new AssertionError("MySQL datasource was not loaded from the external classpath");
			if (cleanupWasAlive != (boolean) cleanup.getMethod("isAlive").invoke(null))
				throw new AssertionError("Skript changed the shared driver's cleanup executor state");
			Class<?> sqlite = loader.loadClass("org.sqlite.SQLiteDataSource");
			if (sqlite.getClassLoader() != loader.getParent())
				throw new AssertionError("SQLite datasource was not loaded from the external classpath");
			DataSource local = (DataSource) sqlite.getConstructor().newInstance();
			sqlite.getMethod("setUrl", String.class).invoke(local, "jdbc:sqlite::memory:");
			try (Connection connection = local.getConnection();
					PreparedStatement statement = connection.prepareStatement("SELECT 1");
					ResultSet result = statement.executeQuery()) {
				if (!result.next() || result.getInt(1) != 1)
					throw new AssertionError("External SQLite driver failed");
			}
			System.out.println("External JDBC drivers verified; no drivers bundled in Skript.");
		}
	}
}
