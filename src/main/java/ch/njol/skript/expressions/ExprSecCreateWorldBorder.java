package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
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

public class ExprSecCreateWorldBorder extends SectionExpression<WorldBorder> {

	static {
		Skript.registerExpression(ExprSecCreateWorldBorder.class, WorldBorder.class, ExpressionType.SIMPLE, "a new world[ ]border");
		EventValues.registerEventValue(CreateWorldborderEvent.class, WorldBorder.class, CreateWorldborderEvent::getWorldBorder, EventValues.TIME_NOW);
	}

	private WorldBorder worldBorder;
	private Trigger trigger = null;

	@Override
	public boolean init(Expression[] expressions, int pattern, Kleenean delayed, SkriptParser.ParseResult result, @Nullable SectionNode node, @Nullable List list) {
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

	@Nullable
	@Override
	protected WorldBorder[] get(Event event) {
		if (trigger == null) return new WorldBorder[] {worldBorder};
		CreateWorldborderEvent worldborderEvent = new CreateWorldborderEvent(worldBorder);
		Variables.setLocalVariables(worldborderEvent, Variables.copyLocalVariables(event));
		TriggerItem.walk(trigger, worldborderEvent);
		Variables.setLocalVariables(event, Variables.copyLocalVariables(worldborderEvent));
		Variables.removeLocals(worldborderEvent);
		if (worldborderEvent.getErrorInSection())
			return null;
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
		return "a new worldborder";
	}

	public static class CreateWorldborderEvent extends Event {
		private boolean errorInSection = false;
		private final WorldBorder worldborder;

		public CreateWorldborderEvent(WorldBorder worldborder) {
			this.worldborder = worldborder;
		}

		public WorldBorder getWorldBorder() {
			return worldborder;
		}

		public void setErrorInSection() {
			this.errorInSection = true;
		}


		public boolean getErrorInSection() {
			return errorInSection;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}
}
