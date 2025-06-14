package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Copy of Object")
@Description({
	"Get a copy of an object. Please note that not all objects are able to be copied.",
	"If an object is not able to be copied, the object will be excluded from the result.",
	"However, by including 'with fallback', you will get the original object, if it can not be copied.",
	"Unlike the copy effect, this will only make copies of the objects and does not copy variable indices."
})
@Example("set {_copy} to a copy of player's tool")
@Example("""
	set {_objects::*} to a diamond, chest[], and location(0, 0, 0)
	set {_copies::*} to the copies of {_objects::*}
	""")
@Example("""
	# This will return nothing because an entity can not be copied
	set {_copy} to the copy of last spawned entity
	
	# This will return the original object
	set {_copy} to the copy of last spawned entity with fallback
	""")
@Since("INSERT VERSION")
@Keywords({"copy"})
public class ExprCopy extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprCopy.class, Object.class, ExpressionType.SIMPLE,
			"[the|a] copy of %objects% [fallback:with fallback]",
			"[the] copies of %objects% [fallback:with fallback]",
			"[the] copied [objects of] %objects% [fallback:with fallback]");
	}

	private Expression<?> objects;
	private boolean withFallback;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = LiteralUtils.defendExpression(exprs[0]);
		withFallback = parseResult.hasTag("fallback");
		return LiteralUtils.canInitSafely(objects);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		List<Object> copies = new ArrayList<>();
		for (Object object : objects.getArray(event)) {
			ClassInfo classInfo = Classes.getSuperClassInfo(object.getClass());
			if (classInfo.getCloner() != null) {
				//noinspection unchecked
				copies.add(classInfo.clone(object));
			} else if (withFallback) {
				copies.add(object);
			}
		}
		return copies.toArray();
	}

	@Override
	public boolean isSingle() {
		return objects.isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the copies of", objects);
		if (withFallback)
			builder.append("with fallback");
		return builder.toString();
	}

}
