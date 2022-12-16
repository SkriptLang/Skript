/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
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
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;
import java.util.WeakHashMap;


@Name("Boss Bar")
@Description("The boss bar of a player")
@Examples({"set bossbar of player to \"Hello!\""})
@Since("INSERT VERSION")
public class ExprBossBar extends SimplePropertyExpression<Player, BossBar> {

	static {
		register(ExprBossBar.class, BossBar.class, "boss[ ]bar", "players");
	}

	private final static Map<Player, BossBar> playerBossBarMap = new WeakHashMap<>();

	@Nullable
	public static BossBar getBossBarForPlayer(Player player) {
		return playerBossBarMap.get(player);
	}

	@Override
	@Nullable
	public BossBar convert(final Player player) {
		return getBossBarForPlayer(player);
	}

	@Override
	protected String getPropertyName() {
		return "bossbar";
	}

	@Override
	public Class<BossBar> getReturnType() {
		return BossBar.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return new Class[] {String.class};
		return null;
	}

	@Override
	public void change(final Event event, final @Nullable Object[] delta, final ChangeMode mode) {
		for (final Player player : getExpr().getArray(event)) {
			switch (mode) {
				case DELETE:
					BossBar bossBar = getBossBarForPlayer(player);
					if (bossBar != null) {
						bossBar.removePlayer(player);
						playerBossBarMap.remove(player);
					}
					break;
				case SET:
					bossBar = getBossBarForPlayer(player);
					if (bossBar == null) {
						bossBar = Bukkit.createBossBar((String) delta[0], BarColor.WHITE, BarStyle.SOLID);
						playerBossBarMap.put(player, bossBar);
					} else {
						bossBar.setTitle((String) delta[0]);
					}
					bossBar.addPlayer(player);
			}
		}
	}
}
