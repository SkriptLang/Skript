package org.skriptlang.skript.bukkit.displays;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.variables.Variables;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DisplayData extends EntityData<Display> {

	public static final Color DEFAULT_BACKGROUND_COLOR = ColorRGB.fromRGBA(0, 0, 0, 64).asBukkitColor();

	static {
		EntityData.register(DisplayData.class, "display", Display.class, 0, DisplayType.codeNames);
		Variables.yggdrasil.registerSingleClass(DisplayType.class, "DisplayType");
	}

	private enum DisplayType {

		ANY("org.bukkit.entity.Display", "display"),
		BLOCK("org.bukkit.entity.BlockDisplay", "block display"),
		ITEM("org.bukkit.entity.ItemDisplay", "item display"),
		TEXT("org.bukkit.entity.TextDisplay", "text display");

		@Nullable
		private Class<? extends Display> displaySubClass;
		private final String codeName;
		
		@SuppressWarnings("unchecked")
		DisplayType(String className, String codeName) {
			try {
				this.displaySubClass = (Class<? extends Display>) Class.forName(className);
			} catch (ClassNotFoundException ignored) {}
			this.codeName = codeName;
		}

		@Override
		public String toString() {
			return codeName;
		}

		private static final String[] codeNames;
		static {
			List<String> codeNamesList = new ArrayList<>();
			for (DisplayType type : values()) {
				if (type.displaySubClass != null)
					codeNamesList.add(type.codeName);
			}
			codeNames = codeNamesList.toArray(new String[0]);
		}
	}

	private DisplayType type = DisplayType.ANY;

	@Nullable
	private BlockData blockData;

	@Nullable
	private ItemStack item;

	@Nullable
	private String text;

	public DisplayData() {}

	public DisplayData(DisplayType type) {
		this.type = type;
		this.matchedPattern = type.ordinal();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		type = DisplayType.values()[matchedPattern];
		// default to 0, use 1 for alternate pattern: %x% display instead of display of %x%
		int exprIndex = parseResult.hasTag("alt") ? 1 : 0;
		if (exprs.length == 0 || exprs[exprIndex] == null)
			return true;

		if (type == DisplayType.BLOCK) {
			Object object = ((Literal<Object>) exprs[exprIndex]).getSingle();
			if (object instanceof ItemType itemType) {
				if (!itemType.hasBlock()) {
					Skript.error("A block display must be of a block item. " + Classes.toString(itemType.getMaterial()) + " is not a block. If you want to display an item, use an 'item display'.");
					return false;
				}
				blockData = Bukkit.createBlockData(itemType.getBlockMaterial());
			} else {
				blockData = (BlockData) object;
			}
		} else if (type == DisplayType.ITEM) {
			ItemType itemType = ((Literal<ItemType>) exprs[exprIndex]).getSingle();
			if (!itemType.hasItem()) {
				Skript.error("An item display must be of a valid item. " + Classes.toString(itemType.getMaterial()) + " is not a valid item. If you want to display a block, use a 'block display'.");
				return false;
			}
			item = itemType.getRandom();
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Display> displayClass, @Nullable Display entity) {
		DisplayType[] types = DisplayType.values();
		for (int i = types.length - 1; i >= 0; i--) {
			Class<?> display = types[i].displaySubClass;
			if (display == null)
				continue;
			//noinspection ConstantConditions
			if (entity == null ? displayClass.isAssignableFrom(display) : display.isInstance(entity)) {
				type = types[i];
				if (entity != null) {
					switch (type) {
						case BLOCK -> blockData = ((BlockDisplay) entity).getBlock();
						case ITEM -> item = ((ItemDisplay) entity).getItemStack();
						case TEXT -> text = ((TextDisplay) entity).getText();
					}
				}
				return true;
			}
		}
		assert false;
		return false;
	}

	@Override
	public void set(Display entity) {
		switch (type) {
			case BLOCK -> {
				if (blockData != null && entity instanceof BlockDisplay blockDisplay)
					blockDisplay.setBlock(blockData);
			}
			case ITEM -> {
				if (item != null && entity instanceof ItemDisplay itemDisplay)
					itemDisplay.setItemStack(item);
			}
			case TEXT -> {
				if (text != null && entity instanceof TextDisplay textDisplay)
					textDisplay.setText(text);
			}
		}
	}

	@Override
	public boolean match(Display entity) {
		switch (type) {
			case BLOCK -> {
				if (!(entity instanceof BlockDisplay blockDisplay))
					return false;
				if (blockData != null && !blockDisplay.getBlock().equals(blockData))
					return false;
			}
			case ITEM -> {
				if (!(entity instanceof ItemDisplay itemDisplay))
					return false;
				if (item != null && !itemDisplay.getItemStack().isSimilar(item))
					return false;
			}
			case TEXT -> {
				if (!(entity instanceof TextDisplay textDisplay))
					return false;
				if (text == null) // all text displays should match a blank one.
					return true;
				String displayText = textDisplay.getText();
				if (displayText == null)
					return false;
				return displayText.equals(text);
			}
		}
		return type.displaySubClass != null && type.displaySubClass.isInstance(entity);
	}

	@Override
	public Class<? extends Display> getType() {
		return type.displaySubClass != null ? type.displaySubClass : Display.class;
	}

	@Override
	protected int hashCode_i() {
		return type.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (obj instanceof DisplayData other)
			return type == other.type;
		return false;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (entityData instanceof DisplayData displayData)
			return type == DisplayType.ANY || displayData.type == type;
		return Display.class.isAssignableFrom(entityData.getType());
	}

	@Override
	public EntityData<?> getSuperType() {
		return new DisplayData(DisplayType.ANY);
	}

}
