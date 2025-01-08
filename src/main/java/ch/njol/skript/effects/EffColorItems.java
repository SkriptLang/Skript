package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.Nullable;

@Name("Color Items")
@Description({
	"Colors items in a given <a href='classes.html#color'>color</a>.",
	"You can also use RGB codes if you feel limited with the 16 default colors.",
	"Armor is colorable for all Minecraft versions. With Minecraft 1.11+ you can also color potions and maps. Note that the colors might not look exactly how you'd expect."
})
@Examples({
	"dye player's helmet blue",
	"color the player's tool red"
})
@Since("2.0, 2.2-dev26 (maps and potions)")
public class EffColorItems extends Effect {
	
	private static final boolean MAPS_AND_POTIONS_COLORS = Skript.methodExists(PotionMeta.class, "setColor", org.bukkit.Color.class);
	
	static {
		Skript.registerEffect(EffColorItems.class,
			"(dye|colo[u]r|paint) %itemtypes% %color%",
			"(dye|colo[u]r|paint) %itemtypes% \\(%number%, %number%, %number%\\)");
	}

	private Expression<ItemType> items;
	private Expression<Color> color;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		items = (Expression<ItemType>) exprs[0];
		if (matchedPattern == 0) {
			color = (Expression<Color>) exprs[1];
		} else {
			color = new SimpleExpression<Color>() {
				
				private Expression<Number> red;
				private Expression<Number> green;
				private Expression<Number> blue;
				
				@Override
				public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
					red = (Expression<Number>) exprs[0];
					green = (Expression<Number>) exprs[1];
					blue = (Expression<Number>) exprs[2];
					return true;
				}
				
				@Nullable
				@Override
				protected Color[] get(Event e) {
					Number r = red.getSingle(e),
						g = green.getSingle(e),
						b = blue.getSingle(e);
					
					if (r == null || g == null || b == null)
						return null;
					
					return CollectionUtils.array(ColorRGB.fromRGB(r.intValue(), g.intValue(), b.intValue()));
				}
				
				@Override
				public boolean isSingle() {
					return true;
				}
				
				@Override
				public Class<? extends Color> getReturnType() {
					return ColorRGB.class;
				}
				
				@Override
				public String toString(@Nullable Event e, boolean debug) {
					return "RED: " + red.toString(e, debug) + ", GREEN: " + green.toString(e, debug) + "BLUE: " + blue.toString(e, debug);
				}
			};
			color.init(CollectionUtils.array(exprs[1], exprs[2], exprs[3]), 0, isDelayed, parser);
		}
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		Color color = this.color.getSingle(event);
		if (color == null) {
			return;
		}
		org.bukkit.Color bukkitColor = color.asBukkitColor();
		
		for (ItemType item : this.items.getArray(event)) {
			ItemMeta meta = item.getItemMeta();
			
			if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
				leatherArmorMeta.setColor(bukkitColor);
				item.setItemMeta(leatherArmorMeta);
			} else if (MAPS_AND_POTIONS_COLORS) {
				
				if (meta instanceof MapMeta mapMeta) {
					mapMeta.setColor(bukkitColor);
					item.setItemMeta(mapMeta);
				} else if (meta instanceof PotionMeta potionMeta) {
					potionMeta.setColor(bukkitColor);
					item.setItemMeta(potionMeta);
				}
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "dye " + items.toString(event, debug) + " " + color.toString(event, debug);
	}
}
