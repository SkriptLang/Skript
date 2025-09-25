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

@Name("Incremental Mob Spawn Amount")
@Description("""
	Returns the incremental (concurrent) mob spawn amount of the trial spawner data.

	The incremental mob spawn amount determines how many additional mobs are added to the total mob spawn amount \
	for each tracked player beyond the first.
	For example, if the incremental mob spawn amount is 2 \
	and there are 2 tracked players, 2 extra mobs are added (for the second player), resulting in 8 total mobs \
	when combined with the base amount of 6. By default, the incremental mob spawn amount is 2.

	The incremental simultaneous mob spawn amount works the same way, but applies to mobs that spawn at once. \
	This value determines how many additional simultaneous spawns are added for each tracked player beyond the first.
	For example, if the incremental simultaneous mob spawn amount is 5 and there are 3 tracked players, \
	10 extra mobs are added (for the second & third player), resulting in 12 simultaneous spawns when combined with \
	the base amount of 2. By default, the incremental simultaneous mob spawn amount is 2.

	The trial spawner will continue spawning mobs until the total mob spawn amount is reached, \
	with the number of entities present at once capped by the total simultaneous spawn amount \
	as seen in the formula below.

	The formulas are:
	```
	total mob spawn amount = base mob spawn amount + (incremental mob spawn amount × (tracked player count - 1))
	total simultaneous mob spawn amount = base simultaneous mob spawn amount + (incremental simultaneous mob spawn amount × (tracked player count - 1))
	```
	""")
@Example("""
	set {_data} to trial spawner data of event-block
	set incremental mob spawn amount of {_data} to 10
	add 2 to incremental concurrent entity spawn amount of {_data}
	""")
@Example("""
	modify the trial spawner data of event-block:
		add 5 to incremental mob spawn amount
		remove 3 from additional simultaneous mob spawn amount
		reset additional mob spawn amount
		add 10 to incremental concurrent entity spawn amount
	""")
@Since("INSERT VERSION")
public class ExprIncrementalEntityCount extends SimplePropertyExpression<SkriptTrialSpawnerData, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprIncrementalEntityCount.class, Integer.class,
			"(incremental|additional) [concurrent:(concurrent|simultaneous)] (mob|entity) [spawn] (count|amount)[s]", "trialspawnerdatas", true)
				.supplier(ExprIncrementalEntityCount::new)
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
	public @Nullable Integer convert(SkriptTrialSpawnerData data) {
		if (concurrent)
			return data.getConcurrentMobAmountIncrement();
		return data.getBaseMobAmountIncrement();
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
			int base = concurrent ? data.getConcurrentMobAmountIncrement() : data.getBaseMobAmountIncrement();
			int value = switch (mode) {
				case ADD -> base + count;
				case REMOVE -> base - count;
				case RESET -> concurrent ? SpawnerUtils.DEFAULT_CONCURRENT_PER_PLAYER_INCREMENT : SpawnerUtils.DEFAULT_BASE_PER_PLAYER_INCREMENT;
				default -> count;
			};

			if (concurrent) {
				data.setConcurrentMobAmountIncrement(value);
			} else {
				data.setBaseMobAmountIncrement(value);
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "incremental" + (concurrent ? " concurrent " : " ") + "mob spawn count";
	}

}
