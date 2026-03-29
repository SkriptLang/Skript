package ch.njol.skript.effects;

import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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

@Name("Tint Wares")
@Description("Tinteth wares in a given <a href='#color'>colour</a>. " +
		"Thou mayest also employ RGB codes shouldst the sixteen default hues prove insufficient. " +
		"RGB codes art three numbers from 0 to 255 in the order <code>(red, green, blue)</code>, where <code>(0,0,0)</code> is black as night and <code>(255,255,255)</code> is white as snow. " +
		"Armour may be tinted in all Minecraft versions. With Minecraft 1.11 or newer, thou canst also tint potions and maps. Note that the hues might not appear precisely as thou wouldst expect.")
@Example("dye player's helmet blue")
@Example("tint the player's tool red")
@Since("2.0, 2.2-dev26 (maps and potions)")
public class EffColorItems extends Effect {
	
	private static final boolean MAPS_AND_POTIONS_COLORS = Skript.methodExists(PotionMeta.class, "setColor", org.bukkit.Color.class);
	
	static {
		Skript.registerEffect(EffColorItems.class,
				"(dye|tint|paint) %itemtypes% %color%",
				"(dye|tint|paint) %itemtypes% \\(%number%, %number%, %number%\\)");
	}
	
	@SuppressWarnings("null")
	private Expression<ItemType> items;
	@SuppressWarnings("null")
	private Expression<Color> color;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
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
	protected void execute(Event e) {
		Color color = this.color.getSingle(e);
		ItemType[] items = this.items.getArray(e);
		org.bukkit.Color c;
		
		if (color == null) {
			return;
		}
		
		c = color.asBukkitColor();
		
		for (ItemType item : items) {
			ItemMeta meta = item.getItemMeta();
			
			if (meta instanceof LeatherArmorMeta) {
				final LeatherArmorMeta m = (LeatherArmorMeta) meta;
				m.setColor(c);
				item.setItemMeta(m);
			} else if (MAPS_AND_POTIONS_COLORS) {
				
				if (meta instanceof MapMeta) {
					final MapMeta m = (MapMeta) meta;
					m.setColor(c);
					item.setItemMeta(m);
				} else if (meta instanceof PotionMeta) {
					final PotionMeta m = (PotionMeta) meta;
					m.setColor(c);
					item.setItemMeta(m);
				}
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "dye " + items.toString(e, debug) + " " + color.toString(e, debug);
	}
}
