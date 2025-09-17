package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawner.spawnerdata;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.TrialSpawner;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerDataType;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptMobSpawnerData;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptSpawnerData;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptTrialSpawnerData;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Spawner Data")
@Description("""
    Returns the spawner data of a spawner. Trial spawners use different data in their regular and ominous states.
    Use 'ominous trial spawner data' to set data for the ominous state only, \
    or 'ominous and regular trial spawner data' to set data for both states at once.
    """)
@Example("""
	set the spawner data of event-block to the mob spawner data:
		set the spawn count to 5
		add 2 to the maximum nearby entity cap
		remove 5 from the activation range
	""")
@Example("""
	set {_data} to spawner data of event-block
	add {_spawner entries::*} to spawner entries of {_data}
	set spawn range of {_data} to 12
	add 6 to the activation range of {_data}
	set the spawner data of event-block to {_data}
	""")
@Example("""
	set {_trial data} to the trial spawner data:
		set the activation range to 32
		set the spawn range to 8
		add {_entries::*} to the spawner entries
		set the base entity spawn count to 12

	set the trial spawner data of event-block to {_trial data} # regular state
	set the ominous trial spawner data of event-block to {_trial data} # ominous state
	set the ominous and regular trial spawner datas of event-block to {_trial data} # both states
	""")
@Since("INSERT VERSION")
public class ExprSpawnerData extends PropertyExpression<Object, SkriptSpawnerData> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprSpawnerData.class, SkriptSpawnerData.class,
			"[trial:[:ominous|:regular|:ominous and regular] trial|:mob] spawner data[s]", "blocks/entities", false)
			.supplier(ExprSpawnerData::new)
			.build()
		);
	}

	private enum TrialSpawnerState {
		OMINOUS, REGULAR, BOTH;

		public static TrialSpawnerState fromTags(List<String> tags) {
			if (tags.contains("ominous")) {
				return OMINOUS;
			} else if (tags.contains("ominous and regular")) {
				return BOTH;
			} else {
				return REGULAR;
			}
		}
	}

	private SpawnerDataType dataType;
	private TrialSpawnerState state;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		dataType = SpawnerDataType.fromTags(parseResult.tags);
		state = TrialSpawnerState.fromTags(parseResult.tags);
		return true;
	}

	@Override
	protected SkriptSpawnerData[] get(Event event, Object[] source) {
		List<SkriptSpawnerData> datas = new ArrayList<>();

		for (Object spawnerObject : source) {
			if (!dataType.matches(spawnerObject))
				continue;

			if (SpawnerUtils.isMobSpawner(spawnerObject)) {
				datas.add(SkriptMobSpawnerData.fromSpawner(SpawnerUtils.getMobSpawner(spawnerObject)));
				continue;
			}

			TrialSpawner trialSpawner = SpawnerUtils.getTrialSpawner(spawnerObject);
			datas.addAll(switch (state) {
				case OMINOUS -> List.of(SkriptTrialSpawnerData.fromTrialSpawner(trialSpawner, true));
				case REGULAR -> List.of(SkriptTrialSpawnerData.fromTrialSpawner(trialSpawner, false));
				case BOTH -> List.of(
					SkriptTrialSpawnerData.fromTrialSpawner(trialSpawner, true),
					SkriptTrialSpawnerData.fromTrialSpawner(trialSpawner, false)
				);
			});
		}

		return datas.toArray(SkriptSpawnerData[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET -> CollectionUtils.array(dataType.getDataClass());
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		SkriptSpawnerData data = delta != null ? (SkriptSpawnerData) delta[0] : null;

		for (Object spawnerObject : getExpr().getArray(event)) {
			if (!dataType.matches(spawnerObject)) {
				continue;
			}

			if (data == null) {
				if (SpawnerUtils.isMobSpawner(spawnerObject)) {
					data = new SkriptMobSpawnerData();
				} else if (SpawnerUtils.isTrialSpawner(spawnerObject)) {
					data = new SkriptTrialSpawnerData();
				}
			}

			if (data == null) {
				continue;
			}

			if (data instanceof SkriptMobSpawnerData mobData) {
				mobData.applyData(SpawnerUtils.getMobSpawner(spawnerObject));
			} else if (data instanceof SkriptTrialSpawnerData trialData) {
				TrialSpawner trialSpawner = SpawnerUtils.getTrialSpawner(spawnerObject);
				switch (state) {
					case OMINOUS -> trialData.applyData(trialSpawner, true);
					case REGULAR -> trialData.applyData(trialSpawner, false);
					case BOTH -> {
						trialData.applyData(trialSpawner, true);
						trialData.applyData(trialSpawner, false);
					}
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle() && state != TrialSpawnerState.BOTH;
	}

	@Override
	public Class<? extends SkriptSpawnerData> getReturnType() {
		return dataType.getDataClass();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("the");
		if (dataType.isTrial()) {
			if (state == TrialSpawnerState.REGULAR) {
				builder.append("regular");
			} else if (state == TrialSpawnerState.OMINOUS) {
				builder.append("ominous");
			} else {
				builder.append("ominous and regular");
			}
		}

		builder.append(dataType.toString() + " spawner data of", getExpr());

		return builder.toString();
	}

}
