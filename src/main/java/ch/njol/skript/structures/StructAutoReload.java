package ch.njol.skript.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RedirectingLogHandler;
import ch.njol.skript.log.TimingLogHandler;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Utils;
import ch.njol.util.OpenCloseable;
import ch.njol.util.StringUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryData;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptData;
import org.skriptlang.skript.lang.structure.Structure;

import com.google.common.collect.Lists;

@Name("Auto Reload")
@Description({
	"Place at the top of a script file to enable and configure automatic reloading of the script.",
	"When the script is saved, Skript will automatically reload the script.",
	"The config.sk node 'script loader thread size' must be set to a positive number for this to be enabled.",
	"",
	"available optional nodes:",
	"\trecipients: The players to send reload messages to. Defaults to console.",
	"\tpermission: The permission required to receive reload messages. 'recipients' will override this node.",
})
@Examples({
	"auto reload",
	"",
	"auto reload:",
	"\trecipients: \"SkriptDev\", \"61699b2e-d327-4a01-9f1e-0ea8c3f06bc6\" and \"Njol\"", // UUID is Dinnerbone's.
	"\tpermission: \"skript.reloadnotify\"",
})
@Since("INSERT VERSION")
public class StructAutoReload extends Structure {

	public static final Priority PRIORITY = new Priority(10);

	static {
		Skript.registerSimpleStructure(StructAutoReload.class, "auto[matically] reload");
	}

	@Override
	public EntryValidator entryValidator() {
		return EntryValidator.builder()
				// Uses OfflinePlayer because this is determined at parse time. Runtime will make sure it's a Player.
				.addEntryData(new EntryData<OfflinePlayer[]>("recipients", null, true) {

					@Override
					public OfflinePlayer @Nullable [] getValue(Node node) {
						EntryNode entry = (EntryNode) node;
						Literal<? extends OfflinePlayer> recipients = SkriptParser.parseLiteral(entry.getValue(), OfflinePlayer.class, ParseContext.DEFAULT);
						return recipients.getArray();
					}

					@Override
					public boolean canCreateWith(Node node) {
						return node instanceof EntryNode;
					}

				})
				.addEntry("permission", "skript.reloadnotify", true)
				.build();
	}

	private Script script;
	private Task task;

	@Override
	public boolean init(Literal<?> @NotNull [] arguments, int pattern, ParseResult result, EntryContainer container) {
		try {
			int threadSize = Integer.parseInt(SkriptConfig.scriptLoaderThreadSize.value());
			if (threadSize <= 0)
				throw new IllegalStateException();
		} catch (IllegalStateException | NumberFormatException e) {
			Skript.error(Language.get("log.auto reload.async required"));
			return false;
		}

		OfflinePlayer[] recipients = null;
		String permission = "skript.reloadnotify";

		// Container can be null if the structure is simple.
		if (container != null) {
			recipients = container.get("recipients", OfflinePlayer[].class, false);
			permission = container.get("permission", String.class, false);
		}

		script = getParser().getCurrentScript();
		File file = script.getConfig().getFile();
		if (file == null || !file.exists()) {
			Skript.error(Language.get("log.auto reload.file not found"));
			return false;
		}
		script.addData(new AutoReload(file.lastModified(), permission, recipients));
		return true;
	}

	@Override
	public boolean load() {
		return true;
	}

	@Override
	public boolean postLoad() {
		task = new Task(Skript.getInstance(), 0, 20 * 2, true) {
			@Override
			public void run() {
				AutoReload data = script.getData(AutoReload.class);
				File file = script.getConfig().getFile();
				if (file == null || !file.exists())
					return;
				long lastModified = file.lastModified();
				if (lastModified <= data.getLastReloadTime())
					return;

				data.setLastReloadTime(lastModified);
				try (
					RedirectingLogHandler logHandler = new RedirectingLogHandler(data.getRecipients(), "").start();
					TimingLogHandler timingLogHandler = new TimingLogHandler().start()
				) {
					OpenCloseable openCloseable = OpenCloseable.combine(logHandler, timingLogHandler);
					ScriptLoader.reloadScript(script, openCloseable).thenRun(() -> reloaded(logHandler, timingLogHandler));
				} catch (Exception e) {
					//noinspection ThrowableNotThrown
					Skript.exception(e, "Exception occurred while automatically reloading a script", script.getConfig().getFileName());
				}
			}
		};
		return true;
	}

	@Override
	public void unload() {
		task.cancel();
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "auto reload";
	}

	private static final ArgsMessage m_reload_error = new ArgsMessage("log.auto reload.error");
	private static final ArgsMessage m_reloaded = new ArgsMessage("log.auto reload.reloaded");

	private void reloaded(RedirectingLogHandler logHandler, TimingLogHandler timingLogHandler) {
		String what = PluralizingArgsMessage.format(Language.format("log.auto reload.script", script.getConfig().getFileName()));
		String timeTaken = String.valueOf(timingLogHandler.getTimeTaken());

		String message;
		if (logHandler.numErrors() == 0) {
			message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(m_reloaded.toString(what, timeTaken)));
			logHandler.log(new LogEntry(Level.INFO, Utils.replaceEnglishChatStyles(message)));
		} else {
			message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(m_reload_error.toString(what, logHandler.numErrors(), timeTaken)));
			logHandler.log(new LogEntry(Level.SEVERE, Utils.replaceEnglishChatStyles(message)));
		}
	}

	public final class AutoReload implements ScriptData {

		private final List<OfflinePlayer> recipients = new ArrayList<>();
		private long lastReload; // Compare with File#lastModified()

		// private constructor to prevent instantiation.
		private AutoReload(long lastReload, @Nullable String permission, @Nullable OfflinePlayer... recipients) {
			if (recipients != null) {
				this.recipients.addAll(Lists.newArrayList(recipients));
			} else if (permission != null) {
				Bukkit.getOnlinePlayers().stream()
					.filter(p -> p.hasPermission(permission))
					.forEach(this.recipients::add);
			}
			this.lastReload = lastReload;
		}

		/**
		 * Returns a new list of the recipients to recieve reload errors.
		 * Console command sender included.
		 * 
		 * @return the recipients in a list
		 */
		@Unmodifiable
		public List<CommandSender> getRecipients() {
			List<CommandSender> senders = Lists.newArrayList(Bukkit.getConsoleSender());
			senders.addAll(recipients.stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).toList());
			return Collections.unmodifiableList(senders); // Unmodifiable to denote that changes won't affect the data.
		}

		public long getLastReloadTime() {
			return lastReload;
		}

		public void setLastReloadTime(long lastReload) {
			this.lastReload = lastReload;
		}

	}

}
