package ch.njol.skript.hooks;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.localization.ArgsMessage;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class Hook<P extends Plugin> {

	private final static ArgsMessage m_hooked = new ArgsMessage("hooks.hooked"),
		m_hook_error = new ArgsMessage("hooks.error");

	public final P plugin;

	@SuppressWarnings({"null", "unchecked"})
	public Hook() throws IOException {
		final P p = (P) Bukkit.getPluginManager().getPlugin(getName());
		plugin = p;
		if (p == null) {
			if (Documentation.canGenerateUnsafeDocs()) {
				loadClasses();
				if (Skript.logHigh())
					Skript.info(m_hooked.toString(getName()));
			}
			return;
		}

		if (!init()) {
			Skript.error(m_hook_error.toString(p.getName()));
			return;
		}

		loadClasses();

		if (Skript.logHigh())
			Skript.info(m_hooked.toString(p.getName()));
    }

	public final P getPlugin() {
		return plugin;
	}

	protected void loadClasses() throws IOException {
		Skript.getAddonInstance().loadClasses("" + getClass().getPackage().getName());
	}

	/**
	 * @return The hooked plugin's exact name
	 */
	public abstract String getName();

	/**
	 * Called when the plugin has been successfully hooked
	 */
	protected boolean init() {
		return true;
	}

}
