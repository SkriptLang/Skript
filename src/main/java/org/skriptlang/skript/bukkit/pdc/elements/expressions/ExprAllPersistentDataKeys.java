package org.skriptlang.skript.bukkit.pdc.elements.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.pdc.PDCUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("All Persistent Data Keys")
@Description("""
	Returns all persistent data keys stored in the specified objects.
	This is not limited to tags set by Skript, but includes all keys regardless of their origin.
	""")
@Example("""
	set {_keys::*} to persistent data keys of player's tool
	if size of {_keys::*} > 0:
	    broadcast "The tool has the following persistent data keys: %{_keys::*}%"
	else:
	    broadcast "The tool has no persistent data keys."
	""")
@Example("""
	for each {_key} in persistent data keys of player's tool:
	    broadcast "Persistent data tag %{_key}%: %data tag {_key} of player's tool%"
	""")
@Since("INSERT VERSION")
@Keywords({"pdc", "persistent data container", "custom data", "nbt"})
public class ExprAllPersistentDataKeys extends PropertyExpression<Object, String> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprAllPersistentDataKeys.class, String.class)
				.addPatterns(
					"[all [[of] the]] [persistent] data [tag] keys of %objects%",
					"[all of] %objects%'[s] [persistent] data [tag] keys"
				)
				.priority(DEFAULT_PRIORITY)
				.supplier(ExprAllPersistentDataKeys::new)
				.build());
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(LiteralUtils.defendExpression(expressions[0]));
		return LiteralUtils.canInitSafely(getExpr());
	}

	@Override
	protected String[] get(Event event, Object[] source) {
		List<String> keys = new ArrayList<>();
		for (Object obj : source) {
			PDCUtils.getPersistentDataContainer(obj, container -> {
				for (NamespacedKey key : container.getKeys()) {
					keys.add(key.toString());
				}
			});
		}
		return keys.toArray(new String[0]);
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "persistent data keys of " + getExpr().toString(event, debug);
	}

}
