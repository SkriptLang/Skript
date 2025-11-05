package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.BanEntry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprBanExpiration extends SimpleExpression<Date> {

	static {
		Skript.registerExpression(ExprBanExpiration.class, Date.class, ExpressionType.SIMPLE,
			"[the] expiration date[s] of %offlineplayers/strings%'s ban",
			"[the] ban expiration date[s] of %offlineplayers/strings%"
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

		Date[] expirations = new Date[targets.length];

		for (int i = 0; i < targets.length; i++) {
			Object target = targets[i];

			BanEntry<?> banEntry = BukkitUtils.getBanEntry(target);
			if (banEntry == null) {
				continue;
			}

			expirations[i] = Date.fromJavaDate(banEntry.getExpiration());
		}

		return expirations;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return new Class<?>[]{ Date.class };
		}

		if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
			return new Class<?>[]{ Timespan.class };
		}

		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;

		Object[] targets = this.targets.getArray(event);
		if (targets == null) {
			return;
		}

		for (Object target : targets) {
			BanEntry<?> banEntry = BukkitUtils.getBanEntry(target);
			if  (banEntry == null) {
				continue;
			}

			Date expiration = Date.fromJavaDate(banEntry.getExpiration());
			Date newExpiration;

			switch (mode) {
				case SET -> newExpiration = (Date) delta[0];
				case ADD -> {
					Timespan timespan = (Timespan) delta[0];
					newExpiration = expiration.plus(timespan);
				}
				case REMOVE -> {
					Timespan timespan = (Timespan) delta[0];
					newExpiration = expiration.minus(timespan);
				}
				default -> {
					continue;
				}
			}

			banEntry.setExpiration(newExpiration);
			banEntry.save();
		}
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
		return "the ban expiration date of " +  targets.toString(event, debug);
	}

}
