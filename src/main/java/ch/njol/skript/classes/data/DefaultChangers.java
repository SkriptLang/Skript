package ch.njol.skript.classes.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.util.Experience;
import ch.njol.util.coll.CollectionUtils;

public class DefaultChangers {

	public DefaultChangers() {}

	public final static Changer<Entity> entityChanger = new Changer<Entity>() {

		@Override
		public Class<? extends Object> @Nullable [] acceptChange(ChangeMode mode) {
			switch (mode) {
				case ADD:
					return CollectionUtils.array(ItemType[].class, Inventory.class, Experience[].class);
				case DELETE:
					return CollectionUtils.array();
				case REMOVE:
					return CollectionUtils.array(PotionEffectType[].class, ItemType[].class, Inventory.class);
				case REMOVE_ALL:
					return CollectionUtils.array(PotionEffectType[].class, ItemType[].class);
				case SET:
				case RESET: // REMIND reset entity? (unshear, remove held item, reset weapon/armour, ...)
					return null;
				case INTERNAL:
					return CollectionUtils.array(Location.class);
				default:
					return null;
			}
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
				for (Object object : delta) {
					if (object instanceof Location location) {
						assert mode == ChangeMode.INTERNAL;
						entity.teleport(location);
					} else if (object instanceof PotionEffectType) {
						assert mode == ChangeMode.REMOVE || mode == ChangeMode.REMOVE_ALL;
						if (entity instanceof LivingEntity livingEntity)
							livingEntity.removePotionEffect((PotionEffectType) object);
					} else {
						if (entity instanceof Player player) {
							if (object instanceof Experience experience) {
								player.giveExp(experience.getXP());
							} else if (object instanceof Inventory inventory) {
								PlayerInventory playerInventory = player.getInventory();
								for (ItemStack itemStack : inventory) {
									if (itemStack == null)
										continue;
									if (mode == ChangeMode.ADD) {
										playerInventory.addItem(itemStack);
									} else {
										playerInventory.remove(itemStack);
									}
								}
							} else if (object instanceof ItemType itemType) {
								hasItem = true;
								final PlayerInventory invi = player.getInventory();
								if (mode == ChangeMode.ADD)
									itemType.addTo(invi);
								else if (mode == ChangeMode.REMOVE)
									itemType.removeFrom(invi);
								else
									itemType.removeAll(invi);
							}
						}
					}
				}
				if (entity instanceof Player player && hasItem)
					PlayerUtils.updateInventory(player);
			}
		}
	};
	
	public final static Changer<Player> playerChanger = new Changer<Player>() {
		@Override
		@Nullable
		public Class<? extends Object>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.DELETE)
				return null;
			return entityChanger.acceptChange(mode);
		}
		
		@Override
		public void change(final Player[] players, final @Nullable Object[] delta, final ChangeMode mode) {
			entityChanger.change(players, delta, mode);
		}
	};
	
	public final static Changer<Entity> nonLivingEntityChanger = new Changer<Entity>() {
		@Override
		@Nullable
		public Class<Object>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.DELETE)
				return CollectionUtils.array();
			return null;
		}
		
		@Override
		public void change(final Entity[] entities, final @Nullable Object[] delta, final ChangeMode mode) {
			assert mode == ChangeMode.DELETE;
			for (final Entity e : entities) {
				if (e instanceof Player)
					continue;
				e.remove();
			}
		}
	};
	
	public final static Changer<Item> itemChanger = new Changer<Item>() {
		@Override
		@Nullable
		public Class<?>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.SET)
				return CollectionUtils.array(ItemStack.class);
			return nonLivingEntityChanger.acceptChange(mode);
		}
		
		@Override
		public void change(final Item[] what, final @Nullable Object[] delta, final ChangeMode mode) {
			if (mode == ChangeMode.SET) {
				assert delta != null;
				for (final Item i : what)
					i.setItemStack((ItemStack) delta[0]);
			} else {
				nonLivingEntityChanger.change(what, delta, mode);
			}
		}
	};
	
	public final static Changer<Inventory> inventoryChanger = new Changer<Inventory>() {
		
		private Material[] cachedMaterials = Material.values();
		
		@Override
		@Nullable
		public Class<? extends Object>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.RESET)
				return null;
			if (mode == ChangeMode.REMOVE_ALL)
				return CollectionUtils.array(ItemType[].class);
			if (mode == ChangeMode.SET)
				return CollectionUtils.array(ItemType[].class, Inventory.class);
			return CollectionUtils.array(ItemType[].class, Inventory[].class);
		}
		
		@Override
		public void change(final Inventory[] invis, final @Nullable Object[] delta, final ChangeMode mode) {
			for (final Inventory invi : invis) {
				assert invi != null;
				switch (mode) {
					case DELETE:
						invi.clear();
						break;
					case SET:
						invi.clear();
						//$FALL-THROUGH$
					case ADD:
						assert delta != null;
						
						if(delta instanceof ItemStack[]) { // Old behavior - legacy code (is it used? no idea)
							ItemStack[] items = (ItemStack[]) delta;
							if(items.length > 36) {
								return;
							}
							for (final Object d : delta) {
								if (d instanceof Inventory) {
									for (final ItemStack i : (Inventory) d) {
										if (i != null)
											invi.addItem(i);
									}
								} else {
									((ItemType) d).addTo(invi);
								}
							}
						} else {
							for (final Object d : delta) {
								if (d instanceof ItemStack) {
									new ItemType((ItemStack) d).addTo(invi); // Can't imagine why would be ItemStack, but just in case...
								} else if (d instanceof ItemType) {
									((ItemType) d).addTo(invi);
								} else if (d instanceof Block) {
									new ItemType((Block) d).addTo(invi);
								} else {
									Skript.error("Can't " + d.toString() + " to an inventory!");
								}
							}
						}
						
						break;
					case REMOVE:
					case REMOVE_ALL:
						assert delta != null;
						if (delta.length == cachedMaterials.length) {
							// Potential fast path: remove all items -> clear inventory
							boolean equal = true;
							for (int i = 0; i < delta.length; i++) {
								if (!(delta[i] instanceof ItemType)) {
									equal = false;
									break; // Not an item, take slow path
								}
								if (((ItemType) delta[i]).getMaterial() != cachedMaterials[i]) {
									equal = false;
									break;
								}
							}
							if (equal) { // Take fast path, break out before slow one
								invi.clear();
								break;
							}
						}
						
						// Slow path
						for (final Object d : delta) {
							if (d instanceof Inventory) {
								assert mode == ChangeMode.REMOVE;
								for (ItemStack itemStack : (Inventory) d) {
									if (itemStack != null)
										invi.removeItem(itemStack);
								}
							} else {
								if (mode == ChangeMode.REMOVE)
									((ItemType) d).removeFrom(invi);
								else
									((ItemType) d).removeAll(invi);
							}
						}
						break;
					case RESET:
						assert false;
				}
				InventoryHolder holder = invi.getHolder();
				if (holder instanceof Player) {
					((Player) holder).updateInventory();
				}
			}
		}
	};
	
	public final static Changer<Block> blockChanger = new Changer<Block>() {
		@Override
		@Nullable
		public Class<?>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.RESET)
				return null; // REMIND regenerate?
			if (mode == ChangeMode.SET)
				return CollectionUtils.array(ItemType.class, BlockData.class);
			return CollectionUtils.array(ItemType[].class, Inventory[].class);
		}
		
		@Override
		public void change(final Block[] blocks, final @Nullable Object[] delta, final ChangeMode mode) {
			for (Block block : blocks) {
				assert block != null;
				switch (mode) {
					case SET:
						assert delta != null;
						Object object = delta[0];
						if (object instanceof ItemType) {
							((ItemType) object).getBlock().setBlock(block, true);
						} else if (object instanceof BlockData) {
							block.setBlockData(((BlockData) object));
						}
						break;
					case DELETE:
						block.setType(Material.AIR, true);
						break;
					case ADD:
					case REMOVE:
					case REMOVE_ALL:
						assert delta != null;
						BlockState state = block.getState();
						if (!(state instanceof InventoryHolder))
							break;
						Inventory invi = ((InventoryHolder) state).getInventory();
						if (mode == ChangeMode.ADD) {
							for (Object obj : delta) {
								if (obj instanceof Inventory) {
									for (ItemStack i : (Inventory) obj) {
										if (i != null)
											invi.addItem(i);
									}
								} else {
									((ItemType) obj).addTo(invi);
								}
							}
						} else {
							for (Object obj : delta) {
								if (obj instanceof Inventory) {
									invi.removeItem(((Inventory) obj).getContents());
								} else {
									if (mode == ChangeMode.REMOVE)
										((ItemType) obj).removeFrom(invi);
									else
										((ItemType) obj).removeAll(invi);
								}
							}
						}
						state.update();
						break;
					case RESET:
						assert false;
				}
			}
		}
	};
	
}
