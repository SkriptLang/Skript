package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawner.spawnerdata;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerDataType;
import org.skriptlang.skript.bukkit.spawners.util.events.MobSpawnerDataEvent;
import org.skriptlang.skript.bukkit.spawners.util.events.SpawnerDataEvent;
import org.skriptlang.skript.bukkit.spawners.util.events.TrialSpawnerDataEvent;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptSpawnerData;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.StringJoiner;

@Name("Event Spawner Data")
@Description("""
    Returns the spawner data associated with the current event. This is only available within spawner data sections.
    Use 'trial spawner data' exclusively in trial spawner data sections, \
    and 'mob spawner data' exclusively in mob spawner data sections.
    """)
@Example("""
	modify the mob spawner data of event-block:
		broadcast the mob spawner data

	set {_data} to the mob spawner data:
		broadcast the mob spawner data
	""")
@Example("""
	modify the trial spawner data of event-block:
		broadcast the trial spawner data

	set {_data} to the trial spawner data:
		broadcast the trial spawner data
	""")
@Example("""
	modify the spawner data of event-block:
		broadcast the spawner data
	""")
@Since("INSERT VERSION")
public class ExprEventSpawnerData extends SimpleExpression<SkriptSpawnerData> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprEventSpawnerData.class, SkriptSpawnerData.class)
			.supplier(ExprEventSpawnerData::new)
			.priority(SyntaxInfo.SIMPLE)
			.addPattern("[the] [:mob|:trial] spawner data")
			.build()
		);
	}

	private SpawnerDataType dataType;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		dataType = SpawnerDataType.fromTags(parseResult.tags);

		if (dataType.isTrial() && !getParser().isCurrentEvent(TrialSpawnerDataEvent.class)) {
			Skript.error("'trial spawner data' can only be used in the trial spawner data sections.");
			return false;
		} else if (dataType.isMob() && !getParser().isCurrentEvent(MobSpawnerDataEvent.class)) {
			Skript.error("'mob spawner data' can only be used in the mob spawner data sections.");
			return false;
		}

		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(SpawnerDataEvent.class);
	}

	@Override
	protected SkriptSpawnerData @Nullable [] get(Event event) {
		if (!(event instanceof SpawnerDataEvent<?> dataEvent))
			return null;

		return new SkriptSpawnerData[]{dataEvent.getSpawnerData()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends SkriptSpawnerData> getReturnType() {
		return dataType.getDataClass();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		StringJoiner joiner = new StringJoiner(" ", "the", "spawner data");
		joiner.add(dataType.toString());
		return joiner.toString();
	}

}
