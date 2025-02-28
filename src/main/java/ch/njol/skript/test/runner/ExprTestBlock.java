package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Test Block")
@Description("The block the testing is taking place at.")
@Examples({
	"test \"example\":",
		"\tset test block to air"
})
@NoDoc
public class ExprTestBlock extends SimpleExpression<Block> {

	static {
		if (TestMode.ENABLED)
			Skript.registerExpression(ExprTestBlock.class, Block.class, ExpressionType.SIMPLE,
					"[the] test(-| )block");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected Block @Nullable [] get(Event event) {
		return new Block[]{SkriptJUnitTest.getBlock()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the test block";
	}

}
