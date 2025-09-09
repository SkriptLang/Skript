package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Registry;
import org.bukkit.damage.DamageType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingWrapper;

public class ExprBlockCompBypass extends SimplePropertyExpression<BlockingWrapper, DamageType> implements BlockingExperimentalSyntax {

	static {
		registerDefault(ExprBlockCompBypass.class, DamageType.class, "[blocking] damage type bypass", "blockingcomponents");
	}

	@Override
	public @Nullable DamageType convert(BlockingWrapper wrapper) {
		TagKey<DamageType> damageKey = wrapper.getComponent().bypassedBy();
		if (damageKey == null)
			return null;
		return Registry.DAMAGE_TYPE.get(damageKey.key());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(DamageType.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		DamageType provided = delta == null ? null : (DamageType) delta[0];
		TagKey<DamageType> key;
		if (provided != null) {
			key = TagKey.create(RegistryKey.DAMAGE_TYPE, provided.key());
		} else {
			key = null;
		}

		getExpr().stream(event).forEach(wrapper ->
			wrapper.editBuilder(builder -> builder.bypassedBy(key)));
	}

	@Override
	public Class<DamageType> getReturnType() {
		return DamageType.class;
	}

	@Override
	protected String getPropertyName() {
		return "blocking damage type bypass";
	}

}
