package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Boss Bar")
@Description("""
	A new boss bar.
	Boss bars have several properties (a title/name, a color, a style, visibility and flags).
	For players to see the boss bar they must be added to it. Players may be added to multiple boss bars at a time.""")
@Examples({
	"set {bar} to a new boss bar",
	"set the name of {bar} to \"hello\"",
	"set the color of {bar} to red",
	"add player to {bar}",
	"set {bar} to a new pink boss bar named \"hello\""
})
@Since("INSERT VERSION")
public class ExprBossBar extends SimpleExpression<BossBar> {

	static {
		Skript.registerExpression(ExprBossBar.class, BossBar.class, ExpressionType.SIMPLE,
			"[a] [new] boss[ ]bar [(named|with name|titled|with title) %-string%]",
			"[a] [new] %color% boss[ ]bar [(named|with name|titled|with title) %-string%]"
		);
	}

	private @Nullable Expression<String> name;
	private @Nullable Expression<Color> color;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, final ParseResult result) {
		//noinspection unchecked
		this.name = (Expression<String>) expressions[pattern];
		if (pattern == 1)
			//noinspection unchecked
			this.color = (Expression<Color>) expressions[0];
		return true;
	}

	@Override
	protected BossBar[] get(Event event) {
		@NotNull BossBar bar;
		BarColor color = BarColor.PINK;
		if (this.color != null) {
			color = Utils.getBarColor(this.color
					.getOptionalSingle(event)
					.map(Color::asDyeColor)
					.orElse(DyeColor.PINK));
		}
		if (name != null) {
			bar = Bukkit.createBossBar(name.getSingle(event), color, BarStyle.SOLID);
		} else {
			bar = Bukkit.createBossBar(null, color, BarStyle.SOLID);
		}
		return new BossBar[] {bar};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends BossBar> getReturnType() {
		return BossBar.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		StringBuilder builder = new StringBuilder("a new ");
		if (color != null)
			builder.append(color.toString(event, debug)).append(" ");
		builder.append("boss bar");
		if (name != null)
			builder.append(" named ").append(name.toString(event, debug));
		return builder.toString();
	}

}
