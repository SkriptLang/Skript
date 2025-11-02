package org.skriptlang.skript.bukkit.mannequin.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.datacomponent.item.ResolvableProfile.SkinPatch;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.Event;
import org.bukkit.profile.PlayerTextures.SkinModel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.mannequin.ResolvableProfileBuilder;
import org.skriptlang.skript.bukkit.mannequin.SkinPatchBuilder;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mannequin Model")
@Description("The skin model of a mannequin.")
@Example("set the mannequin model to classic")
@Example("set the mannequin skin model to slim")
@RequiredPlugins("Minecraft 1.21.9+")
@Since("INSERT VERSION")
public class ExprMannequinModel extends SimplePropertyExpression<Entity, SkinModel> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprMannequinModel.class,
				SkinModel.class,
				"mannequin [skin] model",
				"entities",
				false
			).supplier(ExprMannequinModel::new)
				.build()
		);
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public @Nullable SkinModel convert(Entity entity) {
		if (!(entity instanceof Mannequin mannequin))
			return null;
		SkinPatch skinPatch = mannequin.getProfile().skinPatch();
		return skinPatch.model();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(SkinModel.class);
		return null;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		SkinModel model = delta == null ? null : (SkinModel) delta[0];

		for (Entity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Mannequin mannequin))
				continue;
			ResolvableProfile profile = mannequin.getProfile();
			SkinPatch skinPatch = profile.skinPatch();
			if (skinPatch.model() == model)
				continue;
			SkinPatch newPatch = new SkinPatchBuilder(skinPatch)
				.model(model)
				.build();
			ResolvableProfile newProfile = new ResolvableProfileBuilder(profile)
				.skinPatch(newPatch)
				.build();
			newProfile.resolve();
			mannequin.setProfile(newProfile);
		}
	}

	@Override
	public Class<? extends SkinModel> getReturnType() {
		return SkinModel.class;
	}

	@Override
	protected String getPropertyName() {
		return "mannequin model";
	}

}
