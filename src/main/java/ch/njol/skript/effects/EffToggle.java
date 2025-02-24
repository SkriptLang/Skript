package ch.njol.skript.effects;

import java.util.function.Function;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Toggle")
@Description("Toggle a boolean or the state of a block.")
@Examples({"# use arrows to toggle switches, doors, etc.",
		"on projectile hit:",
		"\tprojectile is arrow",
		"\ttoggle the block at the arrow",
		"",
		"# With booleans",
		"toggle gravity of player"
})
@Since("1.4, INSERT VERSION (booleans)")
public class EffToggle extends Effect {
	
	static {
		Skript.registerEffect(EffToggle.class,
			"(open|turn on|activate) %blocks%",
			"(close|turn off|de[-]activate) %blocks%",
			"(toggle|switch) [[the] state of] %booleans/blocks%");
	}

	private enum State {
		ACTIVATE, DEACTIVATE, TOGGLE
	}

	@SuppressWarnings("null")
	private Expression<?> togglables;
	private State state;
	
	@Override
	public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		togglables = (Expression<?>) vars[0];
		state = State.values()[matchedPattern];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Function<Object, Object> changeFunction = obj -> {
			if (obj instanceof Block) {
				Block block = (Block) obj;
				BlockData data = block.getBlockData();
				if (state == State.TOGGLE) {
					if (data instanceof Openable) { // open = NOT was open
						((Openable) data).setOpen(!((Openable) data).isOpen());
					} else if (data instanceof Powerable) { // power = NOT power
						((Powerable) data).setPowered(!((Powerable) data).isPowered());
					}
				} else {
					boolean value = state == State.ACTIVATE; 
					if (data instanceof Openable) {
						((Openable) data).setOpen(value);
					} else if (data instanceof Powerable) {
						((Powerable) data).setPowered(value);
					}
				}

				block.setBlockData(data);
				return block;

			} else if (obj instanceof Boolean && state == State.TOGGLE) {
				Boolean bool = (Boolean) obj;
				return !bool;
			}

			return obj;
		};

		togglables.changeInPlace(event, (Function) changeFunction);
		
	}
	
	@Override
	public String toString(@Nullable Event event, final boolean debug) {
		return "toggle " + togglables.toString(event, debug);
	}
	
}
