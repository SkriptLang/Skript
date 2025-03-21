package ch.njol.skript.classes.data;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.*;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.lang.util.common.AnyAmount;
import ch.njol.skript.lang.util.common.AnyContains;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.lang.util.common.AnyValued;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.localization.RegexMessage;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.*;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.util.visual.VisualEffect;
import ch.njol.skript.util.visual.VisualEffects;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.util.SkriptQueue;
import org.skriptlang.skript.util.Executable;

import java.io.File;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
public class SkriptClasses {
	public SkriptClasses() {}

	static {
		//noinspection unchecked
		Classes.registerClass(new ClassInfo<>(ClassInfo.class, "classinfo")
				.user("types?")
				.name("Type")
				.description("Represents a type, e.g. number, object, item type, location, block, world, entity type, etc.",
						"This is mostly used for expressions like 'event-&lt;type&gt;', '&lt;type&gt;-argument', 'loop-&lt;type&gt;', etc., e.g. event-world, number-argument and loop-player.")
				.usage("See the type name patterns of all types - including this one")
				.examples("{variable} is a number # check whether the variable contains a number, e.g. -1 or 5.5",
						"{variable} is a type # check whether the variable contains a type, e.g. number or player",
						"{variable} is an object # will always succeed if the variable is set as everything is an object, even types.",
						"disable PvP in the event-world",
						"kill the loop-entity")
				.since("2.0")
				.after("entitydata", "entitytype", "itemtype")
				.supplier(() -> (Iterator) Classes.getClassInfos().iterator())
				.parser(new Parser<ClassInfo>() {
					@Override
					@Nullable
					public ClassInfo parse(final String s, final ParseContext context) {
						return Classes.getClassInfoFromUserInput(Noun.stripIndefiniteArticle(s));
					}

					@Override
					public String toString(final ClassInfo c, final int flags) {
						return c.toString(flags);
					}

					@Override
					public String toVariableNameString(final ClassInfo c) {
						return c.getCodeName();
					}

					@Override
					public String getDebugMessage(final ClassInfo c) {
						return c.getCodeName();
					}

				})
				.serializer(new Serializer<ClassInfo>() {
					@Override
					public Fields serialize(final ClassInfo c) {
						final Fields f = new Fields();
						f.putObject("codeName", c.getCodeName());
						return f;
					}

					@Override
					public boolean canBeInstantiated() {
						return false;
					}

					@Override
					public void deserialize(final ClassInfo o, final Fields f) throws StreamCorruptedException {
						assert false;
					}

					@Override
					protected ClassInfo deserialize(final Fields fields) throws StreamCorruptedException {
						final String codeName = fields.getObject("codeName", String.class);
						if (codeName == null)
							throw new StreamCorruptedException();
						final ClassInfo<?> ci = Classes.getClassInfoNoError(codeName);
						if (ci == null)
							throw new StreamCorruptedException("Invalid ClassInfo " + codeName);
						return ci;
					}

//					return c.getCodeName();
					@Override
					@Nullable
					public ClassInfo deserialize(final String s) {
						return Classes.getClassInfoNoError(s);
					}

					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}));

		Classes.registerClass(new ClassInfo<>(WeatherType.class, "weathertype")
				.user("weather ?types?", "weather conditions?", "weathers?")
				.name("Weather Type")
				.description("The weather types sunny, rainy, and thundering.")
				.usage("clear/sun/sunny, rain/rainy/raining, and thunder/thundering/thunderstorm")
				.examples("is raining",
						"is sunny in the player's world",
						"message \"It is %weather in the argument's world% in %world of the argument%\"")
				.since("1.0")
				.defaultExpression(new SimpleLiteral<>(WeatherType.CLEAR, true))
				.parser(new Parser<WeatherType>() {
					@Override
					@Nullable
					public WeatherType parse(final String s, final ParseContext context) {
						return WeatherType.parse(s);
					}

					@Override
					public String toString(final WeatherType o, final int flags) {
						return o.toString(flags);
					}

					@Override
					public String toVariableNameString(final WeatherType o) {
						return "" + o.name().toLowerCase(Locale.ENGLISH);
					}

				})
				.serializer(new EnumSerializer<>(WeatherType.class)));

		Classes.registerClass(new ClassInfo<>(ItemType.class, "itemtype")
				.user("item ?types?", "materials?")
				.name("Item Type")
				.description("An item type is an alias, e.g. 'a pickaxe', 'all plants', etc., and can result in different items when added to an inventory, " +
						"and unlike <a href='#itemstack'>items</a> they are well suited for checking whether an inventory contains a certain item or whether a certain item is of a certain type.",
						"An item type can also have one or more <a href='#enchantmenttype'>enchantments</a> with or without a specific level defined, " +
								"and can optionally start with 'all' or 'every' to make this item type represent <i>all</i> types that the alias represents, including data ranges.")
				.usage("[&lt;number&gt; [of]] [all/every] &lt;alias&gt; [of &lt;enchantment&gt; [&lt;level&gt;] [,/and &lt;more enchantments...&gt;]]")
				.examples("give 4 torches to the player",
						"add all slabs to the inventory of the block",
						"player's tool is a diamond sword of sharpness",
						"remove a pickaxes of fortune 4 from {stored items::*}",
						"set {_item} to 10 of every upside-down stair",
						"block is dirt or farmland")
				.since("1.0")
				.before("itemstack", "entitydata", "entitytype")
				.after("number", "integer", "long", "time")
				.supplier(() -> Arrays.stream(Material.values())
					.map(ItemType::new)
					.iterator())
				.parser(new Parser<ItemType>() {
					@Override
					@Nullable
					public ItemType parse(final String s, final ParseContext context) {
						return Aliases.parseItemType(s);
					}

					@Override
					public String toString(final ItemType t, final int flags) {
						return t.toString(flags);
					}

					@Override
					public String getDebugMessage(final ItemType t) {
						return t.getDebugMessage();
					}

					@Override
					public String toVariableNameString(final ItemType t) {
						final StringBuilder b = new StringBuilder("itemtype:");
						b.append(t.getInternalAmount());
						b.append("," + t.isAll());
						// TODO this is missing information
						for (final ItemData d : t.getTypes()) {
							b.append("," + d.getType());
						}
						final EnchantmentType[] enchs = t.getEnchantmentTypes();
						if (enchs != null) {
							b.append("|");
							for (final EnchantmentType ench : enchs) {
								Enchantment e = ench.getType();
								if (e == null)
									continue;
								b.append("#" + e.getKey().toString());
								b.append(":" + ench.getLevel());
							}
						}
						return "" + b.toString();
					}
				})
				.cloner(ItemType::clone)
				.serializer(new YggdrasilSerializer<>()));

		Classes.registerClass(new ClassInfo<>(Time.class, "time")
				.user("times?")
				.name("Time")
				.description("A time is a point in a minecraft day's time (i.e. ranges from 0:00 to 23:59), which can vary per world.",
						"See <a href='#date'>date</a> and <a href='#timespan'>timespan</a> for the other time types of Skript.")
				.usage("##:##",
						"##[:##][ ]am/pm")
				.examples("at 20:00:",
						"	time is 8 pm",
						"	broadcast \"It's %time%\"")
				.since("1.0")
				.defaultExpression(new EventValueExpression<>(Time.class))
				.parser(new Parser<Time>() {
					@Override
					@Nullable
					public Time parse(final String s, final ParseContext context) {
						return Time.parse(s);
					}

					@Override
					public String toString(final Time t, final int flags) {
						return t.toString();
					}

					@Override
					public String toVariableNameString(final Time o) {
						return "time:" + o.getTicks();
					}
				}).serializer(new YggdrasilSerializer<>()));

		Classes.registerClass(new ClassInfo<>(Timespan.class, "timespan")
				.user("time ?spans?")
				.name("Timespan")
				.description("A timespan is a difference of two different dates or times, " +
						"e.g '10 minutes'. Timespans are always displayed as real life time, but can be defined as minecraft time, " +
						"e.g. '5 minecraft days and 12 hours'.",
						"NOTE: Months always have the value of 30 days, and years of 365 days.",
						"See <a href='#date'>date</a> and <a href='#time'>time</a> for the other time types of Skript.")
				.usage("&lt;number&gt; [minecraft/mc/real/rl/irl] ticks/seconds/minutes/hours/days/weeks/months/years [[,/and] &lt;more...&gt;]",
						"[###:]##:##[.####] ([hours:]minutes:seconds[.milliseconds])")
				.examples("every 5 minecraft days:",
						"	wait a minecraft second and 5 ticks",
						"every 10 mc days and 12 hours:",
						"	halt for 12.7 irl minutes, 12 hours and 120.5 seconds")
				.since("1.0, 2.6.1 (weeks, months, years)")
				.parser(new Parser<Timespan>() {
					@Override
					@Nullable
					public Timespan parse(final String s, final ParseContext context) {
						try {
							return Timespan.parse(s, context);
						} catch (IllegalArgumentException e) {
							Skript.error("'" + s + "' is not a valid timespan");
							return null;
						}
					}

					@Override
					public String toString(final Timespan t, final int flags) {
						return t.toString(flags);
					}

					@Override
					public String toVariableNameString(final Timespan o) {
						return "timespan:" + o.getAs(Timespan.TimePeriod.MILLISECOND);
					}
				}).serializer(new YggdrasilSerializer<>()));

		// TODO remove
		Classes.registerClass(new ClassInfo<>(Timeperiod.class, "timeperiod")
				.user("time ?periods?", "durations?")
				.name("Timeperiod")
				.description("A period of time between two <a href='#time'>times</a>. Mostly useful since you can use this to test for whether it's day, night, dusk or dawn in a specific world.",
						"This type might be removed in the future as you can use 'time of world is between x and y' as a replacement.")
				.usage("##:## - ##:##",
						"dusk/day/dawn/night")
				.examples("time in world is night")
				.since("1.0")
				.before("timespan") // otherwise "day" gets parsed as '1 day'
				.defaultExpression(new SimpleLiteral<>(new Timeperiod(0, 23999), true))
				.parser(new Parser<Timeperiod>() {
					@Override
					@Nullable
					public Timeperiod parse(final String s, final ParseContext context) {
						if (s.equalsIgnoreCase("day")) {
							return new Timeperiod(0, 11999);
						} else if (s.equalsIgnoreCase("dusk")) {
							return new Timeperiod(12000, 13799);
						} else if (s.equalsIgnoreCase("night")) {
							return new Timeperiod(13800, 22199);
						} else if (s.equalsIgnoreCase("dawn")) {
							return new Timeperiod(22200, 23999);
						}
						final int c = s.indexOf('-');
						if (c == -1) {
							final Time t = Time.parse(s);
							if (t == null)
								return null;
							return new Timeperiod(t.getTicks());
						}
						final Time t1 = Time.parse("" + s.substring(0, c).trim());
						final Time t2 = Time.parse("" + s.substring(c + 1).trim());
						if (t1 == null || t2 == null)
							return null;
						return new Timeperiod(t1.getTicks(), t2.getTicks());
					}

					@Override
					public String toString(final Timeperiod o, final int flags) {
						return o.toString();
					}

					@Override
					public String toVariableNameString(final Timeperiod o) {
						return "timeperiod:" + o.start + "-" + o.end;
					}
				}).serializer(new YggdrasilSerializer<>()));

		Classes.registerClass(new ClassInfo<>(Date.class, "date")
				.user("dates?")
				.name("Date")
				.description("A date is a certain point in the real world's time which can be obtained with <a href='./expressions.html#ExprNow'>now expression</a>, <a href='./expressions.html#ExprUnixDate'>unix date expression</a> and <a href='./functions.html#date'>date function</a>.",
						"See <a href='#time'>time</a> and <a href='#timespan'>timespan</a> for the other time types of Skript.")
				.usage("")
				.examples("set {_yesterday} to now",
						"subtract a day from {_yesterday}",
						"# now {_yesterday} represents the date 24 hours before now")
				.since("1.4")
				.serializer(new Serializer<Date>() {

					@Override
					public Fields serialize(Date date) {
						Fields fields = new Fields();
						fields.putPrimitive("timestamp", date.getTime());
						return fields;
					}


					@Override
					protected Date deserialize(Fields fields) throws StreamCorruptedException {
						long time;
						if (fields.hasField("time")) { // compatibility for 2.10.0 (#7542)
							time = fields.getPrimitive("time", long.class);
						} else {
							time = fields.getPrimitive("timestamp", long.class);
						}
						return new Date(time);
					}

					@Override
					public void deserialize(Date date, Fields fields) {
						throw new IllegalStateException("Cannot deserialize to an existing date object.");
					}

					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}

					@Override
					protected boolean canBeInstantiated() {
						return false;
					}
				}));

		Classes.registerClass(new ClassInfo<>(Direction.class, "direction")
				.user("directions?")
				.name("Direction")
				.description("A direction, e.g. north, east, behind, 5 south east, 1.3 meters to the right, etc.",
						"<a href='#location'>Locations</a> and some <a href='#block'>blocks</a> also have a direction, but without a length.",
						"Please note that directions have changed extensively in the betas and might not work perfectly. They can also not be used as command arguments.")
				.usage("see <a href='./expressions.html#ExprDirection'>direction (expression)</a>")
				.examples("set the block below the victim to a chest",
						"loop blocks from the block infront of the player to the block 10 below the player:",
						"	set the block behind the loop-block to water")
				.since("2.0")
				.defaultExpression(new SimpleLiteral<>(new Direction(new double[] {0, 0, 0}), true))
				.parser(new Parser<Direction>() {
					@Override
					@Nullable
					public Direction parse(final String s, final ParseContext context) {
						return null;
					}

					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}

					@Override
					public String toString(final Direction o, final int flags) {
						return o.toString();
					}

					@Override
					public String toVariableNameString(final Direction o) {
						return o.toString();
					}
				})
				.serializer(new YggdrasilSerializer<>()));

		Classes.registerClass(new ClassInfo<>(Slot.class, "slot")
				.user("(inventory )?slots?")
				.name("Slot")
				.description("Represents a single slot of an <a href='#inventory'>inventory</a>. " +
						"Notable slots are the <a href='./expressions.html#ExprArmorSlot'>armour slots</a> and <a href='./expressions/#ExprFurnaceSlot'>furnace slots</a>. ",
						"The most important property that distinguishes a slot from an <a href='#itemstack'>item</a> is its ability to be changed, e.g. it can be set, deleted, enchanted, etc. " +
								"(Some item expressions can be changed as well, e.g. items stored in variables. " +
								"For that matter: slots are never saved to variables, only the items they represent at the time when the variable is set).",
						"Please note that <a href='./expressions.html#ExprTool'>tool</a> can be regarded a slot, but it can actually change it's position, i.e. doesn't represent always the same slot.")
				.usage("")
				.examples("set tool of player to dirt",
						"delete helmet of the victim",
						"set the color of the player's tool to green",
						"enchant the player's chestplate with projectile protection 5")
				.since("")
				.defaultExpression(new EventValueExpression<>(Slot.class))
				.changer(new Changer<Slot>() {
					@SuppressWarnings("unchecked")
					@Override
					@Nullable
					public Class<Object>[] acceptChange(final ChangeMode mode) {
						if (mode == ChangeMode.RESET)
							return null;
						if (mode == ChangeMode.SET)
							return new Class[] {ItemType[].class, ItemStack[].class};
						return new Class[] {ItemType.class, ItemStack.class};
					}

					@Override
					public void change(final Slot[] slots, final @Nullable Object[] deltas, final ChangeMode mode) {
						if (mode == ChangeMode.SET) {
							if (deltas != null) {
								if (deltas.length == 1) {
									final Object delta = deltas[0];
									for (final Slot slot : slots) {
										slot.setItem(delta instanceof ItemStack ? (ItemStack) delta : ((ItemType) delta).getItem().getRandom());
									}
								} else if (deltas.length == slots.length) {
									for (int i = 0; i < slots.length; i++) {
										final Object delta = deltas[i];
										slots[i].setItem(delta instanceof ItemStack ? (ItemStack) delta : ((ItemType) delta).getItem().getRandom());
									}
								}
							}
							return;
						}
						final Object delta = deltas == null ? null : deltas[0];
						for (final Slot slot : slots) {
							switch (mode) {
								case ADD:
									assert delta != null;
									if (delta instanceof ItemStack) {
										final ItemStack i = slot.getItem();
										if (i == null || i.getType() == Material.AIR || ItemUtils.itemStacksEqual(i, (ItemStack) delta)) {
											if (i != null && i.getType() != Material.AIR) {
												i.setAmount(Math.min(i.getAmount() + ((ItemStack) delta).getAmount(), i.getMaxStackSize()));
												slot.setItem(i);
											} else {
												slot.setItem((ItemStack) delta);
											}
										}
									} else {
										slot.setItem(((ItemType) delta).getItem().addTo(slot.getItem()));
									}
									break;
								case REMOVE:
								case REMOVE_ALL:
									assert delta != null;
									if (delta instanceof ItemStack) {
										final ItemStack i = slot.getItem();
										if (i != null && ItemUtils.itemStacksEqual(i, (ItemStack) delta)) {
											final int a = mode == ChangeMode.REMOVE_ALL ? 0 : i.getAmount() - ((ItemStack) delta).getAmount();
											if (a <= 0) {
												slot.setItem(null);
											} else {
												i.setAmount(a);
												slot.setItem(i);
											}
										}
									} else {
										if (mode == ChangeMode.REMOVE)
											slot.setItem(((ItemType) delta).removeFrom(slot.getItem()));
										else
											// REMOVE_ALL
											slot.setItem(((ItemType) delta).removeAll(slot.getItem()));
									}
									break;
								case DELETE:
									slot.setItem(null);
									break;
								case RESET:
									assert false;
							}
						}
					}
				})
				.parser(new Parser<Slot>() {
					@Override
					public boolean canParse(final ParseContext context) {
						return false;
					}

					@Override
					public String toString(Slot o, int flags) {
						ItemStack i = o.getItem();
						if (i == null)
							return new ItemType(Material.AIR).toString(flags);
						return ItemType.toString(i, flags);
					}

					@Override
					public String toVariableNameString(Slot o) {
						return "slot:" + o.toString();
					}
				})
				.serializeAs(ItemStack.class));

		Classes.registerClass(new ClassInfo<>(Color.class, "color")
				.user("colou?rs?")
				.name("Color")
				.description("Wool, dye and chat colors.")
				.usage("black, dark grey/dark gray, grey/light grey/gray/light gray/silver, white, blue/dark blue, cyan/aqua/dark cyan/dark aqua, light blue/light cyan/light aqua, green/dark green, light green/lime/lime green, yellow/light yellow, orange/gold/dark yellow, red/dark red, pink/light red, purple/dark purple, magenta/light purple, brown/indigo")
				.examples("color of the sheep is red or black",
						"set the color of the block to green",
						"message \"You're holding a <%color of tool%>%color of tool%<reset> wool block\"")
				.since("")
				.supplier(SkriptColor.values())
				.parser(new Parser<Color>() {
					@Override
					@Nullable
					public Color parse(String input, ParseContext context) {
						Color rgbColor = ColorRGB.fromString(input);
						if (rgbColor != null)
							return rgbColor;
						return SkriptColor.fromName(input);
					}

					@Override
					public String toString(Color c, int flags) {
						return c.getName();
					}

					@Override
					public String toVariableNameString(Color color) {
						return "" + color.getName().toLowerCase(Locale.ENGLISH).replace('_', ' ');
					}
				}));

		Classes.registerClass(new ClassInfo<>(StructureType.class, "structuretype")
				.user("tree ?types?", "trees?")
				.name("Tree Type")
				.description("A tree type represents a tree species or a huge mushroom species. These can be generated in a world with the <a href='./effects.html#EffTree'>generate tree</a> effect.")
				.usage("[any] &lt;general tree/mushroom type&gt;, e.g. tree/any jungle tree/etc.", "&lt;specific tree/mushroom species&gt;, e.g. red mushroom/small jungle tree/big regular tree/etc.")
				.examples("grow any regular tree at the block",
						"grow a huge red mushroom above the block")
				.since("")
				.defaultExpression(new SimpleLiteral<>(StructureType.TREE, true))
				.parser(new Parser<StructureType>() {
					@Override
					@Nullable
					public StructureType parse(final String s, final ParseContext context) {
						return StructureType.fromName(s);
					}

					@Override
					public String toString(final StructureType o, final int flags) {
						return o.toString(flags);
					}

					@Override
					public String toVariableNameString(final StructureType o) {
						return "" + o.name().toLowerCase(Locale.ENGLISH);
					}
				}).serializer(new EnumSerializer<>(StructureType.class)));

		Classes.registerClass(new ClassInfo<>(EnchantmentType.class, "enchantmenttype")
				.user("enchant(ing|ment) types?")
				.name("Enchantment Type")
				.description("An enchantment with an optional level, e.g. 'sharpness 2' or 'fortune'.")
				.usage("&lt;enchantment&gt; [&lt;level&gt;]")
				.examples("enchant the player's tool with sharpness 5",
						"helmet is enchanted with waterbreathing")
				.since("1.4.6")
				.parser(new Parser<EnchantmentType>() {
					@Override
					@Nullable
					public EnchantmentType parse(final String s, final ParseContext context) {
						return EnchantmentType.parse(s);
					}

					@Override
					public String toString(final EnchantmentType t, final int flags) {
						return t.toString();
					}

					@Override
					public String toVariableNameString(final EnchantmentType o) {
						return o.toString();
					}
				})
				.serializer(new YggdrasilSerializer<>()));

		Classes.registerClass(new ClassInfo<>(Experience.class, "experience")
				.user("experience ?(points?)?")
				.name("Experience")
				.description("Experience points. Please note that Bukkit only allows to give XP, but not remove XP from players. " +
						"You can however change a player's <a href='./expressions.html#ExprLevel'>level</a> and <a href='./expressions/#ExprLevelProgress'>level progress</a> freely.")
				.usage("[&lt;number&gt;] ([e]xp|experience [point[s]])")
				.examples("give 10 xp to the player")
				.since("2.0")
				.parser(new Parser<Experience>() {
					private final RegexMessage pattern = new RegexMessage("types.experience.pattern", Pattern.CASE_INSENSITIVE);

					@Override
					@Nullable
					public Experience parse(String s, final ParseContext context) {
						int xp = -1;
						if (s.matches("\\d+ .+")) {
							xp = Utils.parseInt("" + s.substring(0, s.indexOf(' ')));
							s = "" + s.substring(s.indexOf(' ') + 1);
						}
						if (pattern.matcher(s).matches())
							return new Experience(xp);
						return null;
					}

					@Override
					public String toString(final Experience xp, final int flags) {
						return xp.toString();
					}

					@Override
					public String toVariableNameString(final Experience xp) {
						return "" + xp.getXP();
					}

				})
				.serializer(new YggdrasilSerializer<>()));

		Classes.registerClass(new ClassInfo<>(VisualEffect.class, "visualeffect")
				.name("Visual Effect")
				.description("A visible effect, e.g. particles.")
				.examples("show wolf hearts on the clicked wolf",
						"play mob spawner flames at the targeted block to the player")
				.usage(VisualEffects.getAllNames())
				.since("2.1")
				.user("(visual|particle) effects?")
				.after("itemtype")
				.parser(new Parser<VisualEffect>() {
					@Override
					@Nullable
					public VisualEffect parse(String s, ParseContext context) {
						return VisualEffects.parse(s);
					}

					@Override
					public String toString(VisualEffect e, int flags) {
						return e.toString(flags);
					}

					@Override
					public String toVariableNameString(VisualEffect e) {
						return e.toString();
					}

				})
				.serializer(new YggdrasilSerializer<>()));

		Classes.registerClass(new ClassInfo<>(GameruleValue.class, "gamerulevalue")
				.user("gamerule values?")
				.name("Gamerule Value")
				.description("A wrapper for the value of a gamerule for a world.")
				.usage("")
				.examples("")
				.since("2.5")
				.serializer(new YggdrasilSerializer<GameruleValue>())
		);

		Classes.registerClass(new ClassInfo<>(SkriptQueue.class, "queue")
				.user("queues?")
				.name("Queue")
				.description("A queued list of values. Entries are removed from a queue when they are queried.")
				.examples(
					"set {queue} to a new queue",
					"add \"hello\" to {queue}",
					"broadcast the 1st element of {queue}"
				)
				.since("2.10")
				.changer(new Changer<>() {
					@Override
					public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
						return switch (mode) {
							case ADD, REMOVE, DELETE -> new Class[] {Object.class};
							case RESET -> new Class[0];
							default -> null;
						};
					}

					@Override
					public void change(SkriptQueue[] what, Object @Nullable [] delta, ChangeMode mode) {
						for (SkriptQueue queue : what) {
							switch (mode) {
								case RESET, DELETE -> queue.clear();
								case ADD -> {
									assert delta != null;
									queue.addAll(Arrays.asList(delta));
								}
								case REMOVE -> {
									assert delta != null;
									queue.removeAll(Arrays.asList(delta));
								}
							}
						}
					}
				})
				.parser(new Parser<SkriptQueue>() {

					@Override
					public boolean canParse(ParseContext context) {
						return false;
					}

					@Override
					public String toString(SkriptQueue queue, int flags) {
						return Classes.toString(queue.toArray(), flags, true);
					}

					@Override
					public String toVariableNameString(SkriptQueue queue) {
						return this.toString(queue, 0);
					}

				})
				.serializer(new Serializer<SkriptQueue>() {
					@Override
					public Fields serialize(SkriptQueue queue) throws NotSerializableException {
						Fields fields = new Fields();
						fields.putObject("contents", queue.toArray());
						return fields;
					}

					@Override
					public void deserialize(SkriptQueue queue, Fields fields)
						throws StreamCorruptedException, NotSerializableException {
						Object[] contents = fields.getObject("contents", Object[].class);
						queue.clear();
						if (contents != null)
							queue.addAll(List.of(contents));
					}

					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}

					@Override
					protected boolean canBeInstantiated() {
						return true;
					}
				})
		);


		Classes.registerClass(new ClassInfo<>(Config.class, "config")
			.user("configs?")
			.name("Config")
			.description("A configuration (or code) loaded by Skript, such as the config.sk or aliases.",
				"Configs can be reloaded or navigated to find options.")
			.usage("")
			.examples("the skript config")
			.since("2.10")
			.parser(new Parser<Config>() {

				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(Config config, int flags) {
					@Nullable File file = config.getFile();
					if (file == null)
						return config.getFileName();
					return Skript.getInstance().getDataFolder().getAbsoluteFile().toPath()
						.relativize(file.toPath().toAbsolutePath()).toString();
				}

				@Override
				public String toVariableNameString(Config config) {
					return this.toString(config, 0);
				}
			}));

		Classes.registerClass(new ClassInfo<>(Node.class, "node")
			.user("nodes?")
			.name("Node")
			.description("A node (entry) from a script config file.",
				"This may have navigable children.")
			.usage("")
			.examples("the current script")
			.since("2.10")
			.parser(new Parser<Node>() {

				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(Node node, int flags) {
					return node.getPath();
				}

				@Override
				public String toVariableNameString(Node node) {
					return this.toString(node, 0);
				}

			}));

		Classes.registerClass(new ClassInfo<>(Script.class, "script")
				.user("scripts?")
				.name("Script")
				.description("A script loaded by Skript.",
					"Disabled scripts will report as being empty since their content has not been loaded.")
				.usage("")
				.examples("the current script")
				.since("2.10")
				.parser(new Parser<Script>() {
					final Path path = Skript.getInstance().getScriptsFolder().getAbsoluteFile().toPath();

					@Override
					public boolean canParse(final ParseContext context) {
						return switch (context) {
							case PARSE, COMMAND -> true;
							default -> false;
						};
					}

					@Override
					@Nullable
					public Script parse(final String name, final ParseContext context) {
						return switch (context) {
							case PARSE, COMMAND -> {
								@Nullable File file = ScriptLoader.getScriptFromName(name);
								if (file == null || !file.isFile())
									yield null;
								yield ScriptLoader.getScript(file);
							}
							default -> null;
						};
					}

					@Override
					public String toString(final Script script, final int flags) {
						@Nullable File file = script.getConfig().getFile();
						if (file == null)
							return script.getConfig().getFileName();
						return path.relativize(file.toPath().toAbsolutePath()).toString();
					}
					@Override
					public String toVariableNameString(final Script script) {
						return this.toString(script, 0);
					}
				}));

		Classes.registerClass(new ClassInfo<>(Executable.class, "executable")
			.user("executables?")
			.name("Executable")
			.description("Something that can be executed (run) and may accept arguments, e.g. a function.",
					"This may also return a result.")
			.examples("run {_function} with arguments 1 and true")
			.since("2.10"));

		Classes.registerClass(new ClassInfo<>(DynamicFunctionReference.class, "function")
			.user("functions?")
			.name("Function")
			.description("A function loaded by Skript.",
					"This can be executed (with arguments) and may return a result.")
			.examples("run {_function} with arguments 1 and true",
					"set {_result} to the result of {_function}")
			.since("2.10")
			.parser(new Parser<DynamicFunctionReference<?>>() {

				@Override
				public boolean canParse(final ParseContext context) {
					return switch (context) {
						case PARSE, COMMAND -> true;
						default -> false;
					};
				}

				@Override
				@Nullable
				public DynamicFunctionReference<?> parse(final String name, final ParseContext context) {
					return switch (context) {
						case PARSE, COMMAND -> DynamicFunctionReference.parseFunction(name);
						default -> null;
					};
				}

				@Override
				public String toString(DynamicFunctionReference<?> function, final int flags) {
					return function.toString();
				}

				@Override
				public String toVariableNameString(DynamicFunctionReference<?> function) {
					return this.toString(function, 0);
				}
			}));

		Classes.registerClass(new AnyInfo<>(AnyNamed.class, "named")
				.name("Any Named Thing")
				.description("Something that has a name (e.g. an item).")
				.usage("")
				.examples("{thing}'s name")
				.since("2.10")
		);

		Classes.registerClass(new AnyInfo<>(AnyAmount.class, "numbered")
				.name("Any Numbered/Sized Thing")
				.description("Something that has an amount or size.")
				.usage("")
				.examples("the size of {thing}", "the amount of {thing}")
				.since("2.10")
		);

		Classes.registerClass(new AnyInfo<>(AnyValued.class, "valued")
			.name("Any Valued Thing")
			.description("Something that has a value.")
			.usage("")
			.examples("the text of {node}")
			.since("2.10")
		);

		Classes.registerClass(new AnyInfo<>(AnyContains.class, "containing")
				.user("any container")
				.name("Anything with Contents")
				.description("Something that contains other things.")
				.usage("")
				.examples("{a} contains {b}")
				.since("2.10")
		);
	}

}
