package org.skriptlang.skript.bukkit.command.elements.expressions;

import java.lang.reflect.Array;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.command.brigadier.CommandParsingData;
import org.skriptlang.skript.bukkit.command.brigadier.ExecutorData;
import org.skriptlang.skript.bukkit.command.brigadier.ExecutorData.CooldownManager;
import org.skriptlang.skript.bukkit.command.brigadier.ScriptCommandEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Script Command Cooldown Information")
@Description("""
	Only usable in script commands.
	Represents the cooldown time, the cooldown bypass permission, the remaining cooldown time, the elapsed cooldown time.
	""")
@Example("""
	command /home:
		cooldown: 10 seconds
		cooldown message: You last teleported home %elapsed time% ago, you may teleport home again in %remaining time%.
		trigger:
			teleport player to {home::%player%}
	""")
@Since("2.2-dev33")
@Keywords("")
public class ExprCmdCooldownInfo extends SimpleExpression<Object> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprCmdCooldownInfo.class, ExprCmdCooldownInfo::new, Object.class,
				"[the] ((cooldown|wait) time|[wait] time of [the] (cooldown|wait) [(of|for) [the] [current] command])",
				"[the] [cooldown] bypass perm[ission] [of [the] (cooldown|wait) [(of|for) [the] [current] command]]",
				"[the] remaining [time] [of [the] (cooldown|wait) [(of|for) [the] [current] command]]",
				"[the] elapsed [time] [of [the] (cooldown|wait) [(of|for) [the] [current] command]]"));
	}

	private enum Type {
		COOLDOWN_TIME, BYPASS_PERMISSION, REMAINING_TIME, ELAPSED_TIME
	}

	private Type type;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = Type.values()[matchedPattern];
		CommandParsingData data = getParser().getData(CommandParsingData.class);
		if (!data.isParsingCooldownEntry && data.getExecutorData(ExecutorData::cooldownManager) == null) {
			Skript.error("'" + toString(null, false) + "' can't be used because the command doesn't have a cooldown.");
		}
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(ScriptCommandEvent.class);
	}

	@Override
	protected Object[] get(Event event) {
		if (!(event instanceof ScriptCommandEvent commandEvent)) {
			return (Object[]) Array.newInstance(getReturnType(), 0);
		}

		CooldownManager cooldownManager = commandEvent.getExecutor().getCooldownManager();
		assert cooldownManager != null;

		return switch (type) {
			case COOLDOWN_TIME -> new Timespan[]{cooldownManager.getCooldown()};
			case BYPASS_PERMISSION -> {
				String bypass = cooldownManager.getCooldownBypass();
				yield bypass == null ? new String[0] : new String[]{bypass};
			}
			case REMAINING_TIME -> {
				Date startDate = cooldownManager.getStartDate(event, commandEvent.getSender());
				Date now = Date.now();
				Timespan remaining;
				if (startDate == null) {
					remaining = new Timespan(0);
				} else {
					remaining = cooldownManager.getCooldown().subtract(now.difference(startDate));
				}
				yield new Timespan[]{remaining};
			}
			case ELAPSED_TIME -> {
				Date startDate = cooldownManager.getStartDate(event, commandEvent.getSender());
				Date now = Date.now();
				Timespan elapsed;
				if (startDate == null) {
					elapsed = new Timespan(0);
				} else {
					elapsed = now.difference(startDate);
				}
				yield new Timespan[]{elapsed};
			}
		};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (type) {
			case REMAINING_TIME, ELAPSED_TIME -> switch (mode) {
				case ADD, SET, REMOVE, DELETE, RESET -> CollectionUtils.array(Timespan.class);
				default -> null;
			};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof ScriptCommandEvent commandEvent)) {
			return;
		}

		CooldownManager cooldownManager = commandEvent.getExecutor().getCooldownManager();
		assert cooldownManager != null;

		Date startDate = cooldownManager.getStartDate(event, commandEvent.getSender());
		if (startDate == null) {
			return;
		}

		boolean isRemaining = type == Type.REMAINING_TIME;

		Timespan time;
		Date now = Date.now();
		if (isRemaining) {
			time = cooldownManager.getCooldown().subtract(now.difference(startDate));
		} else {
			time = now.difference(startDate);
		}

		time = switch (mode) {
			case ADD -> {
				assert delta != null;
				yield time.add((Timespan) delta[0]);
			}
			case SET -> {
				assert delta != null;
				yield (Timespan) delta[0];
			}
			case REMOVE -> {
				assert delta != null;
				yield time.subtract((Timespan) delta[0]);
			}
			case DELETE -> new Timespan(0);
			case RESET -> isRemaining ? cooldownManager.getCooldown() : new Timespan(0);
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};

		if (isRemaining) {
			if (time.compareTo(cooldownManager.getCooldown()) > 0) { // cap remaining time at cooldown max
				time = cooldownManager.getCooldown();
			}
			// convert to elapsed time
			time = cooldownManager.getCooldown().subtract(time);
		}

		now.subtract(time);
		cooldownManager.setStartDate(event, commandEvent.getSender(), now);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return type == Type.BYPASS_PERMISSION ? String.class : Timespan.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (type) {
			case COOLDOWN_TIME -> "the cooldown time";
			case BYPASS_PERMISSION -> "the bypass permission of the cooldown";
			case REMAINING_TIME -> "the remaining time of the cooldown";
			case ELAPSED_TIME -> "the elapsed time of the cooldown";
		};
	}

}
