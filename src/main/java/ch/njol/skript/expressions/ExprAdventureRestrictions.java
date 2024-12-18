/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.BukkitUnsafe;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Name("Adventure Restrictions Of Item")
@Description("Get or modify the adventure restrictions of an item.")
@Examples({
	"add dirt to destroyable restrictions of player's tool",
	"add (stone, diamond ore) to breakable blocks of {_item}",
	"clear break restrictions of {_item}",
	"remove sand from destroyable blocks of {_item} in adventure mode"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class ExprAdventureRestrictions extends PropertyExpression<ItemType, ItemType> {

	@SuppressWarnings("NotNullFieldNotInitialized")
    private static Method DESTROY_HAS, PLACE_HAS, DESTROY_GET, PLACE_GET, DESTROY_SET, PLACE_SET;

	static {
		if (Skript.methodExists(ItemMeta.class, "getDestroyableKeys")) {
			Skript.registerExpression(ExprAdventureRestrictions.class, ItemType.class, ExpressionType.PROPERTY,
				"[the] (:break|:destroy|:place|:build)able blocks of %itemtypes% [in adventure [mode]]",
				"[the] (:break|:destroy|:place|:build)[able] restrictions of %itemtypes%",
				"%itemtypes%'[s] (:break|:destroy|:place|:build)able blocks [in adventure [mode]]",
				"%itemtypes%'[s] (:break|:destroy|:place|:build)[able] restrictions");

			try {
                Class<?> META_CLASS = Class.forName("org.bukkit.inventory.meta.ItemMeta");

				DESTROY_HAS = META_CLASS.getDeclaredMethod("hasDestroyableKeys");
				PLACE_HAS = META_CLASS.getDeclaredMethod("hasPlaceableKeys");
				DESTROY_GET = META_CLASS.getDeclaredMethod("getDestroyableKeys");
				PLACE_GET = META_CLASS.getDeclaredMethod("getPlaceableKeys");
				DESTROY_SET = META_CLASS.getDeclaredMethod("setDestroyableKeys", Collection.class);
				PLACE_SET = META_CLASS.getDeclaredMethod("setPlaceableKeys", Collection.class);
			} catch (ClassNotFoundException | NoSuchMethodException e) {
				assert false: e.getMessage();
			}
		}
	}

	private boolean destroy;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ItemType>) exprs[0]);
		this.destroy = parseResult.hasTag("break") || parseResult.hasTag("destroy");
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ItemType @NotNull [] get(Event event, ItemType [] source) {
		Set<ItemType> existingKeys = new HashSet<>();
		for (ItemType item : source) {
			ItemMeta meta = item.getItemMeta();
			try {
				if (destroy ? (boolean) DESTROY_HAS.invoke(meta) : (boolean) PLACE_HAS.invoke(meta)) {
					for (Object key : (Set<Object>) (destroy ? DESTROY_GET.invoke(meta) : PLACE_GET.invoke(meta))) {
						Material material = BukkitUnsafe.getMaterialFromMinecraftId(key.toString());
						if (material != null)
							existingKeys.add(new ItemType(material));
					}
				}
			} catch (IllegalAccessException | InvocationTargetException e) {
				assert false: e.getMessage();
			}
		}
		return existingKeys.toArray(new ItemType[0]);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
			return CollectionUtils.array(ItemType[].class);
		} else if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
			return CollectionUtils.array();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		ItemType[] source = getExpr().getArray(event);

		Set<Object> deltaKeys = new HashSet<>();
		if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
			for (Object o : delta) {
				if (o instanceof ItemType) {
					for (ItemStack stack : ((ItemType) o).getAll())
						deltaKeys.add(stack.getType().getKey());
				}
			}
		}

		try {
			for (ItemType item : source) {

				ItemMeta meta = item.getItemMeta();
				Collection<Object> newKeys = new ArrayList<>();

				switch (mode) {
					case RESET:
					case DELETE:
						break;
					case SET:
						newKeys = deltaKeys;
						break;
					case ADD:
					case REMOVE:
						newKeys = new HashSet<>((Set<Object>) (destroy ? DESTROY_GET.invoke(meta) : PLACE_GET.invoke(meta)));
						if (mode == ChangeMode.ADD) {
							newKeys.addAll(deltaKeys);
						} else {
							newKeys.removeAll(deltaKeys);
						}
						break;
					case REMOVE_ALL:
						assert false;
				}

				if (destroy) {
					DESTROY_SET.invoke(meta, newKeys);
				} else {
					PLACE_SET.invoke(meta, newKeys);
				}

				item.setItemMeta(meta);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			assert false: e.getMessage();
		}

	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public @NotNull Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		if (destroy) {
			return "destroyable restrictions of " + getExpr().toString(event, debug);
		} else {
			return "placeable restrictions of " + getExpr().toString(event, debug);
		}
	}

}
