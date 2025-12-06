package org.skriptlang.skript.bukkit.entity.mannequin;

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

	/**
	 * The body texture to be used in the final {@link SkinPatch}.
	 * @param body The body texture.
	 * @return {@code this}
	 */
	public SkinPatchBuilder body(@Nullable Key body) {
		this.body = body;
		return this;
	}

	/**
	 * The cape texture to be used in the final {@link SkinPatch}.
	 * @param cape The cape texture.
	 * @return {@code this}
	 */
	public SkinPatchBuilder cape(@Nullable Key cape) {
		this.cape = cape;
		return this;
	}

	/**
	 * The elytra texture to be used in the final {@link SkinPatch}.
	 * @param elytra The elytra texture.
	 * @return {@code this}
	 */
	public SkinPatchBuilder elytra(@Nullable Key elytra) {
		this.elytra = elytra;
		return this;
	}

	/**
	 * The {@link SkinModel} to be used in the final {@link SkinPatch}.
	 * @param model The {@link SkinModel}.
	 * @return {@code this}
	 */
	public SkinPatchBuilder model(@Nullable SkinModel model) {
		this.model = model;
		return this;
	}

	/**
	 * @return The finalized {@link SkinPatch}.
	 */
	public SkinPatch build() {
		return SkinPatch.skinPatch()
			.body(body)
			.cape(cape)
			.elytra(elytra)
			.model(model)
			.build();
	}

}
