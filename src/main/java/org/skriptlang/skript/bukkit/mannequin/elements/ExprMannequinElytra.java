package org.skriptlang.skript.bukkit.mannequin.elements;

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
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.datacomponent.item.ResolvableProfile.SkinPatch;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.mannequin.ResolvableProfileBuilder;
import org.skriptlang.skript.bukkit.mannequin.SkinPatchBuilder;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mannequin Elytra")
@Description("""
	The elytra texture displayed on a mannequin.
	The elytra key is represented as a namespaced key.
	A namespaced key can be formatted as 'namespace:id' or 'id'. \
	It can only contain one ':' to separate the namespace and the id. \
	Only alphanumeric characters, periods, underscores, and dashes can be used.
	""")
@Example("set the mannequin elytra of {_mannequin} to \"custom:elytra\"")
@Example("clear the mannequin elytra key of last spawned mannequin")
@RequiredPlugins("Minecraft 1.21.9+")
@Since("INSERT VERSION")
public class ExprMannequinElytra extends SimplePropertyExpression<Entity, String> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprMannequinElytra.class,
				String.class,
				"mannequin elytra [texture] [key]",
				"entities",
				false
			).supplier(ExprMannequinElytra::new)
				.build()
		);
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public @Nullable String convert(Entity entity) {
		if (!(entity instanceof Mannequin mannequin))
			return null;
		SkinPatch skinPatch = mannequin.getProfile().skinPatch();
		Key key = skinPatch.elytra();
		return key == null ? null : key.asString();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(String.class);
		return null;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Key key = null;
		if (delta != null) {
			String string = (String) delta[0];
			ValidationResult<NamespacedKey> result = NamespacedUtils.checkValidation(string);
			String message = result.message();
			if (!result.valid()) {
				error(message + ". " + NamespacedUtils.NAMEDSPACED_FORMAT_MESSAGE);
				return;
			} else if (message != null) {
				warning(message);
			}
			key = result.data();
		}

		for (Entity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Mannequin mannequin))
				continue;
			ResolvableProfile profile = mannequin.getProfile();
			SkinPatch skinPatch = profile.skinPatch();
			if (skinPatch.elytra() == key)
				continue;
			SkinPatch newPatch = new SkinPatchBuilder(skinPatch)
				.elytra(key)
				.build();
			ResolvableProfile newProfile = new ResolvableProfileBuilder(profile)
				.skinPatch(newPatch)
				.build();
			newProfile.resolve();
			mannequin.setProfile(newProfile);
		}
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "mannequin elytra";
	}

}
