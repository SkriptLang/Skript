package ch.njol.skript.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
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
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptData;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.DefaultSyntaxInfos.Structure.NodeType;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

@Name("Auto Reload")
@Description("""
	Place at the top of a script file to enable and configure automatic reloading of the script.
	When the script is saved, Skript will automatically reload the script.
	The config.sk node 'script loader thread size' must be set to a positive number (async or parallel loading) \
	for this to be enabled.
	
	available optional nodes:
		recipients: The players to send reload messages to. Defaults to console.
		permission: The permission required to receive reload messages. 'recipients' will override this node.
	""")
@Example("auto reload")
@Example("""
	auto reload:
		recipients: "SkriptDev",  "61699b2e-d327-4a01-9f1e-0ea8c3f06bc6" and "Njol"
		permission: "skript.reloadnotify"
	""") // UUID is Dinnerbone's.
@Since("2.13")
public class StructAutoReload extends Structure {

	public static final Priority PRIORITY = new Priority(10);
	private static final EntryValidator VALIDATOR = EntryValidator.builder()
		.addEntryData(new ExpressionEntryData<>("recipients", null, true, String.class, SkriptParser.PARSE_EXPRESSIONS)) // LiteralString doesn't work with PARSE_LITERALS
		.addEntry("permission", "skript.reloadnotify", true)
		.build();

	static {
		Skript.registerStructure(StructAutoReload.class, VALIDATOR, NodeType.BOTH, "auto[matically] reload [(this|the) script]");
	}

	private Script script;
	private Task task;

	@Override
	public boolean init(Literal<?> @NotNull [] arguments, int pattern, ParseResult result, EntryContainer container) {
		if (!ScriptLoader.isAsync()) {
			Skript.error(Language.get("log.auto reload.async required"));
			return false;
		}

		String[] recipients = null;
		String permission = "skript.reloadnotify";

		// Container can be null if the structure is simple.
		if (container != null) {
			@SuppressWarnings("unchecked")
			Expression<String> expression = (Expression<String>) container.getOptional("recipients", false); // Must be false otherwise the API will throw an exception.
			List<String> strings = new ArrayList<>();
			if (expression instanceof LiteralString literal) {
				strings.add(literal.getSingle());
			} else if (expression instanceof ExpressionList<String> list) {
				list.getAllExpressions().forEach(expr -> {
					if (expr instanceof LiteralString literalString)
						strings.add(literalString.getSingle());
				});
			}
			if (!strings.isEmpty()) {
				recipients = strings.toArray(String[]::new);
			}
			permission = container.getOptional("permission", String.class, false);
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
				if (data == null || file == null || !file.exists())
					return;
				long lastModified = file.lastModified();
				if (lastModified <= data.getLastReloadTime())
					return;

				data.setLastReloadTime(lastModified);
				try (
					RedirectingLogHandler logHandler = new RedirectingLogHandler(data.getRecipients(), "").start();
					TimingLogHandler timingLogHandler = new TimingLogHandler().start()
				) {
					reloading(logHandler);
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

	private void reloading(RedirectingLogHandler logHandler) {
		String prefix = Language.get("skript.prefix");
		String what = PluralizingArgsMessage.format(Language.format("log.auto reload.script", script.getConfig().getFileName()));
		String message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(Language.format("log.auto reload.reloading", what)));
		logHandler.log(new LogEntry(Level.INFO, Utils.replaceEnglishChatStyles(prefix + message)));
	}

	private void reloaded(RedirectingLogHandler logHandler, TimingLogHandler timingLogHandler) {
		String prefix = Language.get("skript.prefix");
		ArgsMessage m_reload_error = new ArgsMessage("log.auto reload.error");
		ArgsMessage m_reloaded = new ArgsMessage("log.auto reload.reloaded");
		String what = PluralizingArgsMessage.format(Language.format("log.auto reload.script", script.getConfig().getFileName()));
		String timeTaken = String.valueOf(timingLogHandler.getTimeTaken());

		String message;
		if (logHandler.numErrors() == 0) {
			message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(m_reloaded.toString(what, timeTaken)));
			logHandler.log(new LogEntry(Level.INFO, Utils.replaceEnglishChatStyles(prefix + message)));
		} else {
			message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(m_reload_error.toString(what, logHandler.numErrors(), timeTaken)));
			logHandler.log(new LogEntry(Level.SEVERE, Utils.replaceEnglishChatStyles(prefix + message)));
		}
	}

	public static final class AutoReload implements ScriptData {

		private final Set<String> recipients = new HashSet<>();
		private final String permission;
		private long lastReload; // Compare with File#lastModified()

		// private constructor to prevent instantiation.
		private AutoReload(long lastReload, @Nullable String permission, @Nullable String... recipients) {
			if (recipients != null) {
				for (String recipient : recipients) {
					if (recipient != null)
						this.recipients.add(recipient.toLowerCase(Locale.ENGLISH));
				}
			}

			this.permission = permission;
			this.lastReload = lastReload;
		}

		/**
		 * Returns a new list of the recipients to receive reload errors.
		 * Console command sender included.
		 * 
		 * @return the recipients in a list
		 */
		public @Unmodifiable List<CommandSender> getRecipients() {
			List<CommandSender> senders = Lists.newArrayList(Bukkit.getConsoleSender());
			if (!recipients.isEmpty()) {
				Bukkit.getOnlinePlayers().stream()
					.filter(p -> recipients.contains(p.getName().toLowerCase(Locale.ENGLISH)) || recipients.contains(p.getUniqueId().toString()))
					.forEach(senders::add);
				return Collections.unmodifiableList(senders);
			}

			// Collect players with permission. Recipients overrides the permission node.
			Bukkit.getOnlinePlayers().stream()
				.filter(p -> p.hasPermission(permission))
				.forEach(senders::add);
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
