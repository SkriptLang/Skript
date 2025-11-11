package org.skriptlang.skript.bukkit.entity.mannequin.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.datacomponent.item.ResolvableProfile.Builder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.UUID;

@Name("Mannequin Skin")
@Description("""
	The skin of a mannequin.
	The skin can be set to an offlineplayer or a texture value of a skin.
	""")
@Example("set the mannequin skin of {_mannequin} to offlineplayer(\"Notch\")")
@Example("""
	set the mannequin skin of last spawned mannequin to "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW\
	5lY3JhZnQubmV0L3RleHR1cmUvYWJjNzNlN2VlNzA4ZTQ3NjU4YWY3YzMzMTZkY2VkNzk5NDUyNTg3NDg1NjM5ODA4ZDQ3OTVkMjNmYzY4NDU1MCJ9fX0="
	""")
@RequiredPlugins("Minecraft 1.21.9+")
@Since("INSERT VERSION")
public class ExprMannequinSkin extends SimplePropertyExpression<Entity, Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprMannequinSkin.class,
				Object.class,
				"mannequin skin",
				"entities",
				false
			).supplier(ExprMannequinSkin::new)
				.build()
		);
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public @Nullable Object convert(Entity entity) {
		if (!(entity instanceof Mannequin mannequin))
			return null;
		ResolvableProfile profile = mannequin.getProfile();
		UUID uuid = profile.uuid();
		if (uuid != null)
			return Bukkit.getOfflinePlayer(uuid);
		String name = profile.name();
		if (name != null)
			return Bukkit.getOfflinePlayer(name);
		for (ProfileProperty property : profile.properties()) {
			if (property.getName().equals("textures"))
				return property.getValue();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return CollectionUtils.array(OfflinePlayer.class, String.class);
		return null;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ResolvableProfile profile = Mannequin.defaultProfile();
		if (delta != null) {
			if (delta[0] instanceof OfflinePlayer player) {
				PlayerProfile playerProfile = player.getPlayerProfile();
				playerProfile.complete();
				profile = ResolvableProfile.resolvableProfile(playerProfile);
			} else if (delta[0] instanceof String string) {
				if (string.length() < 10 || string.contains(" "))
					return;
				Builder builder = ResolvableProfile.resolvableProfile();
				ProfileProperty property = new ProfileProperty("textures", string);
				builder.addProperty(property);
				profile = builder.build();
			}
		}

		for (Entity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Mannequin mannequin))
				continue;
			mannequin.setProfile(profile);
		}
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return CollectionUtils.array(OfflinePlayer.class, String.class);
	}

	@Override
	protected String getPropertyName() {
		return "mannequin skin";
	}

}
