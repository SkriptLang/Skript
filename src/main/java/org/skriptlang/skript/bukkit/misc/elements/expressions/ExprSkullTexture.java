package org.skriptlang.skript.bukkit.misc.elements.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.SkullMeta;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.UUID;

@Name("Skull Texture")
@Description("""
	The skull texture of a player head. This allows you to give a skull a custom texture (e.g. instead of it being a Steve head, it's Notch's head).
	The texture input is a base64 string containing the texture data to use (https://minecraft-heads.com is one site that provides easy access to base64 texture strings).
	Resetting the texture of a skull will make it look like a Steve/Alex head.
	""")
@Example("set the skull texture of {_i} to \"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4NmRmZDc0Y2JhZmJkMWRiZTQ3OWY1ZTAzNzRjMDliZjJlYjRlMzg2NjExZmM0ZmM2OTlmMDJlY2E0ZGQyYyJ9fX0=\"")
@Since("INSERT VERSION")
public class ExprSkullTexture extends SimplePropertyExpression<ItemType, String> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			 infoBuilder(ExprSkullTexture.class, String.class, "skull texture", "itemtypes", false)
			.supplier(ExprSkullTexture::new)
			.priority(SyntaxInfo.SIMPLE)
			.addPattern("[the] (skull|head) texture [of] %itemtypes%")
			.build());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET -> CollectionUtils.array(String.class);
			case DELETE, RESET -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		String value = delta == null ? null : (String) delta[0];
		switch (mode) {
			case DELETE, RESET:
				for (ItemType item : getExpr().getArray(event)) {
					if (item.getMaterial() != Material.PLAYER_HEAD) {
						continue;
					}
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					meta.setPlayerProfile(null);
					item.setItemMeta(meta);
				}
				break;
			case SET:
				for (ItemType item : getExpr().getArray(event)) {
					if (item.getMaterial() != Material.PLAYER_HEAD) {
						continue;
					}
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
					playerProfile.setProperty(new ProfileProperty("textures", value));
					meta.setPlayerProfile(playerProfile);
					item.setItemMeta(meta);
				}
		}
	}

	@Override
	public @Nullable String convert(ItemType item) {
		if (item.getMaterial() != Material.PLAYER_HEAD) {
			return null;
		}
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		PlayerProfile profile = meta.getPlayerProfile();
		if (profile == null) {
			return null;
		}
		ProfileProperty texture = profile.getProperties().stream()
			.filter(property -> property.getName().equals("textures"))
			.findFirst()
			.orElse(null);
		if (!(texture == null)) {
			return texture.getValue();
		} else {
			return null;
		}
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "skull texture";
	}
}
