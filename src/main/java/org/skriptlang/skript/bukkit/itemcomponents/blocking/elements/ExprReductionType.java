package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.registry.set.RegistryKeySet;
import org.bukkit.Registry;
import org.bukkit.damage.DamageType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentUtils;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.DamageReductionWrapper;

import java.util.ArrayList;
import java.util.List;

public class ExprReductionType extends PropertyExpression<DamageReductionWrapper, DamageType> implements BlockingExperimentalSyntax {

	static {
		registerDefault(ExprReductionType.class, DamageType.class, "[damage] reduction damage types", "damagereductions");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<DamageReductionWrapper>) exprs[0]);
		return true;
	}

	@Override
	protected DamageType[] get(Event event, DamageReductionWrapper[] source) {
		List<DamageType> types = new ArrayList<>();
		for (DamageReductionWrapper wrapper : source) {
			RegistryKeySet<DamageType> current = wrapper.getDamageReduction().type();
			types.addAll(ComponentUtils.registryKeySetToCollection(current, Registry.DAMAGE_TYPE));
		}
		return types.toArray(DamageType[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, REMOVE, ADD -> CollectionUtils.array(DamageType[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		List<DamageType> provided = new ArrayList<>();
		if (delta != null) {
			for (Object object : delta) {
				if (object instanceof DamageType damageType)
					provided.add(damageType);
			}
		}

		getExpr().stream(event).forEach(wrapper -> {
			RegistryKeySet<DamageType> types = wrapper.getDamageReduction().type();
			List<DamageType> current = new ArrayList<>(ComponentUtils.registryKeySetToCollection(types, Registry.DAMAGE_TYPE));
			switch (mode) {
				case SET -> {
					current.clear();
					current.addAll(provided);
				}
				case ADD -> current.addAll(provided);
				case REMOVE -> current.removeAll(provided);
				case DELETE -> current.clear();
			}
			wrapper.modify(builder -> builder.types(current));
		});
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<DamageType> getReturnType() {
		return DamageType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the damage reduction damage types of", getExpr())
			.toString();
	}

}
