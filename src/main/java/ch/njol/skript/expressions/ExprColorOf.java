package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.Colorable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Name("Color of")
@Description("The <a href='./classes.html#color'>color</a> of an item, can also be used to color chat messages with \"&lt;%color of player's tool%&gt;this text is colored!\".")
@Examples({
	"on click on wool:",
		"\tsend \"This wool block is <%colour of block%>%colour of block%<reset>!\" to player",
		"\tset the color of the block to black"
})
@Since("1.2, INSERT VERSION (potions, maps and leather armor, block colors for MC 1.14+)")
@Keywords("colour")
public class ExprColorOf extends SimplePropertyExpression<Object, Color> {

	private static final DyeColor DEFAULT_MATERIAL_COLOR = DyeColor.WHITE;
	public static final Pattern MATERIAL_COLORS_PATTERN;

	static {
		DyeColor[] dyeColors = DyeColor.values();
		String colors = Arrays.stream(dyeColors)
			.map(DyeColor::name)
			.collect(Collectors.joining("|"));
		MATERIAL_COLORS_PATTERN = Pattern.compile("^(" + colors + ")_.+");
		register(ExprColorOf.class, Color.class, "colo[u]r[s]", "blocks/itemtypes/entities/fireworkeffects/slots");
	}

	@Override
	@Nullable
	public Color convert(Object obj) {
		if (obj instanceof FireworkEffect) {
			List<Color> colors = new ArrayList<>();
			((FireworkEffect) obj).getColors().stream()
				.map(ColorRGB::fromBukkitColor)
				.forEach(colors::add);
			return colors.isEmpty() ? null : colors.get(0);
		}
		// ItemType/Item/Slot
		if (obj instanceof ItemType || obj instanceof Item || obj instanceof Slot) {
			ItemMeta meta;
			Material material;
			if (obj instanceof ItemType) {
				meta = ((ItemType) obj).getItemMeta();
				material = ((ItemType) obj).getMaterial();
			} else if (obj instanceof Item) {
				meta = ((Item) obj).getItemStack().getItemMeta();
				material = ((Item) obj).getItemStack().getType();
			} else {
				if (((Slot) obj).getItem() == null)
					return null;
				meta = ((Slot) obj).getItem().getItemMeta();
				material = ((Slot) obj).getItem().getType();
			}

			if (meta == null)
				return null;

			if (meta instanceof LeatherArmorMeta) {
				LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;
				return ColorRGB.fromBukkitColor(leatherArmorMeta.getColor());
			} else if (meta instanceof MapMeta) {
				MapMeta mapMeta = (MapMeta) meta;
				if (mapMeta.hasColor())
					return ColorRGB.fromBukkitColor(mapMeta.getColor());
			} else if (meta instanceof PotionMeta) {
				PotionMeta potionMeta = (PotionMeta) meta;
				if (potionMeta.hasColor())
					return ColorRGB.fromBukkitColor(potionMeta.getColor());
			} else {
				return getMaterialColor(material);
			}
		}
		// Blocks
		if (obj instanceof Block) {
			return getMaterialColor(((Block) obj).getType());
		}

		// Colorable
		Colorable colorable = getColorable(obj);
		if (colorable == null)
			return null;

		DyeColor dyeColor = colorable.getColor();
		if (dyeColor == null)
			return null;
		return SkriptColor.fromDyeColor(dyeColor);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		Class<?> returnType = getExpr().getReturnType();

		if (FireworkEffect.class.isAssignableFrom(returnType))
			return CollectionUtils.array(Color[].class);

		if (mode != ChangeMode.SET && !getExpr().isSingle())
			return null;

		if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)
			return null;
		return CollectionUtils.array(Color.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		DyeColor originalColor = (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) ? null : ((Color) delta[0]).asDyeColor();
		org.bukkit.Color bukkitColor = (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) ? null : ((Color) delta[0]).asBukkitColor();

		for (Object obj : getExpr().getArray(event)) {
			DyeColor color = originalColor; // reset
			if (obj instanceof Item || obj instanceof ItemType || obj instanceof Slot) {
				ItemStack stack;
				if (obj instanceof ItemType itemType) {
					stack = itemType.getRandom();
				} else if (obj instanceof Item item) {
					stack = item.getItemStack();
				} else {
					stack = ((Slot) obj).getItem();
				}

				if (stack == null)
					continue;

				ItemMeta meta = stack.getItemMeta();
				if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
					leatherArmorMeta.setColor(bukkitColor);
					stack.setItemMeta(leatherArmorMeta);
				} else if (meta instanceof MapMeta mapMeta) {
					mapMeta.setColor(bukkitColor);
					stack.setItemMeta(mapMeta);
				} else if (meta instanceof PotionMeta potionMeta) {
					potionMeta.setColor(bukkitColor);
					stack.setItemMeta(potionMeta);
				} else {
					color = (color == null) ? DEFAULT_MATERIAL_COLOR : color;
					Material newItem = setMaterialColor(stack.getType(), color);
					if (newItem == null)
						continue;
					ItemMeta oldItemMeta = stack.getItemMeta();
					stack.setType(newItem);
					stack.setItemMeta(oldItemMeta);
					if (obj instanceof ItemType itemType) {
						ItemType newItemType = new ItemType(newItem);
						newItemType.setItemMeta(oldItemMeta);
						itemType.setTo(newItemType);
					} else if (obj instanceof Item item) {
						item.setItemStack(stack);
					} else if (obj instanceof Slot slot)
						slot.setItem(stack);
				}
			} else if (obj instanceof Colorable) {
				Colorable colorable = getColorable(obj);
				if (colorable != null) {
					colorable.setColor(color);
				}
			} else if (obj instanceof Block block) {
				Material blockType = block.getType();
				if (blockType == Material.WHITE_BED || blockType == Material.ORANGE_BED || blockType == Material.MAGENTA_BED ||
					blockType == Material.LIGHT_BLUE_BED || blockType == Material.YELLOW_BED || blockType == Material.LIME_BED ||
					blockType == Material.PINK_BED || blockType == Material.GRAY_BED || blockType == Material.LIGHT_GRAY_BED ||
					blockType == Material.CYAN_BED || blockType == Material.PURPLE_BED || blockType == Material.BLUE_BED ||
					blockType == Material.BROWN_BED || blockType == Material.GREEN_BED || blockType == Material.RED_BED ||
					blockType == Material.BLACK_BED || blockType == Material.WHITE_BANNER || blockType == Material.ORANGE_BANNER ||
					blockType == Material.MAGENTA_BANNER || blockType == Material.LIGHT_BLUE_BANNER || blockType == Material.YELLOW_BANNER ||
					blockType == Material.LIME_BANNER || blockType == Material.PINK_BANNER || blockType == Material.GRAY_BANNER ||
					blockType == Material.LIGHT_GRAY_BANNER || blockType == Material.CYAN_BANNER || blockType == Material.PURPLE_BANNER ||
					blockType == Material.BLUE_BANNER || blockType == Material.BROWN_BANNER || blockType == Material.GREEN_BANNER ||
					blockType == Material.RED_BANNER || blockType == Material.BLACK_BANNER) {
					continue;
				}

				color = (color == null) ? DEFAULT_MATERIAL_COLOR : color;

				Material newBlock = setMaterialColor(block.getType(), color);
				if (newBlock == null)
					continue;

				block.setType(newBlock);
			} else if (obj instanceof FireworkEffect effect) {
				Color[] input = (Color[]) delta;
				switch (mode) {
					case ADD -> {
						for (Color inputColor : input)
							effect.getColors().add(inputColor.asBukkitColor());
					}
					case REMOVE, REMOVE_ALL -> {
						for (Color inputColor : input)
							effect.getColors().remove(inputColor.asBukkitColor());
					}
					case DELETE, RESET -> effect.getColors().clear();
					case SET -> {
						effect.getColors().clear();
						for (Color inputColor : input)
							effect.getColors().add(inputColor.asBukkitColor());
					}
					default -> {
						break;
					}
				}
			}
		}
	}

	@Override
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}

	@Override
	protected String getPropertyName() {
		return "color";
	}

	@Nullable
	private static Colorable getColorable(Object object) {
		if (object instanceof Colorable colorable) {
			return colorable;
		} else if (object instanceof Block block) {
			return (Colorable) block.getState().getBlockData();
		} else if (object instanceof Item item) {
			return getColorable(item.getItemStack().getType());
		} else if (object instanceof Slot slot) {
			return getColorable(slot.getItem().getType());
		} else if (object instanceof ItemType itemType) {
			return getColorable(itemType.getMaterial());
		} else if (object instanceof ItemStack itemStack) {
			return getColorable(itemStack.getType());
		}
		return null;
	}


	private static @Nullable Color getMaterialColor(@Nullable Material material) {
		if (material == null)
			return null;
		String matName = material.name();
		Matcher matcher = MATERIAL_COLORS_PATTERN.matcher(matName);
		if (matcher.find())
			return SkriptColor.fromDyeColor(DyeColor.valueOf(matcher.group(1)));
		return null;
	}

	private static @Nullable Material setMaterialColor(Material material, DyeColor color) {
		String matName = material.name();
		Matcher matcher = MATERIAL_COLORS_PATTERN.matcher(matName);
		if (matcher.find())
			matName = matName.replace(matcher.group(1), color.name());
		try {
			return Material.valueOf(matName);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
