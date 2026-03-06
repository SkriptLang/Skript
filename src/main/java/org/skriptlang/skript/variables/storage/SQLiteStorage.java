package org.skriptlang.skript.variables.storage;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.JdbcStorage;
import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.File;

/**
 * SQLite storage for Skript variables.
 */
public class SQLiteStorage extends JdbcStorage {

	public SQLiteStorage(SkriptAddon source, String type) {
		super(source, type);
	}

	@Override
	protected @Nullable HikariConfig configuration(SectionNode sectionNode) {
		if (file == null)
			return null;
		assert file.getName().endsWith(".db");

		HikariConfig configuration = new HikariConfig();
		configuration.setJdbcUrl("jdbc:sqlite:" + (file == null ? ":memory:" : file.getAbsolutePath()));
		return configuration;
	}

	@Override
	protected boolean requiresFile() {
		return true;
	}

	@Override
	protected File getFile(String fileName) {
		if (!fileName.endsWith(".db"))
			fileName = fileName + ".db"; // required by SQLite
		return new File(fileName);
	}

}
