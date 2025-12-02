package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

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
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Consumable Component - Consume Sound")
@Description("""
	The sound to be played when the item is being consumed.
	NOTE: Consumable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set {_sound} to the consumption sound of {_item}")
@Example("set the consumption sound of {_item} to \"minecraft:entity.sheep.ambient\"")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class ExprConsCompSound extends SimplePropertyExpression<ConsumableWrapper, String> implements ConsumableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprConsCompSound.class, String.class, "consum(e|ption) sound", "consumablecomponents", true)
				.supplier(ExprConsCompSound::new)
				.build()
		);
	}

	@Override
	public @Nullable String convert(ConsumableWrapper wrapper) {
		return wrapper.getComponent().sound().toString();
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
			String string = (String) delta[0];
			enumSound = SoundUtils.getSound(string);
			if (enumSound == null) {
				error("Could not find a sound with the id '" + string + "'.");
				return;
			}
		}

		Key key;
		if (enumSound != null) {
			key = Registry.SOUNDS.getKey(enumSound);
		} else {
			key = null;
		}

		getExpr().stream(event).forEach(wrapper ->
			wrapper.editBuilder(builder -> builder.sound(key)));
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "consume sound";
	}

}
