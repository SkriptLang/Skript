package ch.njol.skript.hooks;

import java.io.IOException;

import ch.njol.skript.doc.Documentation;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.ArgsMessage;

/**
 * @author Peter Güttinger
 */
public abstract class Hook<P extends Plugin> {
	
	private final static ArgsMessage m_hooked = new ArgsMessage("hooks.hooked"),
			m_hook_error = new ArgsMessage("hooks.error");
	
	public final P plugin;
	
	public final P getPlugin() {
		return plugin;
	}
	
	@SuppressWarnings("null")
	public Hook() throws IOException {
		@SuppressWarnings("unchecked")
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

		return;
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
