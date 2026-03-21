package ch.njol.skript.expressions;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Version")
@Description("The version of Bukkit, Minecraft or Skript respectively.")
@Example("message \"This server is running Minecraft %minecraft version% on Bukkit %bukkit version%\"")
@Example("message \"This server is powered by Skript %skript version%\"")
@Since("2.0")
public class ExprVersion extends SimpleLiteral<String> {

	private enum VersionType {
		BUKKIT("Bukkit") {
			@Override
			public String get() {
				return Bukkit.getBukkitVersion();
			}
		},
		MINECRAFT("Minecraft") {
			@Override
			public String get() {
				return Skript.getMinecraftVersion().toString();
			}
		},
		SKRIPT("Skript") {
			@Override
			public String get() {
				return Skript.getVersion().toString();
			}
		};

		private final String name;

		VersionType(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		public abstract String get();
	}

	static {
		Skript.registerExpression(ExprVersion.class, String.class, ExpressionType.SIMPLE, "(0¦[craft]bukkit|1¦minecraft|2¦skript)( |-)version");
	}

	@SuppressWarnings("null")
	private VersionType versionType;

	public ExprVersion() {
		super(new String[0], String.class, false);
	}

	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		versionType = VersionType.values()[parseResult.mark];
		data = new String[] {versionType.get()};
		return true;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return versionType + " version";
	}

}
