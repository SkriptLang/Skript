package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Name("Load Server Icon")
@Description({
	"Loads server icons from the given files. You can get the loaded icon using the",
	"<a href='expressions.html#ExprLastLoadedServerIcon'>last loaded server icon</a> expression.",
	"Please note that the image must be 64x64 and the file path starts from the server folder."
})
@Examples({
	"on load:",
		"\tclear {server-icons::*}",
		"\tloop 5 times:",
			"\t\tload server icon from file \"icons/%loop-number%.png\"",
			"\t\tadd the last loaded server icon to {server-icons::*}",
	"",
	"on server list ping:",
		"\tset the icon to a random server icon out of {server-icons::*}"
})
@Since("2.3")
@RequiredPlugins("Paper 1.12.2+")
public class EffLoadServerIcon extends AsyncEffect implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffLoadServerIcon.class, "load [the] server icon (from|of) [the] [image] [file] %string%");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	public static @Nullable CachedServerIcon lastLoaded = null;

	private Node node;
	private Expression<String> path;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		getParser().setHasDelayBefore(Kleenean.TRUE);
		if (!PAPER_EVENT_EXISTS) {
			Skript.error("The load server icon effect requires Paper 1.12.2 or newer");
			return false;
		}
		node = getParser().getNode();
		path = (Expression<String>) exprs[0];
		return true;
	}

    @Override
    protected void execute(Event event) {
		String pathString = path.getSingle(event);
		if (pathString == null) {
			error("The path string was null.", path.toString(null, false));
			return;
		}
		
		Path path = Paths.get(pathString);
		if (Files.isRegularFile(path)) {
			try {
				lastLoaded = Bukkit.loadServerIcon(path.toFile());
			} catch (NullPointerException | IllegalArgumentException ignored) {
			} catch (Exception ex) {
				Skript.exception(ex);
			}
		}
    }

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "load server icon from file " + path.toString(event, debug);
	}

}