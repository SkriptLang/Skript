package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import org.bukkit.BanEntry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Ban Date")
@Description("Returns the ban date of a player or IP.")
@Example("send the ban date of \"3.3.3.3\"")
@Since("INSERT VERSION")
public class ExprBanDate extends SimpleExpression<Date> {

	static {
		Skript.registerExpression(ExprBanDate.class, Date.class, ExpressionType.SIMPLE,
			"[the] date %offlineplayers/strings% (was|were) banned",
			"[the] date[s] of %offlineplayers/strings%'[s] ban",
			"[the] ban date[s] of %offlineplayers/strings%"
		);
	}

	private Expression<Object> targets;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		// noinspection unchecked
		targets = (Expression<Object>) expressions[0];
		return true;
	}

	@Override
	protected Date @Nullable [] get(Event event) {
		Object[] targets = this.targets.getAll(event);
		if (targets == null) {
			return null;
		}

		Date[] dates = new Date[targets.length];

		for (int i = 0; i < targets.length; i++) {
			Object target = targets[i];

			BanEntry<?> banEntry = BukkitUtils.getBanEntry(target);
			if (banEntry == null) {
				continue;
			}

			dates[i] = Date.fromJavaDate(banEntry.getCreated());
		}

		return dates;
	}

	@Override
	public boolean isSingle() {
		return targets.isSingle();
	}

	@Override
	public Class<? extends Date> getReturnType() {
		return Date.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the ban date of " + targets.toString(event, debug);
	}

}
