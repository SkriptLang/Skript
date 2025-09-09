package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingWrapper;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.DamageFunctionWrapper;

@Name("Blocking Component - Item Damage Function")
@Description("""
	The item damage function of a blocking component.
	NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set {_damageFunction} to the damage function of {_item}")
@Example("set the item damage function of {_item} to a custom damage function")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class ExprBlockCompDamageFunction extends SimplePropertyExpression<BlockingWrapper, DamageFunctionWrapper> implements BlockingExperimentalSyntax {

	static {
		registerDefault(ExprBlockCompDamageFunction.class, DamageFunctionWrapper.class, "[item] damage function[s]",
			"blockingcomponents");
	}

	@Override
	public @Nullable DamageFunctionWrapper convert(BlockingWrapper wrapper) {
		return wrapper.getDamageFunction();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(DamageFunctionWrapper.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		DamageFunctionWrapper functionWrapper = (DamageFunctionWrapper) delta[0];

		getExpr().stream(event).forEach(wrapper ->
			wrapper.editBuilder(builder -> builder.itemDamage(functionWrapper.getDamageFunction())));
	}

	@Override
	public Class<DamageFunctionWrapper> getReturnType() {
		return DamageFunctionWrapper.class;
	}

	@Override
	protected String getPropertyName() {
		return "item damage function";
	}

}
