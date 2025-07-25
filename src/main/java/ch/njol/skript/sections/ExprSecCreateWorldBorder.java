package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.expressions.base.SectionValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SectionEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Name("Create WorldBorder")
@Description({
    "Creates a new, unused world border. World borders can be assigned to either worlds or specific players.",
    "Borders assigned to worlds apply to all players in that world.",
    "Borders assigned to players apply only to those players, and different players can have different borders."
})
@Example("""
	on join:
		set {_location} to location of player
		set worldborder of player to a virtual worldborder:
			set worldborder radius to 25
			set world border center of event-worldborder to {_location}
	""")
@Example("""
	on load:
		set worldborder of world "world" to a worldborder:
			set worldborder radius of event-worldborder to 200
			set worldborder center of event-worldborder to location(0, 64, 0)
			set worldborder warning distance of event-worldborder to 5
	""")
@Since("2.11")
public class ExprSecCreateWorldBorder extends SectionExpression<WorldBorder> {

	static {
		Skript.registerExpression(ExprSecCreateWorldBorder.class, WorldBorder.class, ExpressionType.SIMPLE, "a [virtual] world[ ]border");
		SectionValueExpression.registerSectionValue(ExprSecCreateWorldBorder.class, WorldBorder.class);
	}

	private Trigger trigger = null;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			//noinspection unchecked
			trigger = SectionUtils.loadLinkedCode("create worldborder", (beforeLoading, afterLoading)
					-> loadCode(node, "create worldborder", beforeLoading, afterLoading, SectionEvent.class));
			return trigger != null;
		}
		return true;
	}

	@Override
	protected WorldBorder @Nullable [] get(Event event) {
		WorldBorder worldBorder = Bukkit.createWorldBorder();
		if (trigger == null) 
			return new WorldBorder[] {worldBorder};
		SectionEvent<WorldBorder> sectionEvent = new SectionEvent<>(worldBorder);
		Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		return new WorldBorder[] {sectionEvent.getObject()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<WorldBorder> getReturnType() {
		return WorldBorder.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a virtual worldborder";
	}

}
