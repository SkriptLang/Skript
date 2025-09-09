package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

import ch.njol.skript.bukkitutil.SoundUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingWrapper;

public class ExprBlockCompDisableSound extends SimplePropertyExpression<BlockingWrapper, String> implements BlockingExperimentalSyntax {

	static {
		registerDefault(ExprBlockCompDisableSound.class, String.class, "[blocking] disable sound", "blockingcomponents");
	}

	@Override
	public @Nullable String convert(BlockingWrapper wrapper) {
		return wrapper.getComponent().disableSound().toString();
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
			wrapper.editBuilder(builder -> builder.disableSound(key)));
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "blocking disable sound";
	}

}
