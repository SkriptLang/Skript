package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.Locale;

@Name("Has Custom Model Data")
@Description("Check if an item has a custom model data tag")
@Example("player's tool has custom model data")
@Example("""
	if player's tool has custom model data flags:
		loop custom model data flags of player's tool:
			send "Flag %loop-index%: %loop-value%"
	""")
@Example("set {_coloured} to whether player's tool has model data colours")
@Since("2.5, 2.12 (expanded data types)")
@RequiredPlugins("Minecraft 1.21.4+ (floats/flags/strings/colours)")
public class CondHasCustomModelData extends PropertyCondition<ItemType> {

	// 1.21.5+
	private static final boolean HAS_HAS_COMPONENT = Skript.methodExists(ItemMeta.class, "hasCustomModelDataComponent");

	static {
		// 1.21.4+
		if (Skript.methodExists(ItemMeta.class, "getCustomModelDataComponent")) {
			// new style
			register(CondHasCustomModelData.class, PropertyType.HAVE, "[custom] model data [1:floats|2:flags|3:strings|4:colo[u]rs]", "itemtypes");
		} else {
			// old style
			register(CondHasCustomModelData.class, PropertyType.HAVE, "[custom] model data", "itemtypes");
		}
	}

	private enum CMDType {
		ANY,
		FLOATS,
		FLAGS,
		STRINGS,
		COLORS
	}

	private CMDType dataType;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		dataType = CMDType.values()[parseResult.mark];
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public boolean check(ItemType item) {
		ItemMeta meta = item.getItemMeta();
		if (dataType == CMDType.ANY)
			return HAS_HAS_COMPONENT ? meta.hasCustomModelDataComponent() : meta.hasCustomModelData();
		CustomModelDataComponent component = meta.getCustomModelDataComponent();
		return switch (dataType) {
			case FLOATS -> !component.getFloats().isEmpty();
			case FLAGS -> !component.getFlags().isEmpty();
			case STRINGS -> !component.getStrings().isEmpty();
			case COLORS -> !component.getColors().isEmpty();
			case ANY -> throw new IllegalStateException("Wrong path for CMDType.ANY.");
		};
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	protected String getPropertyName() {
		return "custom model data" + (dataType != CMDType.ANY ? " " + dataType.name().toLowerCase(Locale.ENGLISH) : "");
	}
	
}

