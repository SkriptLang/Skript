package ch.njol.skript.effects;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;

@Name("Load Server Icon")
@Description({"Loads server icons from the given files. You can get the loaded icon using the",
		"<a href='#ExprLastLoadedServerIcon'>last loaded server icon</a> expression.",
		"Please note that the image must be 64x64 and the file path starts from the server folder.",})
@Examples({"on load:",
		"	clear {server-icons::*}",
		"	loop 5 times:",
		"		load server icon from file \"icons/%loop-number%.png\"",
		"		add the last loaded server icon to {server-icons::*}",
		"",
		"on server list ping:",
		"	set the icon to a random server icon out of {server-icons::*}"})
@Since("2.3")
@RequiredPlugins("Paper 1.12.2 or newer")
public class EffLoadServerIcon extends AsyncEffect {

	static {
		Skript.registerEffect(EffLoadServerIcon.class, "load [the] server icon (from|of) [the] [image] [file] %string%");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	@SuppressWarnings("null")
	private Expression<String> path;

	@Nullable
	public static CachedServerIcon lastLoaded = null;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		getParser().setHasDelayBefore(Kleenean.TRUE);
		if (!PAPER_EVENT_EXISTS) {
			Skript.error("The load server icon effect requires Paper 1.12.2 or newer");
			return false;
		}
		path = (Expression<String>) exprs[0];
		return true;
	}

    @Override
    protected void execute(Event e) {
		String pathString = path.getSingle(e);
		if (pathString == null)
			return;
		
		Path p = Paths.get(pathString);
		if (Files.isRegularFile(p)) {
			try {
				lastLoaded = Bukkit.loadServerIcon(p.toFile());
			} catch (NullPointerException | IllegalArgumentException ignored) {
			} catch (Exception ex) {
				Skript.exception(ex);
			}
		}
    }

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "load server icon from file " + path.toString(e, debug);
	}

}