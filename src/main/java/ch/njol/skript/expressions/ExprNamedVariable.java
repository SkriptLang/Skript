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

@Name("Named Variable (Experimental)")
@Examples({
	"""
		variable test is a text
		
		example:
			set &test to "hello"
			send &test to the player"""
})
@Since("INSERT VERSION (experimental)")
public class ExprNamedVariable extends ExprAbstractVariable {

	static {
		Skript.registerExpression(ExprNamedVariable.class, Object.class, ExpressionType.SIMPLE,
			pattern(VariableTypes.REGULAR)
		);
	}

	@Override
	public boolean init(int pattern, String name) {
		ParserInstance parser = this.getParser();
		VariableData data = parser.getData(VariableData.class);
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
		return VariableTypes.REGULAR;
	}

}
