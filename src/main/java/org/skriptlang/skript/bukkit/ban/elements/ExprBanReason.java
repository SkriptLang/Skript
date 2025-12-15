package org.skriptlang.skript.bukkit.ban.elements;

import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.BanEntry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Priority;

@Name("Ban Reason")
@Description("The ban reason of a player or IP.")
@Example("send the ban reason of \"3.3.3.3\"")
@Example("set the reason {_p} was banned to \"hacking\"")
@Since("INSERT VERSION")
public class ExprBanReason extends SimpleExpression<String> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprBanReason.class, String.class)
				.addPatterns(
					"[the] reason %offlineplayers/strings% (was|were) banned",
					"[the] reason[s] for %offlineplayers/strings%'[s] ban",
					"[the] ban reason[s] of %offlineplayers/strings%"
				)
				.priority(Priority.base())
				.supplier(ExprBanReason::new)
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
	protected String @Nullable [] get(Event event) {
		Object[] targets = this.targets.getArray(event);
		if (targets == null) {
			return null;
		}

		String[] reasons = new String[targets.length];

		for (int i = 0; i < targets.length; i++) {
			Object target = targets[i];

			BanEntry<?> banEntry = BukkitUtils.getBanEntry(target);
			if (banEntry == null) {
				continue;
			}

			reasons[i] = banEntry.getReason();
		}

		return reasons;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return new Class<?>[]{ String.class };
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object[] targets = this.targets.getArray(event);

		assert delta != null;
		String reason = (String) delta[0];

		for (Object target : targets) {
			BanEntry<?> banEntry = BukkitUtils.getBanEntry(target);
			if (banEntry == null) {
				continue;
			}

			banEntry.setReason(reason);
			banEntry.save();
		}
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return targets.isSingle();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the reason " + targets.toString(event, debug) + " was banned";
	}

}
