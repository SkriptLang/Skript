//package ch.njol.skript.conditions;
//
//import ch.njol.skript.Skript;
//import ch.njol.skript.doc.Description;
//import ch.njol.skript.doc.Examples;
//import ch.njol.skript.doc.Name;
//import ch.njol.skript.doc.Since;
//import ch.njol.skript.lang.Condition;
//import ch.njol.skript.lang.Expression;
//import ch.njol.skript.lang.SkriptParser;
//import ch.njol.util.Kleenean;
//import org.bukkit.event.Event;
//import org.eclipse.jdt.annotation.Nullable;
//
//
//
//@Name("Multiple Conditions")
//@Description("Allows you to use multiple conditions.")
//@Examples("if attacker is online && victim is online:")
//@Since("1.0.0")
//public class CondNegateBool extends Condition {
//
//	static {
//		Skript.registerCondition(ch.njol.skript.conditions.CondNegateBool.class,
//			"!%boolean%");
//	}
//
//	private Condition cond;
//
//	@Override
//	public boolean init(Expression<?>[] exprs, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
//		String unparsed1 = parseResult.regexes.get(0).group(0);
//		cond = Condition.parse(unparsed1, "Can't understand this condition: " + unparsed1);
//		return cond.isNegated();
//	}
//
//	@Override
//	public boolean check(Event event) {
//		return cond;
//	}
//
//	@Override
//	public String toString(@Nullable Event event, boolean b) {
//		return cond1.toString() + (isAnd ? " && " : " || ") + cond2.toString();
//	}
//}
//
