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
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Model")
@Description("""
	The model of the item when equipped.
	The model key is represented as a namespaced key.
	A namespaced key can be formatted as 'namespace:id' or 'id'. \
	It can only contain one ':' to separate the namespace and the id. \
	Only alphanumeric characters, periods, underscores, and dashes can be used.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set the equipped model key of {_item} to \"custom_model\"")
@Example("""
	set {_component} to the equippable component of {_item}
	set the equipped model id of {_component} to "custom_model"
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class ExprEquipCompModel extends SimplePropertyExpression<EquippableWrapper, String> implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprEquipCompModel.class, String.class, "equipped (model|asset) (key|id)", "equippablecomponents", true)
				.supplier(ExprEquipCompModel::new)
				.build()
		);
	}

	@Override
	public @Nullable String convert(EquippableWrapper wrapper) {
		Key key = wrapper.getAssetId();
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

		getExpr().stream(event).forEach(wrapper -> wrapper.setAssetId(finalKey));
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
