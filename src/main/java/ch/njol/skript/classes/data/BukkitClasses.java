package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.bukkitutil.SkriptTeleportFlag;
import ch.njol.skript.classes.*;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.expressions.ExprDamageCause;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.BlockUtils;
import ch.njol.yggdrasil.Fields;
import io.papermc.paper.world.MoonPhase;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityTransformEvent.TransformReason;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent.ChangeReason;
import org.bukkit.event.player.PlayerQuitEvent.QuitReason;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.metadata.Metadatable;
import org.bukkit.util.CachedServerIcon;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.base.types.*;
import org.skriptlang.skript.bukkit.base.types.EntityClassInfo.EntityChanger;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyHandler.ExpressionPropertyHandler;

import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitClasses {

	// TODO - remove unnecessary classExists checks when Spigot support is dropped

	public BukkitClasses() {}

	static {
		Classes.registerClass(new EntityClassInfo());

		Classes.registerClass(new ClassInfo<>(LivingEntity.class, "livingentity")
				.user("living ?entit(y|ies)")
				.name("Living Entity")
				.description("A living <a href='#entity'>entity</a>, i.e. a mob or <a href='#player'>player</a>, " +
						"not inanimate entities like <a href='#projectile'>projectiles</a> or dropped items.")
				.usage("see <a href='#entity'>entity</a>, but ignore inanimate objects")
				.examples("spawn 5 powered creepers",
						"shoot a zombie from the creeper")
				.since("1.0")
				.defaultExpression(new EventValueExpression<>(LivingEntity.class))
				.changer(new EntityChanger()));

		Classes.registerClass(new ClassInfo<>(Projectile.class, "projectile")
				.user("projectiles?")
				.name("Projectile")
				.description("A projectile, e.g. an arrow, snowball or thrown potion.")
				.usage("arrow, fireball, snowball, thrown potion, etc.")
				.examples("projectile is a snowball",
						"shoot an arrow at speed 5 from the player")
				.since("1.0")
				.defaultExpression(new EventValueExpression<>(Projectile.class))
				.changer(DefaultChangers.nonLivingEntityChanger));

		Classes.registerClass(new BlockClassInfo());

		Classes.registerClass(new ClassInfo<>(BlockData.class, "blockdata")
				.user("block ?datas?")
				.name("Block Data")
				.description("Block data is the detailed information about a block, referred to in Minecraft as BlockStates, " +
						"allowing for the manipulation of different aspects of the block, including shape, waterlogging, direction the block is facing, " +
						"and so much more. Information regarding each block's optional data can be found on Minecraft's Wiki. Find the block you're " +
						"looking for and scroll down to 'Block States'. Different states must be separated by a semicolon (see examples). " +
						"The 'minecraft:' namespace is optional, as well as are underscores.")
				.examples("set block at player to campfire[lit=false]",
						"set target block of player to oak stairs[facing=north;waterlogged=true]",
						"set block at player to grass_block[snowy=true]",
						"set loop-block to minecraft:chest[facing=north]",
						"set block above player to oak_log[axis=y]",
						"set target block of player to minecraft:oak_leaves[distance=2;persistent=false]")
				.after("itemtype")
				.since("2.5")
				.parser(new Parser<BlockData>() {
					@Nullable
					@Override
					public BlockData parse(String input, ParseContext context) {
						return BlockUtils.createBlockData(input);
					}

					@Override
					public String toString(BlockData o, int flags) {
						return o.getAsString().replace(",", ";");
					}

					@Override
					public String toVariableNameString(BlockData o) {
						return "blockdata:" + o.getAsString();
					}
				})
				.serializer(new Serializer<BlockData>() {
					@Override
					public Fields serialize(BlockData o) {
						Fields f = new Fields();
						f.putObject("blockdata", o.getAsString());
						return f;
					}

					@Override
					public void deserialize(BlockData o, Fields f) {
						assert false;
					}

					@Override
					protected BlockData deserialize(Fields f) throws StreamCorruptedException {
						String data = f.getObject("blockdata", String.class);
						assert data != null;
						try {
							return Bukkit.createBlockData(data);
						} catch (IllegalArgumentException ex) {
							throw new StreamCorruptedException("Invalid block data: " + data);
						}
					}

					@Override
					public boolean mustSyncDeserialization() {
						return true;
					}

					@Override
					protected boolean canBeInstantiated() {
						return false;
					}
				}).cloner(BlockData::clone));

		Classes.registerClass(new ClassInfo<>(Location.class, "location")
				.user("locations?")
				.name("Location")
				.description("A location in a <a href='#world'>world</a>. Locations are world-specific and even store a <a href='#direction'>direction</a>, " +
						"e.g. if you save a location and later teleport to it you will face the exact same direction you did when you saved the location.")
				.usage("")
				.examples("teleport player to location at 0, 69, 0",
						  "set {home::%uuid of player%} to location of the player")
				.since("1.0")
				.defaultExpression(new EventValueExpression<>(Location.class))
				.parser(new Parser<Location>() {
					@Override
					@Nullable
					public Location parse(final String s, final ParseContext context) {
						return null;
					}

					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}

					@Override
					public String toString(final Location l, final int flags) {
						String worldPart = l.getWorld() == null ? "" : " in '" + l.getWorld().getName() + "'"; // Safety: getWorld is marked as Nullable by spigot
						return "x: " + Skript.toString(l.getX()) + ", y: " + Skript.toString(l.getY()) + ", z: " + Skript.toString(l.getZ()) + ", yaw: " + Skript.toString(l.getYaw()) + ", pitch: " + Skript.toString(l.getPitch()) + worldPart;
					}

					@Override
					public String toVariableNameString(final Location l) {
						return l.getWorld().getName() + ":" + l.getX() + "," + l.getY() + "," + l.getZ();
					}

					@Override
					public String getDebugMessage(final Location l) {
						return "(" + l.getWorld().getName() + ":" + l.getX() + "," + l.getY() + "," + l.getZ() + "|yaw=" + l.getYaw() + "/pitch=" + l.getPitch() + ")";
					}
				}).serializer(new Serializer<Location>() {
					@Override
					public Fields serialize(Location location) {
						Fields fields = new Fields();
						World world = null;
						try {
							world = location.getWorld();
						} catch (IllegalArgumentException exception) {
							Skript.warning("A location failed to serialize with its defined world, as the world was unloaded.");
						}
						fields.putObject("world", world);
						fields.putPrimitive("x", location.getX());
						fields.putPrimitive("y", location.getY());
						fields.putPrimitive("z", location.getZ());
						fields.putPrimitive("yaw", location.getYaw());
						fields.putPrimitive("pitch", location.getPitch());
						return fields;
					}

					@Override
					public void deserialize(final Location o, final Fields f) {
						assert false;
					}

					@Override
					public Location deserialize(final Fields f) throws StreamCorruptedException {
						return new Location(f.getObject("world", World.class),
								f.getPrimitive("x", double.class), f.getPrimitive("y", double.class), f.getPrimitive("z", double.class),
								f.getPrimitive("yaw", float.class), f.getPrimitive("pitch", float.class));
					}

					@Override
					public boolean canBeInstantiated() {
						return false; // no nullary constructor - also, saving the location manually prevents errors should Location ever be changed
					}

					@Override
					public boolean mustSyncDeserialization() {
						return true;
					}

					// return l.getWorld().getName() + ":" + l.getX() + "," + l.getY() + "," + l.getZ() + "|" + l.getYaw() + "/" + l.getPitch();
					@Override
					@Nullable
					public Location deserialize(final String s) {
						final String[] split = s.split("[:,|/]");
						if (split.length != 6)
							return null;
						final World w = Bukkit.getWorld(split[0]);
						if (w == null)
							return null;
						try {
							final double[] l = new double[5];
							for (int i = 0; i < 5; i++)
								l[i] = Double.parseDouble(split[i + 1]);
							return new Location(w, l[0], l[1], l[2], (float) l[3], (float) l[4]);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				})
				.cloner(Location::clone));

		Classes.registerClass(new ClassInfo<>(Vector.class, "vector")
				.user("vectors?")
				.name("Vector")
				.description("Vector is a collection of numbers. In Minecraft, 3D vectors are used to express velocities of entities.")
				.usage("vector(x, y, z)")
				.examples("")
				.since("2.2-dev23")
				.defaultExpression(new EventValueExpression<>(Vector.class))
				.parser(new Parser<Vector>() {
					@Override
					@Nullable
					public Vector parse(final String s, final ParseContext context) {
						return null;
					}

					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}

					@Override
					public String toString(final Vector vec, final int flags) {
						return "x: " + Skript.toString(vec.getX()) + ", y: " + Skript.toString(vec.getY()) + ", z: " + Skript.toString(vec.getZ());
					}

					@Override
					public String toVariableNameString(final Vector vec) {
						return "vector:" + vec.getX() + "," + vec.getY() + "," + vec.getZ();
					}

					@Override
					public String getDebugMessage(final Vector vec) {
						return "(" + vec.getX() + "," + vec.getY() + "," + vec.getZ() + ")";
					}
				})
				.serializer(new Serializer<Vector>() {
					@Override
					public Fields serialize(Vector o) {
						Fields f = new Fields();
						f.putPrimitive("x", o.getX());
						f.putPrimitive("y", o.getY());
						f.putPrimitive("z", o.getZ());
						return f;
					}

					@Override
					public void deserialize(Vector o, Fields f) {
						assert false;
					}

					@Override
					public Vector deserialize(final Fields f) throws StreamCorruptedException {
						return new Vector(f.getPrimitive("x", double.class), f.getPrimitive("y", double.class), f.getPrimitive("z", double.class));
					}

					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}

					@Override
					protected boolean canBeInstantiated() {
						return false;
					}
				})
				.cloner(Vector::clone));

		Classes.registerClass(new ClassInfo<>(World.class, "world")
				.user("worlds?")
				.name("World")
				.description("One of the server's worlds. Worlds can be put into scripts by surrounding their name with double quotes, e.g. \"world_nether\", " +
						"but this might not work reliably as <a href='#string'>text</a> uses the same syntax.")
				.usage("<code>\"world_name\"</code>, e.g. \"world\"")
				.examples("broadcast \"Hello!\" to the world \"world_nether\"")
				.since("1.0, 2.2 (alternate syntax)")
				.after("string")
				.defaultExpression(new EventValueExpression<>(World.class))
				.parser(new Parser<World>() {
					@SuppressWarnings("null")
					private final Pattern parsePattern = Pattern.compile("(?:(?:the )?world )?\"(.+)\"", Pattern.CASE_INSENSITIVE);

					@Override
					@Nullable
					public World parse(final String s, final ParseContext context) {
						// REMIND allow shortcuts '[over]world', 'nether' and '[the_]end' (server.properties: 'level-name=world') // inconsistent with 'world is "..."'
						if (context == ParseContext.COMMAND || context == ParseContext.PARSE || context == ParseContext.CONFIG)
							return Bukkit.getWorld(s);
						final Matcher m = parsePattern.matcher(s);
						if (m.matches())
							return Bukkit.getWorld(m.group(1));
						return null;
					}

					@Override
					public String toString(final World w, final int flags) {
						return "" + w.getName();
					}

					@Override
					public String toVariableNameString(final World w) {
						return "" + w.getName();
					}
				}).serializer(new Serializer<World>() {
					@Override
					public Fields serialize(final World w) {
						final Fields f = new Fields();
						f.putObject("name", w.getName());
						return f;
					}

					@Override
					public void deserialize(final World o, final Fields f) {
						assert false;
					}

					@Override
					public boolean canBeInstantiated() {
						return false;
					}

					@Override
					protected World deserialize(final Fields fields) throws StreamCorruptedException {
						final String name = fields.getObject("name", String.class);
						assert name != null;
						final World w = Bukkit.getWorld(name);
						if (w == null)
							throw new StreamCorruptedException("Missing world " + name);
						return w;
					}

					// return w.getName();
					@Override
					@Nullable
					public World deserialize(final String s) {
						return Bukkit.getWorld(s);
					}

					@Override
					public boolean mustSyncDeserialization() {
						return true;
					}
				})
				.property(Property.NAME,
					"A world's name, as text. Cannot be changed.",
					Skript.instance(),
					ExpressionPropertyHandler.of(World::getName, String.class))
		);

		Classes.registerClass(new InventoryClassInfo());

		Classes.registerClass(new EnumClassInfo<>(InventoryAction.class, "inventoryaction", "inventory actions")
				.user("inventory ?actions?")
				.name("Inventory Action")
				.description("What player just did in inventory event. Note that when in creative game mode, most actions do not work correctly.")
				.examples("")
				.since("2.2-dev16"));

		Classes.registerClass(new EnumClassInfo<>(ClickType.class, "clicktype", "click types")
				.user("click ?types?")
				.name("Click Type")
				.description("Click type, mostly for inventory events. Tells exactly which keys/buttons player pressed, " +
						"assuming that default keybindings are used in client side.")
				.examples("")
				.since("2.2-dev16b, 2.2-dev35 (renamed to click type)"));

		Classes.registerClass(new EnumClassInfo<>(InventoryType.class, "inventorytype", "inventory types")
				.user("inventory ?types?")
				.name("Inventory Type")
				.description("Minecraft has several different inventory types with their own use cases.")
				.examples("")
				.since("2.2-dev32"));

		Classes.registerClass(new PlayerClassInfo());

		Classes.registerClass(new OfflinePlayerClassInfo());

		Classes.registerClass(new ClassInfo<>(CommandSender.class, "commandsender")
				.user("((commands?)? ?)?(sender|executor)s?")
				.name("Command Sender")
				.description("A player or the console.")
				.usage("use <a href='#LitConsole'>the console</a> for the console",
						"see <a href='#player'>player</a> for players.")
				.examples("command /push [<player>]:",
						"\ttrigger:",
						"\t\tif arg-1 is not set:",
						"\t\t\tif command sender is console:",
						"\t\t\t\tsend \"You can't push yourself as a console :\\\" to sender",
						"\t\t\t\tstop",
						"\t\t\tpush sender upwards with force 2",
						"\t\t\tsend \"Yay!\"",
						"\t\telse:",
						"\t\t\tpush arg-1 upwards with force 2",
						"\t\t\tsend \"Yay!\" to sender and arg-1")
				.since("1.0")
				.defaultExpression(new EventValueExpression<>(CommandSender.class))
				.parser(new Parser<>() {
					@Override
					@Nullable
					public CommandSender parse(final String s, final ParseContext context) {
						return null;
					}

					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}

					@Override
					public String toString(final CommandSender s, final int flags) {
						return s.getName();
					}

					@Override
					public String toVariableNameString(final CommandSender s) {
						return s.getName();
					}
				})
				.property(Property.NAME,
					"A command sender's name, as text. Cannot be changed.",
					Skript.instance(),
					ExpressionPropertyHandler.of(CommandSender::getName, String.class)));

		Classes.registerClass(new NameableClassInfo());

		Classes.registerClass(new ClassInfo<>(InventoryHolder.class, "inventoryholder")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new EventValueExpression<>(InventoryHolder.class))
				.after("entity", "block")
				.parser(new Parser<InventoryHolder>() {
					@Override
					public boolean canParse(ParseContext context) {
						return false;
					}

					@Override
					public String toString(InventoryHolder holder, int flags) {
						if (holder instanceof BlockState) {
							return Classes.toString(((BlockState) holder).getBlock());
						} else if (holder instanceof DoubleChest) {
							return Classes.toString(holder.getInventory().getLocation().getBlock());
						} else if (holder instanceof BlockInventoryHolder) {
							return Classes.toString(((BlockInventoryHolder) holder).getBlock());
						} else if (Classes.getSuperClassInfo(holder.getClass()).getC() == InventoryHolder.class) {
							return holder.getClass().getSimpleName(); // an inventory holder and only that
						} else {
							return Classes.toString(holder);
						}
					}

					@Override
					public String toVariableNameString(InventoryHolder holder) {
						return toString(holder, 0);
					}
				}));
		Classes.registerClass(new EnumClassInfo<>(GameMode.class, "gamemode", "game modes", new SimpleLiteral<>(GameMode.SURVIVAL, true))
				.user("game ?modes?")
				.name("Game Mode")
				.description("The game modes survival, creative, adventure and spectator.")
				.examples("player's gamemode is survival",
						"set the player argument's game mode to creative")
				.since("1.0"));

		Classes.registerClass(new ItemStackClassInfo());

		Classes.registerClass(new ClassInfo<>(Item.class, "itementity")
				.name(ClassInfo.NO_DOC)
				.since("2.0")
				.changer(DefaultChangers.itemChanger));

		Classes.registerClass(new RegistryClassInfo<>(Biome.class, Registry.BIOME, "biome", "biomes")
				.user("biomes?")
				.name("Biome")
				.description("All possible biomes Minecraft uses to generate a world.",
					"NOTE: Minecraft namespaces are supported, ex: 'minecraft:basalt_deltas'.")
				.examples("biome at the player is desert")
				.since("1.4.4")
				.after("damagecause"));

		// REMIND make my own damage cause class (that e.g. stores the attacker entity, the projectile, or the attacking block)
		Classes.registerClass(new EnumClassInfo<>(DamageCause.class, "damagecause", "damage causes", new ExprDamageCause())
				.user("damage ?causes?")
				.name("Damage Cause")
				.description("The cause/type of a <a href='#damage'>damage event</a>, e.g. lava, fall, fire, drowning, explosion, poison, etc.",
						"Please note that support for this type is very rudimentary, e.g. lava, fire and burning, " +
								"as well as projectile and attack are considered different types.")
				.examples("")
				.since("2.0")
				.after("itemtype", "itemstack", "entitydata", "entitytype"));

		Classes.registerClass(new ClassInfo<>(Chunk.class, "chunk")
				.user("chunks?")
				.name("Chunk")
				.description("A chunk is a cuboid of 16×16×128 (x×z×y) blocks. Chunks are spread on a fixed rectangular grid in their world.")
				.usage("")
				.examples("")
				.since("2.0")
				.parser(new Parser<Chunk>() {
					@Override
					@Nullable
					public Chunk parse(final String s, final ParseContext context) {
						return null;
					}

					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}

					@Override
					public String toString(final Chunk c, final int flags) {
						return "chunk (" + c.getX() + "," + c.getZ() + ") of " + c.getWorld().getName();
					}

					@Override
					public String toVariableNameString(final Chunk c) {
						return c.getWorld().getName() + ":" + c.getX() + "," + c.getZ();
					}
				})
				.serializer(new Serializer<Chunk>() {
					@Override
					public Fields serialize(final Chunk c) {
						final Fields f = new Fields();
						f.putObject("world", c.getWorld());
						f.putPrimitive("x", c.getX());
						f.putPrimitive("z", c.getZ());
						return f;
					}

					@Override
					public void deserialize(final Chunk o, final Fields f) {
						assert false;
					}

					@Override
					public boolean canBeInstantiated() {
						return false;
					}

					@Override
					protected Chunk deserialize(final Fields fields) throws StreamCorruptedException {
						final World w = fields.getObject("world", World.class);
						final int x = fields.getPrimitive("x", int.class), z = fields.getPrimitive("z", int.class);
						if (w == null)
							throw new StreamCorruptedException();
						return w.getChunkAt(x, z);
					}

					// return c.getWorld().getName() + ":" + c.getX() + "," + c.getZ();
					@Override
					@Nullable
					public Chunk deserialize(final String s) {
						final String[] split = s.split("[:,]");
						if (split.length != 3)
							return null;
						final World w = Bukkit.getWorld(split[0]);
						if (w == null)
							return null;
						try {
							final int x = Integer.parseInt(split[1]);
							final int z = Integer.parseInt(split[1]);
							return w.getChunkAt(x, z);
						} catch (final NumberFormatException e) {
							return null;
						}
					}

					@Override
					public boolean mustSyncDeserialization() {
						return true;
					}
				}));

		Classes.registerClass(new RegistryClassInfo<>(Enchantment.class, Registry.ENCHANTMENT, "enchantment", "enchantments")
				.user("enchantments?")
				.name("Enchantment")
				.description("An enchantment, e.g. 'sharpness' or 'fortune'. Unlike <a href='#enchantmenttype'>enchantment type</a> " +
						"this type has no level, but you usually don't need to use this type anyway.",
						"NOTE: Minecraft namespaces are supported, ex: 'minecraft:basalt_deltas'.",
						"As of Minecraft 1.21 this will also support custom enchantments using namespaces, ex: 'myenchants:explosive'.")
				.examples("")
				.since("1.4.6")
				.before("enchantmenttype"));

		Material[] allMaterials = Material.values();
		Classes.registerClass(new ClassInfo<>(Material.class, "material")
				.name(ClassInfo.NO_DOC)
				.since("aliases-rework")
				.serializer(new Serializer<Material>() {
					@Override
					public Fields serialize(Material o) {
						Fields f = new Fields();
						f.putObject("i", o.ordinal());
						return f;
					}

					@Override
					public void deserialize(Material o, Fields f) {
						assert false;
					}

					@Override
					public Material deserialize(Fields f) throws StreamCorruptedException {
						Material mat = allMaterials[(int) f.getPrimitive("i")];
						assert mat != null; // Hope server owner didn't mod too much...
						return mat;
					}

					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}

					@Override
					protected boolean canBeInstantiated() {
						return false; // It is an enum, come on
					}
				}));

		Classes.registerClass(new ClassInfo<>(Metadatable.class, "metadataholder")
				.user("metadata ?holders?")
				.name("Metadata Holder")
				.description("Something that can hold metadata (e.g. an entity or block)")
				.examples("set metadata value \"super cool\" of player to true")
				.since("2.2-dev36"));

		Classes.registerClass(new EnumClassInfo<>(TeleportCause.class, "teleportcause", "teleport causes")
				.user("teleport ?(cause|reason|type)s?")
				.name("Teleport Cause")
				.description("The teleport cause in a <a href='#teleport'>teleport</a> event.")
				.since("2.2-dev35"));

		Classes.registerClass(new EnumClassInfo<>(SpawnReason.class, "spawnreason", "spawn reasons")
				.user("spawn(ing)? ?reasons?")
				.name("Spawn Reason")
				.description("The spawn reason in a <a href='#spawn'>spawn</a> event.")
				.since("2.3"));

		if (Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent")) {
			Classes.registerClass(new ClassInfo<>(CachedServerIcon.class, "cachedservericon")
					.user("server ?icons?")
					.name("Server Icon")
					.description("A server icon that was loaded using the <a href='#EffLoadServerIcon'>load server icon</a> effect.")
					.examples("")
					.since("2.3")
					.parser(new Parser<CachedServerIcon>() {
						@Override
						@Nullable
						public CachedServerIcon parse(final String s, final ParseContext context) {
							return null;
						}

						@Override
						public boolean canParse(final ParseContext context) {
							return false;
						}

						@Override
						public String toString(final CachedServerIcon o, int flags) {
							return "server icon";
						}

						@Override
						public String toVariableNameString(final CachedServerIcon o) {
							return "server icon";
						}
					}));
		}

		Classes.registerClass(new EnumClassInfo<>(FireworkEffect.Type.class, "fireworktype", "firework types")
				.user("firework ?types?")
				.name("Firework Type")
				.description("The type of a <a href='#fireworkeffect'>fireworkeffect</a>.")
				.since("2.4")
				.documentationId("FireworkType"));

		Classes.registerClass(new ClassInfo<>(FireworkEffect.class, "fireworkeffect")
				.user("firework ?effects?")
				.name("Firework Effect")
				.usage("See <a href='/#FireworkType'>Firework Types</a>")
				.description(
					"A configuration of effects that defines the firework when exploded",
					"which can be used in the <a href='#EffFireworkLaunch'>launch firework</a> effect.",
					"See the <a href='#ExprFireworkEffect'>firework effect</a> expression for detailed patterns."
				).defaultExpression(new EventValueExpression<>(FireworkEffect.class))
				.examples(
					"launch flickering trailing burst firework colored blue and green at player",
					"launch trailing flickering star colored purple, yellow, blue, green and red fading to pink at target entity",
					"launch ball large colored red, purple and white fading to light green and black at player's location with duration 1"
				).since("2.4")
				.parser(new Parser<FireworkEffect>() {
					@Override
					@Nullable
					public FireworkEffect parse(String input, ParseContext context) {
						return null;
					}

					@Override
					public boolean canParse(ParseContext context) {
						return false;
					}

					@Override
					public String toString(FireworkEffect effect, int flags) {
						return "Firework effect " + effect.toString();
					}

					@Override
					public String toVariableNameString(FireworkEffect effect) {
						return "firework effect " + effect.toString();
					}
				}));

		Classes.registerClass(new EnumClassInfo<>(Difficulty.class, "difficulty", "difficulties")
				.user("difficult(y|ies)")
				.name("Difficulty")
				.description("The difficulty of a <a href='#world'>world</a>.")
				.since("2.3"));

		Classes.registerClass(new EnumClassInfo<>(Status.class, "resourcepackstate", "resource pack states")
				.user("resource ?pack ?states?")
				.name("Resource Pack State")
				.description("The state in a <a href='#resource_pack_request_action'>resource pack request response</a> event.")
				.since("2.4"));

		Classes.registerClass(new EnumClassInfo<>(SoundCategory.class, "soundcategory", "sound categories")
				.user("sound ?categor(y|ies)")
				.name("Sound Category")
				.description("The category of a sound, they are used for sound options of Minecraft. " +
						"See the <a href='#EffPlaySound'>play sound</a> and <a href='#EffStopSound'>stop sound</a> effects.")
				.since("2.4"));

		Classes.registerClass(new EnumClassInfo<>(RegainReason.class, "healreason", "heal reasons")
				.user("(regen|heal) (reason|cause)")
				.name("Heal Reason")
				.description("The health regain reason in a <a href='#heal'>heal</a> event.")
				.since("2.5"));

		//noinspection rawtypes
		PatternedParser<GameRule> gameRuleParser = new PatternedParser<>() {
			
			private final String[] patterns = Arrays.stream(GameRule.values()).map(GameRule::getName).toArray(String[]::new);
			
			@Override
			public @Nullable GameRule parse(String string, ParseContext context) {
				return GameRule.getByName(string);
			}

			@Override
			public String toString(GameRule gameRule, int flags) {
				return gameRule.getName();
			}

			@Override
			public String toVariableNameString(GameRule gameRule) {
				return gameRule.getName();
			}

			@Override
			public String[] getPatterns() {
				return patterns;
			}
		};

		Classes.registerClass(new ClassInfo<>(GameRule.class, "gamerule")
			.user("gamerules?")
			.name("Gamerule")
			.description("A gamerule")
			.usage(gameRuleParser.getCombinedPatterns())
			.since("2.5")
			.requiredPlugins("Minecraft 1.13 or newer")
			.supplier(GameRule.values())
			.parser(gameRuleParser)
			.property(Property.NAME,
				"A gamerule's name, as text. Cannot be changed.",
				Skript.instance(),
				ExpressionPropertyHandler.of(GameRule::getName, String.class))
		);

		Classes.registerClass(new ClassInfo<>(EnchantmentOffer.class, "enchantmentoffer")
				.user("enchant[ment][ ]offers?")
				.name("Enchantment Offer")
				.description("The enchantmentoffer in an enchant prepare event.")
				.examples("on enchant prepare:",
					"\tset enchant offer 1 to sharpness 1",
					"\tset the cost of enchant offer 1 to 10 levels")
				.since("2.5")
				.parser(new Parser<EnchantmentOffer>() {
					@Override
					public boolean canParse(ParseContext context) {
						return false;
					}

					@Override
					public String toString(EnchantmentOffer eo, int flags) {
						return Classes.toString(eo.getEnchantment()) + " " + eo.getEnchantmentLevel();
					}

					@Override
					public String toVariableNameString(EnchantmentOffer eo) {
						return "offer:" + Classes.toString(eo.getEnchantment()) + "=" + eo.getEnchantmentLevel();
					}
				}));

		Classes.registerClass(new RegistryClassInfo<>(Attribute.class, Registry.ATTRIBUTE, "attributetype", "attribute types")
				.user("attribute ?types?")
				.name("Attribute Type")
				.description("Represents the type of an attribute. Note that this type does not contain any numerical values."
						+ "See <a href='https://minecraft.wiki/w/Attribute#Attributes'>attribute types</a> for more info.",
					"NOTE: Minecraft namespaces are supported, ex: 'minecraft:generic.attack_damage'.")
				.since("2.5"));

		Classes.registerClass(new EnumClassInfo<>(Environment.class, "environment", "environments")
				.user("(world ?)?environments?")
				.name("World Environment")
				.description("Represents the environment of a world.")
				.since("2.7"));

		if (Skript.classExists("io.papermc.paper.world.MoonPhase"))
			Classes.registerClass(new EnumClassInfo<>(MoonPhase.class, "moonphase", "moon phases")
					.user("(lunar|moon) ?phases?")
					.name("Moon Phase")
					.description("Represents the phase of a moon.")
					.since("2.7"));

		if (Skript.classExists("org.bukkit.event.player.PlayerQuitEvent$QuitReason"))
			Classes.registerClass(new EnumClassInfo<>(QuitReason.class, "quitreason", "quit reasons")
					.user("(quit|disconnect) ?(reason|cause)s?")
					.name("Quit Reason")
					.description("Represents a quit reason from a <a href='/#quit'>player quit server event</a>.")
					.since("2.8.0"));

		if (Skript.classExists("org.bukkit.event.inventory.InventoryCloseEvent$Reason"))
			Classes.registerClass(new EnumClassInfo<>(InventoryCloseEvent.Reason.class, "inventoryclosereason", "inventory close reasons")
					.user("inventory ?close ?reasons?")
					.name("Inventory Close Reasons")
					.description("The inventory close reason in an <a href='/#inventory_close'>inventory close event</a>.")
					.since("2.8.0"));

		Classes.registerClass(new EnumClassInfo<>(TransformReason.class, "transformreason", "transform reasons")
				.user("(entity)? ?transform ?(reason|cause)s?")
				.name("Transform Reason")
				.description("Represents a transform reason of an <a href='#entity transform'>entity transform event</a>.")
				.since("2.8.0"));

		Classes.registerClass(new EnumClassInfo<>(EntityUnleashEvent.UnleashReason.class, "unleashreason", "unleash reasons")
			.user("unleash ?(reason|cause)s?")
			.name("Unleash Reason")
			.description("Represents an unleash reason of an unleash event.")
			.since("2.10"));

		Classes.registerClass(new EnumClassInfo<>(ItemFlag.class, "itemflag", "item flags")
				.user("item ?flags?")
				.name("Item Flag")
				.description("Represents flags that may be applied to hide certain attributes of an item.")
				.since("2.10"));

		Classes.registerClass(new EnumClassInfo<>(ChangeReason.class,  "experiencecooldownchangereason", "experience cooldown change reasons")
			.user("(experience|[e]xp) cooldown change (reason|cause)s?")
			.name("Experience Cooldown Change Reason")
			.description("Represents a change reason of an <a href='#experience cooldown change event'>experience cooldown change event</a>.")
			.since("2.10"));

		Classes.registerClass(new RegistryClassInfo<>(Villager.Type.class, Registry.VILLAGER_TYPE, "villagertype", "villager types")
			.user("villager ?types?")
			.name("Villager Type")
			.description("Represents the different types of villagers. These are usually the biomes a villager can be from.")
			.after("biome")
			.since("2.10"));

		Classes.registerClass(new RegistryClassInfo<>(Villager.Profession.class, Registry.VILLAGER_PROFESSION, "villagerprofession", "villager professions")
			.user("villager ?professions?")
			.name("Villager Profession")
			.description("Represents the different professions of villagers.")
			.since("2.10"));

		if (Skript.classExists("org.bukkit.entity.EntitySnapshot")) {
			Classes.registerClass(new ClassInfo<>(EntitySnapshot.class, "entitysnapshot")
				.user("entity ?snapshots?")
				.name("Entity Snapshot")
				.description("Represents a snapshot of an entity's data.",
					"This includes all of the data associated with an entity (its name, health, attributes, etc.), at the time this expression is used. "
						+ "Essentially, these are a way to create templates for entities.",
					"Individual attributes of a snapshot cannot be modified or retrieved.")
				.since("2.10")
				.parser(new Parser<>() {
					@Override
					public boolean canParse(ParseContext context) {
						return false;
					}

					@Override
					public String toString(EntitySnapshot snapshot, int flags) {
						return EntityUtils.toSkriptEntityData(snapshot.getEntityType()).toString() + " snapshot";
					}

					@Override
					public String toVariableNameString(EntitySnapshot snapshot) {
						return toString(snapshot, 0);
					}
				})
			);
		}

		Classes.registerClass(new ClassInfo<>(WorldBorder.class, "worldborder")
			.user("world ?borders?")
			.name("World Border")
			.description("Represents the border of a world or player.")
			.since("2.11")
			.parser(new Parser<WorldBorder>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(WorldBorder border, int flags) {
					if (border.getWorld() == null)
						return "virtual world border";
					return "world border of world named '" + border.getWorld().getName() + "'";
				}

				@Override
				public String toVariableNameString(WorldBorder border) {
					return toString(border, 0);
				}
			})
			.defaultExpression(new EventValueExpression<>(WorldBorder.class)));

		Classes.registerClass(new ClassInfo<>(org.bukkit.block.banner.Pattern.class, "bannerpattern")
			.user("banner ?patterns?")
			.name("Banner Pattern")
			.description("Represents a banner pattern.")
			.since("2.10")
		);

		ClassInfo<?> patternTypeInfo;
		Registry<PatternType> patternRegistry = Bukkit.getRegistry(PatternType.class);
		if (patternRegistry != null) {
			patternTypeInfo = new RegistryClassInfo<>(PatternType.class, patternRegistry, "bannerpatterntype", "banner pattern types");
		} else {
			try {
				Class<?> patternClass = Class.forName("org.bukkit.block.banner.PatternType");
				if (patternClass.isEnum()) {
					//noinspection unchecked,rawtypes
					Class<? extends Enum> enumClass = (Class<? extends Enum>) patternClass;
					//noinspection rawtypes,unchecked
					patternTypeInfo = new EnumClassInfo<>(enumClass, "bannerpatterntype", "banner pattern types");
				} else {
					throw new IllegalStateException("PatternType is neither an enum nor a valid registry.");
				}
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		Classes.registerClass(patternTypeInfo
			.user("banner ?pattern ?types?")
			.name("Banner Pattern Type")
			.description("Represents the various banner patterns that can be applied to a banner.")
			.since("2.10")
		);

		if (Skript.classExists("io.papermc.paper.entity.TeleportFlag"))
			Classes.registerClass(new EnumClassInfo<>(SkriptTeleportFlag.class, "teleportflag", "teleport flags")
					.user("teleport ?flags?")
					.name("Teleport Flag")
					.description("Teleport Flags are settings to retain during a teleport.")
					.since("2.10"));

		Classes.registerClass(new ClassInfo<>(Vehicle.class, "vehicle")
			.user("vehicles?")
			.name("Vehicle")
			.description("Represents a vehicle.")
			.since("2.10.2")
			.changer(new EntityChanger())
		);

		Classes.registerClass(new EnumClassInfo<>(EquipmentSlot.class, "equipmentslot", "equipment slots")
			.user("equipment ?slots?")
			.name("Equipment Slot")
			.description("Represents an equipment slot of an entity.")
			.since("2.11")
		);

		Classes.registerClass(new EnumClassInfo<>(VillagerCareerChangeEvent.ChangeReason.class, "villagercareerchangereason", "villager career change reasons")
			.user("(villager )?career ?change ?reasons?")
			.name("Villager Career Change Reason")
			.description("Represents a reason why a villager changed its career.")
			.since("2.12")
		);

	}
}
