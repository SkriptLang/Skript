	package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnrule;

	import ch.njol.skript.Skript;
	import ch.njol.skript.config.SectionNode;
	import ch.njol.skript.expressions.base.SectionExpression;
	import ch.njol.skript.lang.Expression;
	import ch.njol.skript.lang.ExpressionType;
	import ch.njol.skript.lang.SkriptParser.ParseResult;
	import ch.njol.skript.lang.Trigger;
	import ch.njol.skript.lang.TriggerItem;
	import ch.njol.skript.variables.Variables;
	import ch.njol.util.Kleenean;
	import org.bukkit.block.spawner.SpawnRule;
	import org.bukkit.event.Event;
	import org.jetbrains.annotations.Nullable;
	import org.skriptlang.skript.bukkit.spawner.events.SpawnRuleCreateEvent;

	import java.util.List;
	
	public class ExprSecSpawnRule extends SectionExpression<SpawnRule> {
	
		static {
			Skript.registerExpression(ExprSecSpawnRule.class, SpawnRule.class, ExpressionType.SIMPLE,
				"[a] spawn rule"
			);
		}
	
		private Trigger trigger;
	
		@Override
		public boolean init(Expression<?>[] exprs, int pattern, Kleenean isDelayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
			if (node != null)
				//noinspection unchecked
				trigger = loadCode(node, "create loot context", null, SpawnRuleCreateEvent.class);
			return true;
		}
	
		@Override
		protected SpawnRule @Nullable [] get(Event event) {
			SpawnRule rule = new SpawnRule(0, 0, 0, 0);
			if (trigger != null) {
				SpawnRuleCreateEvent createEvent = new SpawnRuleCreateEvent(rule);
				Variables.withLocalVariables(event, createEvent, () ->
					TriggerItem.walk(trigger, createEvent)
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
			return "spawn rule";
		}
	
	}
