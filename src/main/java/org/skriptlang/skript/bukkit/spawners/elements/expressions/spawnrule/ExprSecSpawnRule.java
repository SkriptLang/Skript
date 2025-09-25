package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawnrule;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.events.SpawnRuleEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("New Spawn Rule")
@Description("""
	Returns a new spawn rule.
	""")
@Example("""
	set {_rule} to the spawn rule:
		set the maximum block light spawn level to 12
		set the minimum block light spawn level to 8
		set the maximum sky light spawn level to 15
		set the minimum sky light spawn level to 4
	""")
@Example("""
	modify the spawner data of {_spawner}:
		loop the spawner entries:
			set the spawn rule to a spawn rule:
				set the maximum block light spawn level to 15
				set the minimum block light spawn level to 10
				set the maximum sky light spawn level to 15
				set the minimum sky light spawn level to 5
	""")
@Since("INSERT VERSION")
public class ExprSecSpawnRule extends SectionExpression<SpawnRule> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSecSpawnRule.class, SpawnRule.class)
			.supplier(ExprSecSpawnRule::new)
			.priority(SyntaxInfo.SIMPLE)
			.addPattern("[a|the] spawn rule")
			.build()
		);
	}

	private Trigger trigger;

	@Override
	public boolean init(
		Expression<?>[] exprs, int pattern, Kleenean isDelayed, ParseResult result,
		@Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems
	) {
		if (node != null) {
			trigger = SectionUtils.loadLinkedCode("spawn rule create", (beforeLoading, afterLoading) ->
				loadCode(node, "spawn rule create", beforeLoading, afterLoading, SpawnRuleEvent.class)
			);
			return trigger != null;
		}
		return true;
	}

	@Override
	protected SpawnRule @Nullable [] get(Event event) {
		SpawnRule rule = new SpawnRule(0, 0, 0, 0);
		if (trigger != null) {
			SpawnRuleEvent ruleEvent = new SpawnRuleEvent(rule);
			Variables.withLocalVariables(ruleEvent, ruleEvent, () ->
				TriggerItem.walk(trigger, ruleEvent)
			);
		}
		return new SpawnRule[]{rule};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends SpawnRule> getReturnType() {
		return SpawnRule.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the spawn rule";
	}

}
