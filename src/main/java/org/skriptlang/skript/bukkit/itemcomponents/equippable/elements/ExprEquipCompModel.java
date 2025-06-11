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
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component - Model")
@Description({
	"The model of the item when equipped.",
	"The model key is represented as a namespaced key.",
	"A namespaced key can be formatted as 'namespace:id' or 'id'. "
		+ "It can only contain one ':' to separate the namespace and the id. "
		+ "Only alphanumeric characters, periods, underscores, and dashes can be used.",
	"NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended."
})
@Example("set the equipped model key of {_item} to \"custom_model\"")
@Example("""
	set {_component} to the equippable component of {_item}
	set the equipped model id of {_component} to "custom_model"
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprEquipCompModel extends SimplePropertyExpression<EquippableWrapper, String> implements EquippableExperiment {

	static {
		registerDefault(ExprEquipCompModel.class, String.class, "equipped model (key|id)", "equippablecomponents");
	}

	@Override
	public @Nullable String convert(EquippableWrapper wrapper) {
		NamespacedKey key = wrapper.getComponent().getModel();
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

		getExpr().stream(event).forEach(wrapper -> wrapper.editComponent(component -> component.setModel(finalKey)));
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "equipped model key";
	}

}
