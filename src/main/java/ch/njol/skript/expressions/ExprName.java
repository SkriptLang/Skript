package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.InventoryUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Nameable;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Name / Display Name / Tab List Name")
@Description({
	"Represents the Minecraft account, display or tab list name of a player, or the custom name of an item, entity, "
        + "block, inventory, gamerule, world, script or function.",
	"",
	"<strong>Players:</strong>",
	"\t<strong>Name:</strong> The Minecraft account name of the player. Can't be changed, but 'display name' can be changed.",
	"\t<strong>Display Name:</strong> The name of the player that is displayed in messages. " +
		"This name can be changed freely and can include color codes, and is shared among all plugins (e.g. chat plugins will use the display name).",
	"",
	"<strong>Entities:</strong>",
	"\t<strong>Name:</strong> The custom name of the entity. Can be changed. But for living entities, " +
		"the players will have to target the entity to see its name tag. For non-living entities, the name will not be visible at all. To prevent this, use 'display name'.",
	"\t<strong>Display Name:</strong> The custom name of the entity. Can be changed, " +
		"which will also enable <em>custom name visibility</em> of the entity so name tag of the entity will be visible always.",
	"",
	"<strong>Items:</strong>",
	"\t<strong>Name and Display Name:</strong> The <em>custom</em> name of the item (not the Minecraft locale name). Can be changed.",
	"",
	"<strong>Inventories:</strong>",
	"\t<strong>Name and Display Name:</strong> The name/title of the inventory. " +
		"Changing name of an inventory means opening the same inventory with the same contents but with a different name to its current viewers.",
	"",
	"<strong>Gamerules:</strong>",
	"\t<strong>Name:</strong> The name of the gamerule. Cannot be changed.",
	"",
	"<strong>Worlds:</strong>",
	"\t<strong>Name:</strong> The name of the world. Cannot be changed.",
	"",
	"<strong>Scripts:</strong>",
	"\t<strong>Name:</strong> The name of a script, excluding its file extension."
})
@Examples({
	"on join:",
	"\tplayer has permission \"name.red\"",
	"\tset the player's display name to \"&lt;red&gt;[admin] &lt;gold&gt;%name of player%\"",
	"\tset the player's tab list name to \"&lt;green&gt;%player's name%\"",
	"set the name of the player's tool to \"Legendary Sword of Awesomeness\""
})
@Since("before 2.1, 2.2-dev20 (inventory name), 2.4 (non-living entity support, changeable inventory name), 2.7 (worlds)")
public class ExprName extends SimplePropertyExpression<Object, String> {

	@Nullable
	private static BungeeComponentSerializer serializer;

	static {
		// Check for Adventure API
		if (Skript.classExists("net.kyori.adventure.text.Component") &&
			Skript.methodExists(Bukkit.class, "createInventory", InventoryHolder.class, int.class, Component.class))
			serializer = BungeeComponentSerializer.get();

		List<String> patterns = new ArrayList<>();
		patterns.addAll(Arrays.asList(getPatterns("name[s]", "offlineplayers/entities/inventories/nameds")));
		patterns.addAll(Arrays.asList(getPatterns("(display|nick|chat|custom)[ ]name[s]", "offlineplayers/entities/inventories/nameds")));
		patterns.addAll(Arrays.asList(getPatterns("(player|tab)[ ]list name[s]", "players")));

		Skript.registerExpression(ExprName.class, String.class, ExpressionType.COMBINED, patterns.toArray(new String[0]));
		// we keep the entity input because we want to do something special with entities
	}

	/*
	 * 1 = "name",
	 * 2 = "display name",
	 * 3 = "tablist name"
	 */
	private int mark;
	private boolean scriptResolvedName;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.mark = (matchedPattern / 2) + 1;
		this.setExpr(exprs[0]);
		this.scriptResolvedName = this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION);
		return true;
	}

	@Override
	public @Nullable String convert(Object object) {
		if (object instanceof OfflinePlayer offlinePlayer) {
			if (offlinePlayer.isOnline()) { // Defer to player check below
				object = offlinePlayer.getPlayer();
			} else { // We can only support "name"
				return mark == 1 ? offlinePlayer.getName() : null;
			}
		}

		if (!scriptResolvedName && object instanceof Script script) {
			return script.nameAndPath();
		}

		if (object instanceof Player player) {
			return switch (mark) {
				case 1 -> player.getName();
				case 2 -> player.getDisplayName();
				case 3 -> player.getPlayerListName();
				default -> throw new IllegalStateException("Unexpected value: " + mark);
			};
		} else if (object instanceof Nameable nameable) {
			if (mark == 1 && nameable instanceof CommandSender sender)
				return sender.getName();
			return nameable.getCustomName();
		} else if (object instanceof Inventory inventory) {
			if (inventory.getViewers().isEmpty())
				return null;
			return InventoryUtils.getTitle(inventory.getViewers().get(0).getOpenInventory());
		} else if (object instanceof AnyNamed named) {
			return named.name();
		}
		return null;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET) {
			if (mark == 1) {
				if (Player.class.isAssignableFrom(getExpr().getReturnType())) {
					Skript.error("Can't change the Minecraft name of a player. Change the 'display name' or 'tab list "
						+ "name' instead.");
					return null;
				} else if (World.class.isAssignableFrom(getExpr().getReturnType())) {
					return null;
				} else if (Script.class.isAssignableFrom(getExpr().getReturnType())) {
					return null;
				} else if (DynamicFunctionReference.class.isAssignableFrom(getExpr().getReturnType())) {
					return null;
				}
			}
			return CollectionUtils.array(String.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		String name = delta != null ? (String) delta[0] : null;
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Player player) {
				switch (mark) {
					case 2:
						player.setDisplayName(name != null ? name + ChatColor.RESET : ((Player) object).getName());
						break;
					case 3: // Null check not necessary. This method will use the player's name if 'name' is null.
						player.setPlayerListName(name);
						break;
				}
			} else if (object instanceof Entity entity) {
				entity.setCustomName(name);
				if (mark == 2 || mode == ChangeMode.RESET) // Using "display name"
					entity.setCustomNameVisible(name != null);
				if (object instanceof LivingEntity living)
					living.setRemoveWhenFarAway(name == null);
			} else if (object instanceof AnyNamed named) {
				if (named.supportsNameChange())
					named.setName(name);
			} else if (object instanceof Inventory inventory) {

				if (inventory.getViewers().isEmpty())
					return;
				// Create a clone to avoid a ConcurrentModificationException
				List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());

				InventoryType type = inventory.getType();
				if (!type.isCreatable())
					return;

				Inventory copy;
				if (serializer == null) {
					if (name == null)
						name = type.getDefaultTitle();
					if (type == InventoryType.CHEST) {
						copy = Bukkit.createInventory(inventory.getHolder(), inventory.getSize(), name);
					} else {
						copy = Bukkit.createInventory(inventory.getHolder(), type, name);
					}
				} else {
					Component component = type.defaultTitle();
					if (name != null) {
						BaseComponent[] components = BungeeConverter.convert(ChatMessages.parseToArray(name));
						component = serializer.deserialize(components);
					}
					if (type == InventoryType.CHEST) {
						copy = Bukkit.createInventory(inventory.getHolder(), inventory.getSize(), component);
					} else {
						copy = Bukkit.createInventory(inventory.getHolder(), type, component);
					}
				}
				copy.setContents(inventory.getContents());
				viewers.forEach(viewer -> viewer.openInventory(copy));
			}
		}
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return switch (mark) {
			case 2 -> "display name";
			case 3 -> "tablist name";
			default -> "name";
		};
	}

}
