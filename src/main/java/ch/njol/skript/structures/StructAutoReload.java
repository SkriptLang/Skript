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
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
	"When the script is saved, Skript will automatically reload the script."
})
@Examples({
	"auto reload",
	"",
	"auto reload:",
	"\trecipients: \"SkriptDev\", \"SkriptLang\" and \"Njol\"",
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
				.addEntryData(new EntryData<Player[]>("recipients", null, true) {

					@Override
					public Player @Nullable [] getValue(Node node) {
						EntryNode entry = (EntryNode) node;
						Literal<? extends Player> recipients = SkriptParser.parseLiteral(entry.getValue(), Player.class, ParseContext.DEFAULT);
						return recipients.getArray();
					}

					@Override
					public boolean canCreateWith(Node node) {
						return node instanceof EntryNode;
					}

				})
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

		List<Player> recipients = new ArrayList<>();
		Player[] parsed = container.get("recipients", Player[].class, false);
		if (parsed != null) {
			recipients.addAll(Lists.newArrayList(parsed));
		}

		script = getParser().getCurrentScript();
		File file = script.getConfig().getFile();
		script.addData(new AutoReload(recipients, file.lastModified()));
		return true;
	}

	@Override
	public boolean load() {
		return true;
	}

	@Override
	public boolean postLoad() {
		task = new Task(Skript.getInstance(), 0, 20 * 5, true) {
			@Override
			public void run() {
				AutoReload data = script.getData(AutoReload.class);
				File file = script.getConfig().getFile();
				long lastModified = file.lastModified();
				if (lastModified <= data.getLastReloadTime())
					return;

				data.setLastReloadTime(lastModified);
				try (
					RedirectingLogHandler logHandler = new RedirectingLogHandler(data.getRecipients(), "").start();
					TimingLogHandler timingLogHandler = new TimingLogHandler().start()
				) {
					OpenCloseable openCloseable = OpenCloseable.combine(logHandler, timingLogHandler);
					ScriptLoader.reloadScript(script, openCloseable).thenRun(() -> reloaded(logHandler, timingLogHandler)).get(lastModified, null);
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

		private final List<CommandSender> recipients = Lists.newArrayList(Bukkit.getConsoleSender());
		private long lastReload; // Compare with File#lastModified()

		// private constructor to prevent instantiation.
		// We want to allow modification, but no replacement in Script#addData
		private AutoReload(List<Player> recipients, long lastReload) {
			this.recipients.addAll(recipients);
			this.lastReload = lastReload;
		}

		/**
		 * Gets the recipients to recieve reload errors.
		 * 
		 * @return the recipients in a list, empty if none
		 */
		public List<CommandSender> getRecipients() {
			return recipients;
		}

		public long getLastReloadTime() {
			return lastReload;
		}

		public void setLastReloadTime(long lastReload) {
			this.lastReload = lastReload;
		}

	}

}
