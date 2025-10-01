package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

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
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Blocking Component - Blocked Sound")
@Description("""
	The sound that plays when the item successfully blocks an attack.
	NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set {_sound} to the blocked sound of {_item}")
@Example("set the blocked sound of {_item} to \"minecraft:ui.toast.challenge_complete\"")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class ExprBlockCompBlockSound extends SimplePropertyExpression<BlockingWrapper, String> implements BlockingExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprBlockCompBlockSound.class, String.class, "blocked sound[s]", "blockingcomponents", true)
				.supplier(ExprBlockCompBlockSound::new)
				.build()
		);
	}

	@Override
	public @Nullable String convert(BlockingWrapper wrapper) {
		//noinspection UnstableApiUsage
		Key key = wrapper.getComponent().blockSound();
		return key == null ? null : key.toString();
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

		getExpr().stream(event).forEach(wrapper ->
			wrapper.editBuilder(builder -> builder.blockSound(key)));
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "blocked sound";
	}

}
