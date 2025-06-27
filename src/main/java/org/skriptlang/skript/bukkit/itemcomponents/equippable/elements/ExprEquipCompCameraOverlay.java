package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.ValidationResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component - Camera Overlay")
@Description({
	"The camera overlay for the player when the item is equipped.",
	"Example: The jack-o'-lantern view when having a jack-o'-lantern equipped as a helmet.",
	"The camera overlay is represented as a namespaced key.",
	"A namespaced key can be formatted as 'namespace:id' or 'id'. "
		+ "It can only contain one ':' to separate the namespace and the id. "
		+ "Only alphanumeric characters, periods, underscores, and dashes can be used.",
	"NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended."
})
@Example("set the camera overlay of {_item} to \"custom_overlay\"")
@Example("""
	set {_component} to the equippable component of {_item}
	set the camera overlay of {_component} to "custom_overlay"
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprEquipCompCameraOverlay extends SimplePropertyExpression<EquippableWrapper, String> implements EquippableExperimentSyntax {

	static {
		registerDefault(ExprEquipCompCameraOverlay.class, String.class, "camera overlay", "equippablecomponents");
	}

	@Override
	public @Nullable String convert(EquippableWrapper wrapper) {
		NamespacedKey key = wrapper.getComponent().getCameraOverlay();
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
		NamespacedKey key = null;
		if (delta != null && delta[0] instanceof String string) {
			ValidationResult<NamespacedKey> validationResult = NamespacedUtils.checkValidation(string);
			String validationMessage = validationResult.message();
			if (!validationResult.valid()) {
				error(validationMessage + ". " + NamespacedUtils.NAMEDSPACED_FORMAT_MESSAGE);
				return;
			} else if (validationMessage != null) {
				warning(validationMessage);
			}
			key = validationResult.data();
		}
		NamespacedKey finalKey = key;

		getExpr().stream(event).forEach(wrapper -> wrapper.editComponent(component -> component.setCameraOverlay(finalKey)));
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "camera overlay";
	}

}
