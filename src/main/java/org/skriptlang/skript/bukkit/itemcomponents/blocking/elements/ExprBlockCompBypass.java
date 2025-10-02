package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
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
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Blocking Component - Damage Type Bypass")
@Description("""
	The damage type that can bypass when the item is blocking.
	NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set {_bypass} to the damage type bypass of {_item}")
@Example("set the damage type bypass of {_item} to magic")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class ExprBlockCompBypass extends SimplePropertyExpression<BlockingWrapper, DamageType> implements BlockingExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprBlockCompBypass.class,
				DamageType.class,
				"[blocking] damage type bypass[es]",
				"blockingcomponents",
				true
			)
				.supplier(ExprBlockCompBypass::new)
				.build()
		);
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
