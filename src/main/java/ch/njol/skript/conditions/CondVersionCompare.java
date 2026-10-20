package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Version;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Version Comparison")
@Description({
	"Compares the running Skript, Minecraft or server version against a given version string (e.g. \"2.15\").",
	"This condition has no event requirements, so it can be used in 'parse if' sections " +
		"to prevent code requiring a newer version from being parsed on older versions."
})
@Example("""
	parse if skript version is at least "2.15":
		set data tag "myplugin:flag" of event-entity to true
	""")
@Example("""
	parse if minecraft version is below "1.20":
		# old-syntax fallback code
	""")
@Since("INSERT VERSION")
public class CondVersionCompare extends Condition {

	static {
		Skript.instance().syntaxRegistry().register(SyntaxRegistry.CONDITION,
			SyntaxInfo.simple(CondVersionCompare.class, CondVersionCompare::new,
				"[the] (0:skript|1:minecraft|2:server|3:bukkit) version (is|are) at least %string%",
				"[the] (0:skript|1:minecraft|2:server|3:bukkit) version (is|are) at most %string%",
				"[the] (0:skript|1:minecraft|2:server|3:bukkit) version (is|are) (newer|later|higher|greater|above) [than] %string%",
				"[the] (0:skript|1:minecraft|2:server|3:bukkit) version (is|are) (older|earlier|lower|smaller|less|below) [than] %string%",
				"[the] (0:skript|1:minecraft|2:server|3:bukkit) version (is|are)(n't| not) at least %string%",
				"[the] (0:skript|1:minecraft|2:server|3:bukkit) version (is|are)(n't| not) at most %string%",
				"[the] (0:skript|1:minecraft|2:server|3:bukkit) version (is|are)(n't| not) (newer|later|higher|greater|above) [than] %string%",
				"[the] (0:skript|1:minecraft|2:server|3:bukkit) version (is|are)(n't| not) (older|earlier|lower|smaller|less|below) [than] %string%"));
	}

	private int versionType;
	private int relation;
	private boolean negated;
	private @Nullable Version current;

	@SuppressWarnings("null")
	private Expression<String> required;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		versionType = parseResult.mark;
		relation = matchedPattern % 4;
		negated = matchedPattern >= 4;
		required = (Expression<String>) exprs[0];
		current = switch (versionType) {
			case 1 -> Skript.getMinecraftVersion();
			case 2, 3 -> {
				try {
					yield new Version(Bukkit.getBukkitVersion());
				} catch (IllegalArgumentException e) {
					yield null;
				}
			}
			default -> Skript.getVersion();
		};
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (current == null)
			return false;
		return required.check(event, target -> {
			int comparison;
			try {
				comparison = current.compareTo(new Version(target));
			} catch (IllegalArgumentException e) {
				return false;
			}
			boolean result = switch (relation) {
				case 0 -> comparison >= 0;
				case 1 -> comparison <= 0;
				case 2 -> comparison > 0;
				default -> comparison < 0;
			};
			return result != negated;
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String name = switch (versionType) {
			case 1 -> "minecraft";
			case 2 -> "server";
			case 3 -> "bukkit";
			default -> "skript";
		};
		String relation = switch (this.relation) {
			case 0 -> "at least";
			case 1 -> "at most";
			case 2 -> "newer than";
			default -> "older than";
		};
		return name + " version is " + (negated ? "not " : "") + relation + " " + required.toString(event, debug);
	}

}
