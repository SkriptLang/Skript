package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.Utils;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

import java.util.Locale;
import java.util.UUID;

@ApiStatus.Internal
public class EntityClassInfo extends ClassInfo<Entity> {

	public EntityClassInfo() {
		super(Entity.class, "entity");
		this.user("entit(y|ies)")
			.name("Entity")
			.description("An entity is something in a <a href='#world'>world</a> that's not a <a href='#block'>block</a>, " +
				"e.g. a <a href='#player'>player</a>, a skeleton, or a zombie, but also " +
				"<a href='#projectile'>projectiles</a> like arrows, fireballs or thrown potions, " +
				"or special entities like dropped items, falling blocks or paintings.")
			.usage("player, op, wolf, tamed ocelot, powered creeper, zombie, unsaddled pig, fireball, arrow, dropped item, item frame, etc.")
			.examples("entity is a zombie or creeper",
				"player is an op",
				"projectile is an arrow",
				"shoot a fireball from the player")
			.since("1.0")
			.defaultExpression(new EventValueExpression<>(Entity.class))
			.parser(new EntityParser())
			.changer(new EntityChanger())
			.property(Property.NAME,
				"The entity's name, if it has one, as text." +
					"Note that the regular name cannot be changed, meaning the entity's custom (display) name will be changed instead.",
				Skript.instance(),
				new EntityNameHandler(false))
			.property(Property.DISPLAY_NAME,
				"The entity's custom name, if it has one, as text. Can be set or reset.",
				Skript.instance(),
				new EntityNameHandler(true));
	}

	private static class EntityParser extends Parser<Entity> {
		//<editor-fold desc="entity parser" defaultstate="collapsed">
		@Override
		public @Nullable Entity parse(String s, ParseContext context) {
			if (Utils.isValidUUID(s))
				return Bukkit.getEntity(UUID.fromString(s));

			return null;
		}

		@Override
		public boolean canParse(ParseContext context) {
			return context == ParseContext.COMMAND || context == ParseContext.PARSE;
		}

		@Override
		public String toVariableNameString(Entity entity) {
			return "entity:" + entity.getUniqueId().toString().toLowerCase(Locale.ENGLISH);
		}

		@Override
		public String toString(Entity e, int flags) {
			return EntityData.toString(e, flags);
		}
		//</editor-fold>
	}

	public static class EntityChanger implements Changer<Entity> {
		//<editor-fold desc="entity changer" defaultstate="collapsed">
		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			return switch (mode) {
				case ADD -> CollectionUtils.array(ItemType[].class, Inventory.class, Experience[].class);
				case DELETE -> CollectionUtils.array();
				case REMOVE -> CollectionUtils.array(PotionEffectType[].class, ItemType[].class, Inventory.class);
				case REMOVE_ALL -> CollectionUtils.array(PotionEffectType[].class, ItemType[].class);
				case SET, RESET -> // REMIND reset entity? (unshear, remove held item, reset weapon/armour, ...)
					null;
			};
		}

		@Override
		public void change(Entity[] entities, Object @Nullable [] delta, ChangeMode mode) {
			if (delta == null) {
				for (Entity entity : entities) {
					if (!(entity instanceof Player))
						entity.remove();
				}
				return;
			}
			boolean hasItem = false;
			for (Entity entity : entities) {
				for (Object deltaObj : delta) {
					if (deltaObj instanceof PotionEffectType potionEffectType) {
						assert mode == ChangeMode.REMOVE || mode == ChangeMode.REMOVE_ALL;
						if (!(entity instanceof LivingEntity livingEntity))
							continue;
						livingEntity.removePotionEffect(potionEffectType);
					} else if (entity instanceof Player player) {
						if (deltaObj instanceof Experience experience) {
							player.giveExp(experience.getXP());
						} else if (deltaObj instanceof Inventory itemStacks) {
							PlayerInventory inventory = player.getInventory();
							for (ItemStack itemStack : itemStacks) {
								if (itemStack == null)
									continue;
								if (mode == ChangeMode.ADD) {
									inventory.addItem(itemStack);
								} else {
									inventory.remove(itemStack);
								}
							}
						} else if (deltaObj instanceof ItemType itemType) {
							hasItem = true;
							PlayerInventory invi = player.getInventory();
							if (mode == ChangeMode.ADD) {
								itemType.addTo(invi);
							} else if (mode == ChangeMode.REMOVE) {
								itemType.removeFrom(invi);
							} else {
								itemType.removeAll(invi);
							}
						}
					}
				}
				if (entity instanceof Player player && hasItem)
					PlayerUtils.updateInventory(player);
			}
		}
		//</editor-fold>
	}

	private static class EntityNameHandler implements ExpressionPropertyHandler<Entity, Component> {
		//<editor-fold desc="entity name handler" defaultstate="collapsed">
		private final boolean isDisplayName;

		public EntityNameHandler(boolean isDisplayName) {
			this.isDisplayName = isDisplayName;
		}

		@Override
		public Component convert(Entity entity) {
			if (isDisplayName) {
				return entity.customName();
			}
			return entity.name();
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
				return CollectionUtils.array(Component.class);
			return null;
		}

		@Override
		public void change(Entity entity, Object @Nullable [] delta, ChangeMode mode) {
			assert mode == ChangeMode.SET || mode == ChangeMode.RESET;
			Component name = delta == null ? null : (Component) delta[0];

			entity.customName(name);
			entity.setCustomNameVisible(name != null);
			if (entity instanceof LivingEntity living)
				living.setRemoveWhenFarAway(name == null);
		}

		@Override
		public @NotNull Class<Component> returnType() {
			return Component.class;
		}
		//</editor-fold>
	}

}
