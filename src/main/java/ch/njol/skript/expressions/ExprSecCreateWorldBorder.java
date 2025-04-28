package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Create Virtual WorldBorder")
@Description({
    "Creates a new virtual worldborder that can be customized inside a section.",
    "Each time this expression is used, a fresh worldborder is created.",
    "You can apply the customized worldborder to a player or to a world.",
    "Setting a world's worldborder to a virtual border makes it a real, visible border."
})
@Examples({
    "on join:",
    "\tset player's worldborder to a virtual worldborder",
    "\tset {_center} to location of player",
    "\tset {_border} to a virtual worldborder:",
    "\t\tset worldborder radius to 50",
    "\t\tset world border center of event-worldborder {_center}",
    "\tset player's worldborder to {_border}",
    "",
    "on join:",
    "\tset {_worldborder} to a virtual worldborder:",
    "\t\tset worldborder radius to 200",
    "\t\tset worldborder center of event-worldborder to location(0, 64, 0)",
    "\t\tset worldborder warning distance of event-worldborder to 5",
    "\tset worldborder of world \"world\" to {_worldborder}"
})
@Since("2.11")
public class ExprSecCreateWorldBorder extends SectionExpression<WorldBorder> {

	static {
		Skript.registerExpression(ExprSecCreateWorldBorder.class, WorldBorder.class, ExpressionType.SIMPLE, "a [virtual] world[ ]border");
		EventValues.registerEventValue(CreateWorldborderEvent.class, WorldBorder.class, CreateWorldborderEvent::getWorldBorder);
	}

	private WorldBorder worldBorder;
	private Trigger trigger = null;

	@Override
	public boolean init(Expression[] expressions, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List list) {
		worldBorder = Bukkit.createWorldBorder();
		if (node != null) {
			AtomicBoolean isDelayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> isDelayed.set(!getParser().getHasDelayBefore().isFalse());
			trigger = loadCode(node, "create worldborder", afterLoading, CreateWorldborderEvent.class);
			if (isDelayed.get()) {
				Skript.error("Delays cannot be used within a 'create worldborder' section.");
				return false;
			}
		}
		return true;
	}

	@Override
	protected WorldBorder @Nullable [] get(Event event) {
		if (trigger == null) 
			return new WorldBorder[] {worldBorder};
		CreateWorldborderEvent worldborderEvent = new CreateWorldborderEvent(worldBorder);
		Variables.withLocalVariables(event, worldborderEvent, () -> TriggerItem.walk(trigger, worldborderEvent));
		return new WorldBorder[] {worldborderEvent.getWorldBorder()};
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

	public static class CreateWorldborderEvent extends Event {
		private final WorldBorder worldborder;

		public CreateWorldborderEvent(WorldBorder worldborder) {
			this.worldborder = worldborder;
		}

		public WorldBorder getWorldBorder() {
			return worldborder;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}

	}

}
