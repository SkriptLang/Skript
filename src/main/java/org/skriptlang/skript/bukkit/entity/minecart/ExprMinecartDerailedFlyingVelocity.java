package org.skriptlang.skript.bukkit.entity.minecart;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Name("Minecart Derailed / Flying Velocity")
@Description("The velocity of a minecart as soon as it has been derailed or as soon as it starts flying.")
@Example("""
	on right click on minecart:
		set derailed velocity of event-entity to vector 2, 10, 2
	""")
@Since("2.5.1")
public class ExprMinecartDerailedFlyingVelocity extends SimplePropertyExpression<Entity, Vector> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprMinecartDerailedFlyingVelocity.class,
				Vector.class,
				"[minecart] (derailed|:flying) velocity",
				"entities",
				false
			).supplier(ExprMinecartDerailedFlyingVelocity::new)
				.build()
		);
	}
	
	private boolean flying;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		flying = parseResult.hasTag("flying");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Vector convert(Entity entity) {
		if (entity instanceof Minecart minecart)
			return flying ? minecart.getFlyingVelocityMod() : minecart.getDerailedVelocityMod();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Vector.class);
			default -> null;
		};
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		Vector velocity = (Vector) delta[0];
		BiConsumer<Minecart, Vector> consumer;
		Function<Minecart, Vector> getter;
		if (flying) {
			consumer = Minecart::setFlyingVelocityMod;
			getter = Minecart::getFlyingVelocityMod;
		} else {
			consumer = Minecart::setDerailedVelocityMod;
			getter = Minecart::getDerailedVelocityMod;
		}
		for (Entity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Minecart minecart))
				continue;
			switch (mode) {
				case SET -> consumer.accept(minecart, velocity);
				case ADD -> consumer.accept(minecart, getter.apply(minecart).add(velocity));
				case REMOVE -> consumer.accept(minecart, getter.apply(minecart).subtract(velocity));
			}
		}
	}
	
	@Override
	protected String getPropertyName() {
		return (flying ? "flying" : "derailed") + " velocity";
	}
	
	
	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}
	
}
