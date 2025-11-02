package org.skriptlang.skript.bukkit.mannequin;

import io.papermc.paper.datacomponent.item.ResolvableProfile.SkinPatch;
import net.kyori.adventure.key.Key;
import org.bukkit.profile.PlayerTextures.SkinModel;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for taking a finalized {@link SkinPatch} and turning it into a builder.
 */
@SuppressWarnings("UnstableApiUsage")
public class SkinPatchBuilder {

	private @Nullable Key body = null;
	private @Nullable Key cape = null;
	private @Nullable Key elytra = null;
	private @Nullable SkinModel model = null;

	public SkinPatchBuilder(SkinPatch skinPatch) {
		body = skinPatch.body();
		cape = skinPatch.cape();
		elytra = skinPatch.elytra();
		model = skinPatch.model();
	}

	public SkinPatchBuilder body(@Nullable Key body) {
		this.body = body;
		return this;
	}

	public SkinPatchBuilder cape(@Nullable Key cape) {
		this.cape = cape;
		return this;
	}

	public SkinPatchBuilder elytra(@Nullable Key elytra) {
		this.elytra = elytra;
		return this;
	}

	public SkinPatchBuilder model(@Nullable SkinModel model) {
		this.model = model;
		return this;
	}

	public SkinPatch build() {
		return SkinPatch.skinPatch()
			.body(body)
			.cape(cape)
			.elytra(elytra)
			.model(model)
			.build();
	}

}
