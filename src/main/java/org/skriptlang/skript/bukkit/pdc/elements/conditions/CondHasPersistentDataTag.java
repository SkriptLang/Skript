package org.skriptlang.skript.bukkit.pdc.elements.conditions;

import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.pdc.PDCUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Locale;

@Name("Has Persistent Data Tag")
@Description("""
	Checks if the specified objects have persistent data tags with the given keys.
	Keys should be in the format "namespace:key" or "key". If a namespace is omitted, "minecraft" will be used instead.
	If a key is invalid, it will be ignored and a warning will be logged.
	""")
@Example("""
	if player has persistent data tag "koth:capturePoint":
		add 1 to {points::%{team::%player%}%}
	""")
@Example("""
	if player's tool has persistent data tags "custom:damage" and "custom:owner":
		if data tag "custom:owner" of player's tool is not player:
			broadcast "You are not the owner of this tool!"
			stop
		if data tag "custom:damage" of player's tool > 10:
			broadcast "Your tool is heavily damaged!"
		else:
			broadcast "Your tool is in good condition."
	""")
@Since("2.15")
@Keywords({"pdc", "persistent data container", "custom data", "nbt"})
public class CondHasPersistentDataTag extends Condition {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			PropertyCondition.infoBuilder(
					CondHasPersistentDataTag.class, PropertyCondition.PropertyType.HAVE,
					"[persistent] data tag[s] %strings%", "objects")
				.supplier(CondHasPersistentDataTag::new)
				.build());
	}

	private Expression<String> keys;
	private Expression<Object> holders;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.holders = LiteralUtils.defendExpression(expressions[0]);
		this.keys = (Expression<String>) expressions[1];
		setNegated(matchedPattern == 1);
		return LiteralUtils.canInitSafely(this.holders);
	}

	@Override
	public boolean check(Event event) {
		boolean keysAnd = this.keys.getAnd();
		NamespacedKey[] keys = this.keys.stream(event)
			.map(key -> NamespacedUtils.checkValidationAndSend(key.toLowerCase(Locale.ENGLISH), this))
			.toArray(NamespacedKey[]::new);
		if (keys.length == 0)
			return isNegated();

		return this.holders.check(event, holder -> {
			var container = PDCUtils.getPersistentDataContainer(holder);
			if (container == null)
				return false;
			return SimpleExpression.check(keys, container::has, false, keysAnd);
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyCondition.PropertyType.HAVE, event, debug, holders, "persistent data tag " + keys.toString(event, debug));
	}

}
