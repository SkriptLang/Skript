package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.variable.VariableData;
import ch.njol.skript.registrations.Feature;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;

import static ch.njol.skript.expressions.variable.ExprAbstractVariable.VARIABLE_NAME;

@Name("Variable Declaration")
@Description("""
	Registers a variable for the current script file.
	The variable will be usable between structures.
	
	Use the `global` prefix to register a variable that is also accessible from other scripts.
	""")
@Examples({
	"global variable server_name is a text",
	"variable participants is a list"
})
@Since("INSERT VERSION (experimental)")
public class StructVariable extends Structure {

	public static final Priority PRIORITY = new Priority(30);

	static {
		ParserInstance.registerData(VariableData.class, VariableData::new);
		Skript.registerSimpleStructure(StructVariable.class,
			"global variable <" + VARIABLE_NAME.pattern() + "> is [a[n]] %*classinfo%",
			"variable <" + VARIABLE_NAME.pattern() + "> is [a[n]] %*classinfo%"
		);
	}

	private String name;
	private boolean global;
	private ClassInfo<?> type;

	@Override
	public boolean init(Literal<?> @NotNull [] arguments, int pattern, ParseResult result,
						@Nullable EntryContainer container) {
		if (!this.getParser().hasExperiment(Feature.VARIABLES))
			return false;
		this.type = (ClassInfo<?>) arguments[0];
		this.name = result.regexes.get(0).group().trim();
		this.global = pattern == 0;
		VariableData data = this.getParser().getData(VariableData.class);
		return data.registerNamedVariable(this.getParser().getCurrentScript(), name, type, global);
	}

	@Override
	public boolean load() {
		return true;
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (global ? "global " : "") + "variable " + name + " is a " + type.toString(event, debug);
	}

}
