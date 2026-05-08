package ch.njol.skript.lang.globals;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.lang.OptionRegistry;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.IOException;

/**
 * Represents the 'Skript/globals/options.sk' file
 */
public class GlobalOptions extends GlobalFile {

	public GlobalOptions(SkriptAddon addon) {
		super(addon, "options.sk");
	}

	@Override
	protected void onInit() {
		if (!file.exists()) {
			copyFile("options.sk");
		}
		load();
	}

	@Override
	protected void onLoad() {
		try {
			Config config = new Config(file, true, false, ":");
			config.getMainNode().convertToEntries(-1);
			addon().registry(OptionRegistry.class).loadGlobalOptions(config.getMainNode());
		} catch (IOException e) {
			Skript.exception(e, "Error while loading global options");
		}
	}

	@Override
	protected void onUnload() {
		addon().registry(OptionRegistry.class).getGlobalOptions().clear();
	}

}
