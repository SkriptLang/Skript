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

	public ResolvableProfileBuilder addProperties(Collection<ProfileProperty> properties) {
		this.properties.addAll(properties);
		return this;
	}

	public ResolvableProfileBuilder addProperty(ProfileProperty property) {
		this.properties.add(property);
		return this;
	}

	public ResolvableProfileBuilder setProperties(Collection<ProfileProperty> properties) {
		this.properties = properties;
		return this;
	}

	public ResolvableProfileBuilder name(@Nullable String name) {
		this.name = name;
		return this;
	}

	public ResolvableProfileBuilder skinPatch(SkinPatch skinPatch) {
		this.skinPatch = skinPatch;
		return this;
	}

	public ResolvableProfileBuilder uuid(@Nullable UUID uuid) {
		this.uuid = uuid;
		return this;
	}

	public ResolvableProfile build() {
		return ResolvableProfile.resolvableProfile()
			.addProperties(properties)
			.name(name)
			.skinPatch(skinPatch)
			.uuid(uuid)
			.build();
	}

}
