package org.skriptlang.skript.bukkit.entity.mannequin.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mannequin Description")
@Description("""
	The description of a mannequin.
	The description is displayed below the display name of the mannequin.
	If the display name of the mannequin is not set, the description will not show.
	The default description of a mannequin is "NPC".
	""")
@Example("set the mannequin description of {_mannequin} to \"Shop\"")
@Example("clear the mannequin description of last spawned mannequin")
@Example("reset the mannequin description of last spawned mannequin")
@RequiredPlugins("Minecraft 1.21.9+")
@Since("INSERT VERSION")
public class ExprMannequinDesc extends SimplePropertyExpression<Entity, String> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprMannequinDesc.class,
				String.class,
				"mannequin description",
				"entities",
				false
			).supplier(ExprMannequinDesc::new)
				.build()
		);
	}

	@Override
	public @Nullable String convert(Entity entity) {
		if (!(entity instanceof Mannequin mannequin))
			return null;
		Component desc = mannequin.getDescription();
		if (desc == null)
			return null;
		// TODO: Use Pickle's Adventure Component API
		return LegacyComponentSerializer.legacyAmpersand().serialize(desc);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(String.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Component component = null;
		if (mode == ChangeMode.SET) {
			assert delta != null;
			String string = (String) delta[0];
			component = Component.text(string);
		} else if (mode == ChangeMode.RESET) {
			component = Mannequin.defaultDescription();
		}

		for (Entity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Mannequin mannequin))
				continue;
			mannequin.setDescription(component);
		}
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "mannequin description";
	}

}
