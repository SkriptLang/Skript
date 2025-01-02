package ch.njol.skript.hooks;

import java.io.IOException;

import ch.njol.skript.doc.Documentation;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;

import ch.njol.skript.Skript;

/**
 * @author Peter Güttinger
 */
public class VaultHook extends Hook<Vault> {

	public static final String NO_GROUP_SUPPORT = "The permissions plugin you are using does not support groups.";

	public VaultHook() throws IOException {}
	
	@SuppressWarnings("null")
	public static Economy economy;
	@SuppressWarnings("null")
	public static Chat chat;

	@SuppressWarnings("null")
	public static Permission permission;
	
	@SuppressWarnings("null")
	@Override
	protected boolean init() {
		economy = Bukkit.getServicesManager().getRegistration(Economy.class) == null ? null : Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
		chat = Bukkit.getServicesManager().getRegistration(Chat.class) == null ? null : Bukkit.getServicesManager().getRegistration(Chat.class).getProvider();
		permission = Bukkit.getServicesManager().getRegistration(Permission.class) == null ? null : Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
		return economy != null || chat != null || permission != null;
	}
	
	@Override
	@SuppressWarnings("null")
	protected void loadClasses() throws IOException {
		if (economy != null || Documentation.canGenerateUnsafeDocs())
			Skript.getAddonInstance().loadClasses(getClass().getPackage().getName() + ".economy");
		if (chat != null || (Documentation.canGenerateUnsafeDocs()))
			Skript.getAddonInstance().loadClasses(getClass().getPackage().getName() + ".chat");
		if (permission != null || (Documentation.canGenerateUnsafeDocs()))
			Skript.getAddonInstance().loadClasses(getClass().getPackage().getName() + ".permission");

	}
	
	@Override
	public String getName() {
		return "Vault";
	}
	
}
