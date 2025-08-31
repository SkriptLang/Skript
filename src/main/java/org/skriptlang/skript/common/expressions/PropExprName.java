package org.skriptlang.skript.common.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExpressionType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.PropertyHandler.NameHandler;

public class PropExprName extends PropertyBaseExpression<NameHandler<?,?>> {

	static {
		// Register the expression with Skript
		Skript.registerExpression(PropExprName.class, Object.class, ExpressionType.PROPERTY, "[the] property name[s] of %objects%");
	}

	@Override
	public Property<NameHandler<?, ?>> getProperty() {
		return Property.NAME;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> @Nullable Object convert(Event event, NameHandler<?, ?> handler, T source) {
		return ((NameHandler<T, ?>) handler).name(source);
	}


	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "name property of x";
	}
}
