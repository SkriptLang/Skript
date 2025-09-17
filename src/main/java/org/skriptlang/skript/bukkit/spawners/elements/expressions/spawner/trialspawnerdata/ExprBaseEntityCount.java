package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawner.trialspawnerdata;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptTrialSpawnerData;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Base Mob Spawn Amount")
@Description("""
	Returns the base (simultaneous) mob spawn amount of the trial spawner data.

	The base mob spawn amount is the total number of mobs spawned (by default 6) when there is one tracked player.

	The base simultaneous mob spawn amount is the number of mobs spawned at once (by default 2) when there is \
	one tracked player.

	For tracked players beyond the first one, the total simultaneous and not simultaneous mob spawn amounts \
	will be increased by the incremental mob spawn amounts.

	Once the total mob amount has been spawned, the trial spawner enters cooldown \
	and will not spawn entities again until it ends.
	""")
@Example("""
	set {_data} to trial spawner data of event-block
	set base mob spawn amount of {_data} to 10
	add 2 to base simultaneous entity spawn amount of {_data}
	broadcast "The base mob spawn amount of the trial spawner is %the base trial spawner mob count of {_data}%!"
	""")
@Example("""
	modify the trial spawner data of event-block:
		add 5 to base mob spawn amount
		remove 3 from base simultaneous mob spawn amount
		reset base mob spawn amount
		add 10 to base concurrent entity spawn amount
	""")
@Since("INSERT VERSION")
public class ExprBaseEntityCount extends SimplePropertyExpression<SkriptTrialSpawnerData, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprBaseEntityCount.class, Integer.class,
			"base [concurrent:(concurrent|simultaneous)] (mob|entity) [spawn] (count|amount)[s]", "trialspawnerdatas", true)
				.supplier(ExprBaseEntityCount::new)
				.build()
		);
	}

	private boolean concurrent;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		concurrent = parseResult.hasTag("concurrent");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Integer convert(SkriptTrialSpawnerData data) {
		if (concurrent)
			return data.getConcurrentMobAmount();
		return data.getBaseMobAmount();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int count = delta != null ? ((int) delta[0]) : 0;

		for (SkriptTrialSpawnerData data : getExpr().getArray(event)) {
			int base = concurrent ? data.getConcurrentMobAmount() : data.getBaseMobAmount();
			int value = switch (mode) {
				case ADD -> base + count;
				case REMOVE -> base - count;
				case RESET -> concurrent ? SpawnerUtils.DEFAULT_CONCURRENT_MOB_AMOUNT : SpawnerUtils.DEFAULT_BASE_MOB_AMOUNT;
				default -> count;
			};

			if (concurrent) {
				data.setConcurrentMobAmount(value);
			} else {
				data.setBaseMobAmount(value);
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "base" + (concurrent ? "concurrent " : " ") + "mob count";
	}

}
