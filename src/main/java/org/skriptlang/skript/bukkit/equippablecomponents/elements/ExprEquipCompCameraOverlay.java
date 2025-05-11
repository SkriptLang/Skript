package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.equippablecomponents.EquippableExperiment;
import org.skriptlang.skript.bukkit.equippablecomponents.EquippableWrapper;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Equippable Component - Camera Overlay")
@Description({
	"The camera overlay for the player when the item is equipped.",
	"Example: The jack-o'-lantern view when having a jack-o'-lantern equipped as a helmet.",
	"Note that equippable component elements are experimental making them subject to change and may not work as intended."
})
@Examples({
	"set the camera overlay of {_item} to \"custom_overlay\"",
	"",
	"set {_component} to the equippable component of {_item}",
	"set the camera overlay of {_component} to \"custom_overlay\"",
	"set the equippable component of {_item} to {_component}"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprEquipCompCameraOverlay extends PropertyExpression<EquippableWrapper, String> implements EquippableExperiment, SyntaxRuntimeErrorProducer {

	static {
		register(ExprEquipCompCameraOverlay.class, String.class, "camera overlay", "equippablecomponents");
	}

	private Node node;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<EquippableWrapper>) exprs[0]);
		node = getParser().getNode();
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event, EquippableWrapper[] source) {
		return get(source, wrapper -> {
			NamespacedKey key = wrapper.getComponent().getCameraOverlay();
			return key == null ? null : key.toString();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(String.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		NamespacedKey key = null;
		if (delta != null && delta[0] instanceof String string) {
			boolean thrown = false;
			try {
				key = NamespacedKey.fromString(string);
			} catch (Exception ignored) {
				thrown = true;
			}
			if (thrown || key == null) {
				error("The key '" + string + "' is not in a valid format.");
				return;
			}
		}

		for (EquippableWrapper wrapper : getExpr().getArray(event)) {
			EquippableComponent component = wrapper.getComponent();
			component.setCameraOverlay(key);
			wrapper.applyComponent();
		}
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
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the camera overlay of " + getExpr().toString(event, debug);
	}

}
