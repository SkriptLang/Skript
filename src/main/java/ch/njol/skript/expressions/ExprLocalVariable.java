package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.variable.ExprAbstractVariable;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.variable.VariableChanger;
import ch.njol.skript.lang.variable.VariableData;
import ch.njol.skript.lang.variable.VariableType;
import ch.njol.skript.lang.variable.VariableTypes;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Local Variable (Experimental)")
@Examples({
	"""
		set _test to "hello"
		send _test to the player"""
})
@Since("INSERT VERSION (experimental)")
public class ExprLocalVariable extends ExprAbstractVariable {

	static {
		Skript.registerExpression(ExprLocalVariable.class, Object.class, ExpressionType.SIMPLE,
			pattern(VariableTypes.LOCAL)
		);
	}

	@Override
	public boolean init(int pattern, String name) {
		ParserInstance parser = this.getParser();
		VariableData data = parser.getData(VariableData.class);
		if (VariableChanger.class.isAssignableFrom(parser.getParsingStack().peek().getSyntaxElementClass()))
			data.declare(this.variableType(), name);
		if (!data.isDeclared(this.variableType(), name)) {
			VariableChanger.undeclaredVariableWarning(name);
			return false;
		}
		this.type = data.getClassInfo(this.variableType(), name);
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		return this.getRaw(event); // todo this is wrong
	}

	@Override
	public VariableType variableType() {
		return VariableTypes.LOCAL;
	}

}
