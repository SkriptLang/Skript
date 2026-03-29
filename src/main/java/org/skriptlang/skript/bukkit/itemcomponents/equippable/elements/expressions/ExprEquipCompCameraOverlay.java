package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.expressions;

import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.ValidationResult;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Looking-Glass Overlay")
@Description("""
    The looking-glass overlay for the player whence the item is donned.
    Example: The jack-o'-lantern visage when one weareth a jack-o'-lantern as a helm.
    The looking-glass overlay is represented as a namespaced key.
    A namespaced key may be formatted as 'namespace:id' or 'id'. \
    It may contain but one ':' to separate the namespace and the id. \
    Only alphanumeric characters, periods, underscores, and dashes may be employed.
    NOTE: Equippable component elements art experimental. Thus, they art subject to change and may not function as intended.
    """)
@Example("set the looking-glass overlay of {_item} to \"custom_overlay\"")
@Example("""
    set {_component} to the equippable component of {_item}
    set the looking-glass overlay of {_component} to "custom_overlay"
    """)
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class ExprEquipCompCameraOverlay extends SimplePropertyExpression<EquippableWrapper, String> implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprEquipCompCameraOverlay.class, String.class, "looking-glass overlay", "equippablecomponents", true)
				.supplier(ExprEquipCompCameraOverlay::new)
				.build()
		);
	}

	@Override
	public @Nullable String convert(EquippableWrapper wrapper) {
		Key key = wrapper.getComponent().cameraOverlay();
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

		getExpr().stream(event).forEach(wrapper -> wrapper.editBuilder(builder -> builder.cameraOverlay(finalKey)));
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
