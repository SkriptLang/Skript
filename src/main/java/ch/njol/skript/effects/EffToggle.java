package ch.njol.skript.effects;

import java.util.function.Function;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;

@Name("Toggle")
@Description("Toggle the state of a block or boolean.")
@Example("""
	# use arrows to toggle switches, doors, etc.
	on projectile hit:
		projectile is arrow
		toggle the block at the arrow
	""")
@Example("""
	# With booleans
	toggle gravity of player
	""")
@Since("1.4, 2.12 (booleans)")
public class EffToggle extends Effect {

	private enum Action {
		ACTIVATE, DEACTIVATE, TOGGLE;
		
		public boolean apply(boolean current) {
			return switch(this) {
				case ACTIVATE -> true;
				case DEACTIVATE -> false;
				case TOGGLE -> !current;
			};
		}
	}

	private enum Type {
		BLOCKS, BOOLEANS, MIXED;
		
		/**
		 * Determines the appropriate Type based on the return type of an expression.
		 * @param expression The expression to determine the type of.
		 * @return The corresponding Type
		 */
		public static Type fromClass(Expression<?> expression) {
			boolean isBlockType = expression.canReturn(Block.class);
			boolean isBooleanType = expression.canReturn(Boolean.class);
			
			if (isBlockType && !isBooleanType) {
				return BLOCKS;
			} else if (isBooleanType && !isBlockType) {
				return BOOLEANS;
			} else {
				return MIXED;
			}
		}
	}

	private static final Patterns<Action> patterns = new Patterns<>(new Object[][]{
		{"(open|turn on|activate) %blocks%", Action.ACTIVATE},
		{"(close|turn off|de[-]activate) %blocks%", Action.DEACTIVATE},
		{"(toggle|switch) [[the] state of] %blocks/booleans%", Action.TOGGLE}
	});

	static {
		Skript.registerEffect(EffToggle.class, patterns.getPatterns());
	}

	private Expression<?> togglables;
	private Action action;
	private Type type;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		togglables = expressions[0];
		action = patterns.getInfo(matchedPattern);

		// Determine expression type using the enum method
		type = Type.fromClass(togglables);
		
		// Validate based on type
		if (type == Type.BOOLEANS && 
			!ChangerUtils.acceptsChange(togglables, ChangeMode.SET, Boolean.class)) {
			Skript.error("Cannot toggle '" + togglables + "' as it cannot be set to booleans.");
			return false;
		} else if (type == Type.MIXED && !ChangerUtils.acceptsChange(togglables, ChangeMode.SET, Block.class, Boolean.class)) {
			Skript.error("Cannot toggle '" + togglables + "' as it cannot be set to both blocks and booleans.");
			return false;
		}

		return true;
	}

	@Override
	protected void execute(Event event) {
		switch (type) {
			case BOOLEANS -> toggleBooleans(event);
			case BLOCKS -> toggleBlocks(event);
			case MIXED -> toggleMixed(event);
		}
	}

	/**
	 * Toggles blocks by opening/closing or powering/unpowering them.
	 * @param event the event used for evaluation
	 */
	private void toggleBlocks(Event event) {
		for (Object obj : togglables.getArray(event)) {
			if (obj instanceof Block block) {
				toggleSingleBlock(block);
			}
		}
	}

	/**
	 * Toggles a single block, either by opening/closing or powering/unpowering it.
	 * @param block The block to toggle
	 */
	private void toggleSingleBlock(@NotNull Block block) {
		BlockData data = block.getBlockData();
		if (data instanceof Openable openable) {
			openable.setOpen(action.apply(openable.isOpen()));
		} else if (data instanceof Powerable powerable) {
			powerable.setPowered(action.apply(powerable.isPowered()));
		}
		block.setBlockData(data);
	}

	/**
	 * Uses {@link Expression#changeInPlace(Event, Function)} to toggle booleans.
	 * @param event the event used for evaluation
	 */
	private void toggleBooleans(Event event) {
		togglables.changeInPlace(event, (obj) -> {
			if (!(obj instanceof Boolean bool)) {
				return null;
			}
			return action.apply(bool);
		});
	}

	/**
	 * Uses {@link Expression#changeInPlace(Event, Function)} to toggle both blocks and booleans.
	 * @param event the event used for evaluation
	 */
	private void toggleMixed(Event event) {
		togglables.changeInPlace(event, (obj) -> {
			if (obj instanceof Block block) {
				toggleSingleBlock(block);
				return block;
			} else if (obj instanceof Boolean bool) {
				return action.apply(bool);
			}
			return obj;
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String actionText = switch (action) {
			case ACTIVATE -> "activate";
			case DEACTIVATE -> "deactivate";
			case TOGGLE -> "toggle";
		};
		return actionText + " " + togglables.toString(event, debug);
	}
}
