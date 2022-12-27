/**
 */
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
@Description("")
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
			"[a] new boss bar [name:(named|with title) %-string%]",
			"[a] new %color% boss bar [name:(named|with title) %-string%]"
		);
	}

	private @Nullable Expression<String> name;
	private @Nullable Expression<Color> color;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, final ParseResult result) {
		if (result.hasTag("name"))
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
		color:
		if (this.color != null) {
			@Nullable Color provided = this.color.getSingle(event);
			if (provided == null)
				break color;
			@Nullable DyeColor dye = provided.asDyeColor();
			if (dye == null)
				break color;
			color = getColor(dye);
		}
		if (name != null)
			bar = Bukkit.createBossBar(name.getSingle(event), color, BarStyle.SOLID);
		else
			bar = Bukkit.createBossBar(null, color, BarStyle.SOLID);
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

	static BarColor getColor(@Nullable DyeColor dye) {
		if (dye == null) return BarColor.PINK; // default to pink since it's the original
		return switch (dye) {
			case WHITE, LIGHT_GRAY -> BarColor.WHITE;
			case LIGHT_BLUE, BLACK, CYAN, BLUE -> BarColor.BLUE;
			case LIME, GRAY, GREEN -> BarColor.GREEN;
			case YELLOW -> BarColor.YELLOW;
			case PURPLE -> BarColor.PURPLE;
			case RED -> BarColor.RED;
			default -> BarColor.PINK;
		};
	}

	static DyeColor getDye(BarColor color) {
		return switch (color) {
			case PINK -> DyeColor.PINK;
			case BLUE -> DyeColor.BLUE;
			case RED -> DyeColor.RED;
			case GREEN -> DyeColor.GREEN;
			case YELLOW -> DyeColor.YELLOW;
			case PURPLE -> DyeColor.PURPLE;
			case WHITE -> DyeColor.WHITE;
		};
	}

}

package ch.njol.skript.expressions;


import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


@Name("Boss Bar")
@Description("The boss bar of a player")
@Examples({"set bossbar of player to \"Hello!\""})
@Since("INSERT VERSION")
public class ExprBossBar extends SimpleExpression<BossBar> {

	static {
		PropertyExpression.register(ExprBossBar.class, BossBar.class, "boss[ ]bars", "players");
	}

	// insertion order is important so this MUST be a LinkedHashSet
	private static Map<Player, LinkedHashSet<BossBar>> playerBossBarMap = new WeakHashMap<>();
	@Nullable
	private static BossBar lastBossBar;

	@Nullable
	public static LinkedHashSet<BossBar> getBossBarsForPlayer(Player player) {
		return playerBossBarMap.get(player);
	}

	@Nullable
	public static BossBar getLastBossBar() {
		return lastBossBar;
	}

	private Expression<Player> players;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		return true;
	}

	@Override
	protected BossBar[] get(Event event) {
		List<BossBar> allBars = new ArrayList<>();
		for (Player player : players.getArray(event)) {
			LinkedHashSet<BossBar> playerBars = getBossBarsForPlayer(player);
			if (playerBars != null) {
				allBars.addAll(playerBars);
			}
		}
		return allBars.toArray(new BossBar[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "bossbars of " + players.toString(event, debug);
	}

	@Override
	public Class<BossBar> getReturnType() {
		return BossBar.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case DELETE:
			case ADD:
			case SET:
				return new Class[]{String[].class, BossBar[].class};
			case REMOVE:
				return new Class[]{BossBar[].class};
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		for (Player player : players.getArray(event)) {
			switch (mode) {
				case DELETE:
					LinkedHashSet<BossBar> bossBars = getBossBarsForPlayer(player);
					if (bossBars != null) {
						for (BossBar bar : bossBars) {
							bar.removePlayer(player);
						}
						bossBars.clear();
					}
					break;
				case ADD:
					bossBars = getBossBarsForPlayer(player);
					if (bossBars == null) {
						bossBars = new LinkedHashSet<>();
						playerBossBarMap.put(player, bossBars);
					}
					for (Object objToAdd : delta) {
						BossBar newBar;
						if (objToAdd instanceof BossBar)
							newBar = (BossBar) objToAdd;
						else
							newBar = Bukkit.createBossBar((String) objToAdd, BarColor.WHITE, BarStyle.SOLID);
						newBar.addPlayer(player);
						bossBars.add(newBar);
						lastBossBar = newBar;
					}
					break;
				case SET:
					change(event, null, ChangeMode.DELETE);
					change(event, delta, ChangeMode.ADD);
					break;
				case REMOVE:
					bossBars = getBossBarsForPlayer(player);
					if (bossBars != null) {
						for (Object bossBarToRemove : delta) {
							((BossBar) bossBarToRemove).removePlayer(player);
							bossBars.remove(bossBarToRemove);
						}
					}
					break;
			}
		}
	}

}
