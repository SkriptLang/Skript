package org.skriptlang.skript.bukkit.mannequin;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.datacomponent.item.ResolvableProfile.SkinPatch;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Helper class for taking a finalized {@link ResolvableProfile} and turning it into a builder.
 */
@SuppressWarnings("UnstableApiUsage")
public class ResolvableProfileBuilder {

	private Collection<ProfileProperty> properties = new ArrayList<>();
	private @Nullable String name = null;
	private SkinPatch skinPatch = SkinPatch.empty();
	private @Nullable UUID uuid = null;

	public ResolvableProfileBuilder(ResolvableProfile profile) {
		properties = profile.properties();
		name = profile.name();
		skinPatch = profile.skinPatch();
		uuid = profile.uuid();
	}

	/**
	 * Add {@link ProfileProperty}s to be used in the final {@link ResolvableProfile}.
	 * @param properties The {@link ProfileProperty}s to add.
	 * @return {@code this}
	 */
	public ResolvableProfileBuilder addProperties(Collection<ProfileProperty> properties) {
		this.properties.addAll(properties);
		return this;
	}

	/**
	 * Add a {@link ProfileProperty} to be used in the final {@link ResolvableProfile}.
	 * @param property The {@link ProfileProperty} to add.
	 * @return {@code this}
	 */
	public ResolvableProfileBuilder addProperty(ProfileProperty property) {
		this.properties.add(property);
		return this;
	}

	/**
	 * Sets the {@link ProfileProperty}s to be used in the final {@link ResolvableProfile}.
	 * @param properties The {@link ProfileProperty}s to set.
	 * @return {@code this}
	 */
	public ResolvableProfileBuilder setProperties(Collection<ProfileProperty> properties) {
		this.properties = properties;
		return this;
	}

	/**
	 * The name to be used in the final {@link ResolvableProfile}.
	 * @param name The name.
	 * @return {@code this}
	 */
	public ResolvableProfileBuilder name(@Nullable String name) {
		this.name = name;
		return this;
	}

	/**
	 * The {@link SkinPatch} to be used in the final {@link ResolvableProfile}.
	 * @param skinPatch The {@link SkinPatch}.
	 * @return {@code this}
	 */
	public ResolvableProfileBuilder skinPatch(SkinPatch skinPatch) {
		this.skinPatch = skinPatch;
		return this;
	}

	/**
	 * The {@link UUID} to be used in the final {@link ResolvableProfile}.
	 * @param uuid The {@link UUID}.
	 * @return {@code this}
	 */
	public ResolvableProfileBuilder uuid(@Nullable UUID uuid) {
		this.uuid = uuid;
		return this;
	}

	/**
	 * @return The finalized {@link ResolvableProfile}.
	 */
	public ResolvableProfile build() {
		return ResolvableProfile.resolvableProfile()
			.addProperties(properties)
			.name(name)
			.skinPatch(skinPatch)
			.uuid(uuid)
			.build();
	}

}
