package ch.njol.skript.variables;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.jar.JarFile;
import javax.sql.DataSource;

/** Checks the actual distributable without access to server or dependency classpaths. */
public class JdbcPackagingCheck {

	public static void main(String[] args) throws Exception {
		Path artifact = Path.of(args[0]);
		try (JarFile jar = new JarFile(artifact.toFile())) {
			if (jar.stream().anyMatch(entry -> entry.getName().startsWith("lib/PatPeter/")))
				throw new AssertionError("External SQL plugin classes packaged");
			if (jar.getEntry("META-INF/services/java.sql.Driver") != null)
				throw new AssertionError("Eager driver discovery enabled");
		}
		try (URLClassLoader loader = new URLClassLoader(new URL[]{artifact.toUri().toURL()},
				ClassLoader.getPlatformClassLoader())) {
			Class<?> pool = loader.loadClass("ch.njol.skript.variables.MySQLConnectionPool");
			var create = pool.getDeclaredMethod("dataSource", String.class, int.class,
					String.class, String.class, String.class, String.class);
			create.setAccessible(true);
			Object source = create.invoke(null, "localhost", 3306, "test", "test", "password", "VERIFY_IDENTITY");
			if (source.getClass().getClassLoader() != loader)
				throw new AssertionError("MySQL datasource was not loaded from Skript");
			Class<?> cleanup = loader.loadClass("ch.njol.skript.dependencies.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
			for (int i = 0; i < 100 && (boolean) cleanup.getMethod("isAlive").invoke(null); i++)
				Thread.sleep(10);
			if ((boolean) cleanup.getMethod("isAlive").invoke(null))
				throw new AssertionError("Cleanup thread remains alive");
			Class<?> sqlite = loader.loadClass("org.sqlite.SQLiteDataSource");
			DataSource local = (DataSource) sqlite.getConstructor().newInstance();
			sqlite.getMethod("setUrl", String.class).invoke(local, "jdbc:sqlite::memory:");
			try (Connection connection = local.getConnection();
					PreparedStatement statement = connection.prepareStatement("SELECT 1");
					ResultSet result = statement.executeQuery()) {
				if (!result.next() || result.getInt(1) != 1)
					throw new AssertionError("Bundled SQLite driver failed");
			}
			System.out.println("Packaged JDBC drivers verified with only the Java platform classloader.");
		}
	}
}
