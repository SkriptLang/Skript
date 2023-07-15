/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.hooks;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.doc.Documentation;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

@ApiStatus.Internal
public final class VaultHook extends SimpleHook<Vault> {

	public static final String NO_GROUP_SUPPORT = "The permissions plugin you are using does not support groups.";

	@Nullable
	public static Economy economy;
	@Nullable
	public static Chat chat;
	@Nullable
	public static Permission permission;

	public VaultHook() {
		super("Vault");
	}

	@Override
	public boolean init() {
		economy = obtain(Economy.class);
		chat = obtain(Chat.class);
		permission = obtain(Permission.class);

		return economy != null || chat != null || permission != null;
	}

	@Override
	protected void loadClasses() {
		try {
			SkriptAddon addon = Skript.getAddonInstance();
			String root = getClass().getPackage().getName() + ".";
			boolean docs = Documentation.canGenerateUnsafeDocs();
			if (economy != null || docs)
				addon.loadClasses(root + "economy");
			if (chat != null || docs)
				addon.loadClasses(root + "chat");
			if (permission != null || docs)
				addon.loadClasses(root + "permission");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	private static <T> T obtain(Class<T> clazz) {
		RegisteredServiceProvider<T> service = Bukkit.getServicesManager().getRegistration(clazz);
		return service == null ? null : service.getProvider();
	}

}
