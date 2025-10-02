package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.bukkitutil.SoundUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Equip Sound")
@Description("""
	The sound to be played when the item is equipped.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set the equip sound of {_item} to \"entity.experience_orb.pickup\"")
@Example("""
	set {_component} to the equippable component of {_item}
	set the equip sound of {_component} to "block.note_block.pling"
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class ExprEquipCompEquipSound extends SimplePropertyExpression<EquippableWrapper, String> implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprEquipCompEquipSound.class, String.class, "equip sound", "equippablecomponents", true)
				.supplier(ExprEquipCompEquipSound::new)
				.build()
		);
	}

	@Override
	public @Nullable String convert(EquippableWrapper wrapper) {
		return wrapper.getComponent().equipSound().toString();
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
			if (enumSound == null) {
				error("Could not find a sound with the id '" + soundString + "'.");
				return;
			}
		}
		Key key;
		if (enumSound != null) {
			key = Registry.SOUNDS.getKey(enumSound);
		} else {
			key = null;
		}

		getExpr().stream(event).forEach(wrapper -> wrapper.editBuilder(builder -> builder.equipSound(key)));
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "equip sound";
	}

}
