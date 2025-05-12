package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.bukkitutil.SoundUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Sound;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component - Equip Sound")
@Description("The sound to be played when the item is equipped. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Example("set the equip sound of {_item} to \"entity.experience_orb.pickup\"")
@Example("""
	set {_component} to the equippable component of {_item}
	set the equip sound of {_component} to "block.note_block.pling"
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprEquipCompSound extends PropertyExpression<EquippableWrapper, String> implements EquippableExperiment {

	static {
		register(ExprEquipCompSound.class, String.class, "equip sound", "equippablecomponents");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<EquippableWrapper>) exprs[0]);
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event, EquippableWrapper[] source) {
		return get(source, wrapper -> SoundUtils.getKey(wrapper.getComponent().getEquipSound()).getKey());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(String.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Sound enumSound = null;
		if (delta != null) {
			String soundString = (String) delta[0];
			enumSound = SoundUtils.getSound(soundString);
		}
		Sound finalSound = enumSound;

		getExpr().stream(event).forEach(wrapper -> wrapper.editComponent(component -> component.setEquipSound(finalSound)));
	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the equip sound of " + getExpr().toString(event, debug);
	}

}
