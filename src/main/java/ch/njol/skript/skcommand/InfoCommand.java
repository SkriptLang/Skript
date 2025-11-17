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

import static ch.njol.skript.skcommand.SkriptCommand.info;

/**
 * Shows info about Skript, server, installed addons and dependencies
 */
class InfoCommand extends SubCommand {

	public InfoCommand() {
		super("info");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		info(sender, "info.aliases");
		info(sender, "info.documentation");
		info(sender, "info.tutorials");

		info(sender, "info.server", getServerVersion());
		info(sender, "info.version", getSkriptVersion());

		if (Skript.getAddons().isEmpty()) {
			info(sender, "info.addons", "None");
		} else {
			info(sender, "info.addons", "");
			getAddonList().forEach(sender::sendRichMessage);
		}

		if (getDependencyList().isEmpty()) {
			info(sender, "info.dependencies", "None");
		} else {
			info(sender, "info.dependencies", "");
			getDependencyList().forEach(sender::sendRichMessage);
		}
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return List.of();
	}

	private static String getServerVersion() {
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
