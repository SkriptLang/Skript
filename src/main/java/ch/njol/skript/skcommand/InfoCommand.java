package ch.njol.skript.skcommand;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import ch.njol.skript.update.Updater;
import io.papermc.paper.ServerBuildInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static ch.njol.skript.skcommand.SkriptCommand.send;

public class InfoCommand extends SubCommand {

	static {
		HAS_BUILD_INFO = Skript.classExists("io.papermc.paper.ServerBuildInfo");
	}

	// TODO - remove this field when 1.20.5 & Spigot support is dropped
	private static final boolean HAS_BUILD_INFO;

	public InfoCommand() {
		super("info");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		send(sender, "info.aliases");
		send(sender, "info.documentation");
		send(sender, "info.tutorials");

		send(sender, "info.server", getServerVersion());
		send(sender, "info.version", getSkriptVersion());

		if (Skript.getAddons().isEmpty()) {
			send(sender, "info.addons", "None");
		} else {
			send(sender, "info.addons", "");
			getAddonList().forEach(sender::sendRichMessage);
		}

		if (getDependencyList().isEmpty()) {
			send(sender, "info.dependencies", "None");
		} else {
			send(sender, "info.dependencies", "");
			getDependencyList().forEach(sender::sendRichMessage);
		}
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return List.of();
	}

	private static String getServerVersion() {
		if (HAS_BUILD_INFO) {
			ServerBuildInfo buildInfo = ServerBuildInfo.buildInfo();
			String version = buildInfo.brandName() + " " + buildInfo.minecraftVersionName();

			if (buildInfo.buildNumber().isPresent()) {
				version += " #" + buildInfo.buildNumber().getAsInt();
			}

			if (buildInfo.gitCommit().isPresent()) {
				version += " (" + buildInfo.gitCommit().get() + ")";
			}

			return version;
		}

		return Bukkit.getVersion();
	}

	private static String getSkriptVersion() {
		Updater updater = Skript.getInstance().getUpdater();
		if (updater != null) {
			return Skript.getVersion() + " (" + updater.getCurrentRelease().flavor + ")";
		}
		return Skript.getVersion().toString();
	}

	private static List<String> getAddonList() {
		List<String> list = new ArrayList<>();
		for (SkriptAddon addon : Skript.getAddons()) {
			// noinspection deprecation
			PluginDescriptionFile desc = addon.plugin.getDescription();
			String web = desc.getWebsite();
			list.add(" - " + desc.getFullName() + (web != null ? " (" + web + ")" : ""));
		}
		return list;
	}

	private static List<String> getDependencyList() {
		List<String> list = new ArrayList<>();
		// noinspection deprecation
		for (String pluginName : Skript.getInstance().getDescription().getSoftDepend()) {
			Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
			if (plugin != null) {
				// noinspection deprecation
				list.add(" - " + plugin.getDescription().getFullName());
			}
		}
		return list;
	}

}
