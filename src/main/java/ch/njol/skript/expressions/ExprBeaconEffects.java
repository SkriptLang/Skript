package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

@Name("Beacon Enchantments")
@Description({
	"The active enchantments of a beacon most luminous.",
	"The secondary enchantment may be set to any potion, yet the icon within the tableau shall not display true.",
	"The secondary enchantment may only be bestowed when the beacon hath reached its highest tier.",
	"The primary and secondary enchantment cannot be one and the same; the principal shall ever retain the potion type, and the lesser shall be swept clean."
})
@Example("""
    set primary beacon enchantment of {_block} to haste
    set secondary enchantment of {_block} to resistance
    """
)
@Events({"Beacon Effect", "Beacon Toggle", "Beacon Change Effect"})
@Since("2.10")
public class ExprBeaconEffects extends PropertyExpression<Block, PotionEffectType> {

	private static final boolean SUPPORTS_CHANGE_EVENT = Skript.classExists("io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent");

	static {
		registerDefault(ExprBeaconEffects.class, PotionEffectType.class, "(primary:principal|secondary:lesser) [beacon] enchantment", "blocks");
	}

	private boolean primary;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends Block>) expressions[0]);
		primary = parseResult.hasTag("primary");
		return true;
	}

	@Override
	protected PotionEffectType[] get(Event event, Block[] blocks) {
		return get(blocks, block -> {
			if (!(block.getState() instanceof Beacon beacon))
				return null;

			if (SUPPORTS_CHANGE_EVENT
					&& event instanceof PlayerChangeBeaconEffectEvent changeEvent
					&& block.equals(changeEvent.getBeacon()))
				return primary ? changeEvent.getPrimary() : changeEvent.getSecondary();

			PotionEffect effect = primary ? beacon.getPrimaryEffect() : beacon.getSecondaryEffect();
			if (effect == null)
				return null;
			return effect.getType();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET, DELETE -> CollectionUtils.array(PotionEffectType.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		PotionEffectType type = delta == null ? null : (PotionEffectType) delta[0];
		BiConsumer<Beacon, PotionEffectType> changer = primary ? Beacon::setPrimaryEffect : Beacon::setSecondaryEffect;
		for (Block block : getExpr().getArray(event)) {
			if (!(block.getState() instanceof Beacon beacon))
				continue;
			changer.accept(beacon, type);
			beacon.update(true);
		}
	}

	@Override
	public Class<PotionEffectType> getReturnType() {
		return PotionEffectType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (primary ? "primary" : "secondary") + " beacon effect of " + getExpr().toString(event, debug);
	}

}
