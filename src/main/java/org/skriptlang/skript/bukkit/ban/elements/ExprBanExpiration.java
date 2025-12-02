package org.skriptlang.skript.bukkit.ban.elements;

import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.BanEntry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Priority;

@Name("Ban Expiration Date")
@Description("The ban expiration date of an offline player or IP.")
@Example("send the ban expiration date of \"3.3.3.3\"")
@Example("add 5 days to the expiration date of {_p}'s ban")
@Example("set the expiration date of {_p}'s ban to 10 years later")
@Since("INSERT VERSION")
public class ExprBanExpiration extends SimpleExpression<Date> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprBanExpiration.class, Date.class)
				.addPatterns(
					"[the] expiration date[s] of %offlineplayers/strings%'[s] ban",
					"[the] ban expiration date[s] of %offlineplayers/strings%",
					"[the] date %offlineplayers/strings%'[s] ban expires"
				)
				.priority(Priority.base())
				.supplier(ExprBanExpiration::new)
				.build()
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
		Object[] targets = this.targets.getArray(event);
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
		return switch (mode) {
			case SET -> new Class<?>[]{ Date.class };
			case ADD, REMOVE -> new Class<?>[]{ Timespan.class };
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;

		Object[] targets = this.targets.getArray(event);
		if (targets == null) {
			return;
		}

		Date date = null;
		Timespan timespan = null;
		switch (mode) {
			case SET -> date = (Date) delta[0];
			case ADD, REMOVE -> timespan = (Timespan) delta[0];
		}

		for (Object target : targets) {
			BanEntry<?> banEntry = BukkitUtils.getBanEntry(target);
			if (banEntry == null) {
				continue;
			}

			Date expiration = Date.fromJavaDate(banEntry.getExpiration());
			Date newExpiration;

			switch (mode) {
				case SET -> newExpiration = date;
				case ADD -> newExpiration = expiration.plus(timespan);
				case REMOVE -> newExpiration = expiration.minus(timespan);
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
