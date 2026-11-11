package org.skriptlang.skript.bukkit.docs;

import ch.njol.skript.lang.EventRestrictedSyntax;
import org.bukkit.plugin.java.JavaPlugin;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.docs.Documentable;
import org.skriptlang.skript.docs.DocumentationAdapter;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.function.BiConsumer;

/**
 * An extended implementation of {@link DocumentationAdapter#of(SkriptAddon)} that also covers:
 * <ul>
 *     <li>{@link EventRestrictedSyntax}s</li>
 *     <li>Includes plugin version of plugin representing {@code addon} in "source" scope</li>
 * </ul>
 */
public final class BukkitDocumentationAdapter {

	/**
	 * Creates and populates an adapter with documentation for elements provided by {@code addon}.
	 * @param addon The addon to extract documentation from.
	 * @return A populated adapter.
	 */
	public static DocumentationAdapter of(SkriptAddon addon) {
		return of(addon, (ignored1, ignored2) -> { });
	}

	/**
	 * Creates and populates an adapter with documentation for elements provided by {@code addon}.
	 * @param addon The addon to extract documentation from.
	 * @param writeHandler A consumer to be called for every written {@link Documentable}.
	 *  This is called after {@link Documentable#write(DocumentationAdapter)} but before {@link Documentable#postWrite(DocumentationAdapter)}.
	 *  Thus, it is useful for intercepting certain documentables to write additional data.
	 * @return A populated adapter.
	 */
	public static DocumentationAdapter of(SkriptAddon addon, BiConsumer<DocumentationAdapter, Documentable> writeHandler) {
		return DocumentationAdapter.of(addon, (adapter, documentable) -> {
			if (documentable instanceof SyntaxInfo<?> info) {
				Object instance = info.instance();
				if (instance instanceof EventRestrictedSyntax ers &&
					info.documentation().additionalData(Events.class) == null) {
					adapter.write(Events.of(ers.supportedEvents()));
				}
			} else if (documentable == addon) {
				adapter.editScope("source");
				adapter.write("version", JavaPlugin.getProvidingPlugin(addon.source()).getPluginMeta().getVersion());
				adapter.exitScope();
			}
			writeHandler.accept(adapter, documentable);
		});
	}

}
