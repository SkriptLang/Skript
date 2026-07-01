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
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.SkullMeta;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.jetbrains.annotations.Nullable;
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
			 infoBuilder(ExprSkullTexture.class, String.class, "(skull|head) texture", "itemtypes", false)
			.supplier(ExprSkullTexture::new)
			.build());
	}

	@Override
	public @Nullable String convert(ItemType item) {
		if (!(item.getItemMeta() instanceof SkullMeta meta))
			return null;
		PlayerProfile profile = meta.getPlayerProfile();
		if (profile == null)
			return null;
		return profile.getProperties().stream()
			.filter(property -> property.getName().equals("textures"))
			.findFirst()
			.map(ProfileProperty::getValue)
			.orElse(null);
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
		PlayerProfile profile = null;
		if (delta != null) {
			profile = Bukkit.createProfile(UUID.randomUUID());
			profile.setProperty(new ProfileProperty("textures", (String) delta[0]));
		}
		for (ItemType item : getExpr().getArray(event)) {
			if (item.getItemMeta() instanceof SkullMeta meta) {
				meta.setPlayerProfile(profile);
				item.setItemMeta(meta);
			}
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
