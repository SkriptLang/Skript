package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Command Block Command")
@Description(
	"Gets or sets the command associated with a command block or minecart with command block."
)
@Examples({
	"send command of {_block}",
	"set command of {_cmdMinecart} to \"say asdf\""
})
@Since("INSERT VERSION")
public class ExprCommandBlockCommand extends SimplePropertyExpression<Object, String> {

	static {
		register(ExprCommandBlockCommand.class, String.class, "[command[ ]block] command", "blocks/entities");
	}

	@Override
	public @Nullable String convert(Object holder) {
		String command = "";
		if (holder instanceof Block block && block.getState() instanceof CommandBlock cmdBlock) {
			command = cmdBlock.getCommand();
		} else if (holder instanceof CommandMinecart cmdMinecart) {
			command = cmdMinecart.getCommand();
		}
		return (command.isEmpty()) ? null : command;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE || mode == ChangeMode.RESET)
			return CollectionUtils.array(String.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		String newCommand = delta == null ? null : ((String) delta[0]);
		for (Object holder : getExpr().getArray(event)) {
			switch (mode) {
				case RESET:
				case DELETE:
				case SET:
					if (holder instanceof Block block && block.getState() instanceof CommandBlock cmdBlock) {
						cmdBlock.setCommand(newCommand);
						cmdBlock.update();
					} else if (holder instanceof CommandMinecart cmdMinecart) {
						cmdMinecart.setCommand(newCommand);
					}
					break;
				default:
					assert false;
			}
		}
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "command block command";
	}

}
