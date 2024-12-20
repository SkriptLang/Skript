package ch.njol.skript;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import org.bukkit.command.CommandSender;

import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.update.ReleaseManifest;
import ch.njol.skript.update.ReleaseStatus;
import ch.njol.skript.update.UpdateManifest;
import ch.njol.skript.update.Updater;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;

/**
 * Skript's update checker.
 */
public class SkriptUpdater extends Updater {
	
	public final static Message m_not_started = new Message("updater.not started");
	public final static Message m_checking = new Message("updater.checking");
	public final static Message m_check_in_progress = new Message("updater.check in progress");
	public final static Message m_updater_disabled = new Message("updater.updater disabled");
	public final static ArgsMessage m_check_error = new ArgsMessage("updater.check error");
	public final static Message m_running_latest_version = new Message("updater.running latest version");
	public final static Message m_running_latest_version_beta = new Message("updater.running latest version (beta)");
	public final static ArgsMessage m_update_available = new ArgsMessage("updater.update available");
	public final static ArgsMessage m_downloading = new ArgsMessage("updater.downloading");
	public final static Message m_download_in_progress = new Message("updater.download in progress");
	public final static ArgsMessage m_download_error = new ArgsMessage("updater.download error");
	public final static ArgsMessage m_downloaded = new ArgsMessage("updater.downloaded");
	public final static Message m_internal_error = new Message("updater.internal error");
	public final static Message m_custom_version = new Message("updater.custom version");
	public final static Message m_nightly = new Message("updater.nightly build");
	
	SkriptUpdater() {
		super(loadManifest());
	}
	
	/**
	 * Loads the release manifest from Skript jar.
	 * @return Release manifest.
	 */
	private static ReleaseManifest loadManifest() {
		String manifest;
		try (InputStream is = Skript.getInstance().getResource("release-manifest.json");
				Scanner s = new Scanner(is)) {
			s.useDelimiter("\\\\A");
			manifest = s.next();
		} catch (IOException e) {
			throw new IllegalStateException("Skript is missing release-manifest.json!");
		}
		assert manifest != null;
		return ReleaseManifest.load(manifest);
	}
	
	/**
	 * Checks for updates and messages the sender.
	 * @param sender Who should we message.
	 * @return Future that completes when we're done.
	 */
	public CompletableFuture<Void> updateCheck(CommandSender sender) {
		CompletableFuture<Void> future = checkUpdates().thenAccept(none -> {
			ReleaseStatus status = getReleaseStatus();
			switch (status) {
				case CUSTOM:
					Skript.info(sender, "" + m_custom_version);
					break;
				case DEVELOPMENT:
					Skript.info(sender, "" + m_nightly);
					break;
				case LATEST:
					Skript.info(sender, "" + m_running_latest_version);
					break;
				case OUTDATED:
					UpdateManifest update = getUpdateManifest();
					assert update != null; // Because we just checked that one is available
					Skript.info(sender, "" + m_update_available.toString(update.id, Skript.getVersion()));
					sender.spigot().sendMessage(BungeeConverter.convert(ChatMessages.parseToArray(
							"Download it at: <aqua><u><link:" + update.downloadUrl + ">" + update.downloadUrl)));
					break;
				case UNKNOWN:
					if (isEnabled()) {
						Skript.error(sender, "" + m_internal_error);
					} else {
						Skript.info(sender, "" + m_updater_disabled);
					}
					break;
			}
		});
		assert future != null;
		return future;
	}

	/**
	 * Checks for update change log and messages the sender.
	 * @param sender Who should we message
	 * @return Future that completes when we're done.
	 */
	public CompletableFuture<Void> changesCheck(CommandSender sender) {
		CompletableFuture<Void> future = updateCheck(sender).thenAccept(none -> {
			if (getReleaseStatus() == ReleaseStatus.OUTDATED) {
				UpdateManifest update = getUpdateManifest();
				if (update != null) { // Avoid a race condition
					sender.sendMessage("");
					Skript.info(sender, "Patch notes:");
					for (String line : update.patchNotes.split("\\n")) {
						// Minecraft doesn't like CRLF, remove it
						line = line.replace("\r", "");
						
						// Find #issue references and make them links
						String processed = line;
						for (int start = line.indexOf('#'); start != -1; start = line.indexOf('#', start + 1)) {
							StringBuilder issue = new StringBuilder();
							for (int i = start + 1; i < line.length();) {
								int c = line.codePointAt(i);
								if (Character.isDigit(c)) {
									issue.appendCodePoint(c);
								} else {
									break;
								}
								i += Character.charCount(c);
							}
							
							// Ok, looks like valid issue reference
							if (issue.length() > 0) {
								// TODO get issue tracker URL from manifest or something
								processed = processed.replace("#" + issue,
										"<aqua><u><link:https://github.com/SkriptLang/Skript/issues/"
										+ issue + ">#" + issue + "<r>");
							}
						}
						line = processed;
						
						assert line != null;
						sender.spigot().sendMessage(BungeeConverter.convert(ChatMessages.parseToArray(line)));
					}
				}
			}
		});
		assert future != null;
		return future;
	}
}
