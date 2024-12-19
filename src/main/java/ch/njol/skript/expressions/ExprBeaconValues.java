package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import io.papermc.paper.event.block.BeaconActivatedEvent;
import io.papermc.paper.event.block.BeaconDeactivatedEvent;
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Name("Beacon Effects")
@Description({
	"The active effects of a beacon.",
	"The secondary effect can be set to anything, but the icon in the GUI will not display correctly.",
	"The secondary effect can only be set when the beacon is at max tier.",
	"The primary and secondary effect can not be the same, primary will always retain the potion type and secondary will be cleared.",
	"You can only change the range on Paper."
})
@Examples({
	"broadcast tier of {_block}",
	"set primary beacon effect of {_block} to haste",
	"add 1 to range of {_block}"
})
@RequiredPlugins("Paper (range)")
@Events({"Beacon Effect", "Beacon Toggle", "Beacon Change Effect"})
@Since("INSERT VERSION")
public class ExprBeaconValues extends PropertyExpression<Block, Object> {

	enum BeaconValues {
		PRIMARY("primary [beacon] effect"),
		SECONDARY("secondary [beacon] effect"),
		RANGE("[beacon] range"),
		TIER("[beacon] tier");

		private final String pattern;

		BeaconValues(String pattern) {
			this.pattern = pattern;
		}
	}

	private static final boolean SUPPORTS_CHANGE_EVENT = Skript.classExists("io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent");
	private static final boolean SUPPORTS_RANGE = Skript.methodExists(Beacon.class, "getEffectRange");
	private static BeaconValues[] beaconValues = BeaconValues.values();

	static {
		String patternEnding = " of %blocks%";
		if (SUPPORTS_CHANGE_EVENT)
			patternEnding = " [of %blocks%]";
		int size = beaconValues.length;
		String[] patterns = new String[size * 2];
		for (BeaconValues value : beaconValues) {
			patterns[2 * value.ordinal()] = "%blocks%['s] " + value.pattern;
			patterns[2 * value.ordinal() + 1] = "[the] " + value.pattern + patternEnding;
		}

		Skript.registerExpression(ExprBeaconValues.class, Object.class, ExpressionType.PROPERTY, patterns);
	}

	private BeaconValues valueType;
	private boolean isEffect;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		valueType = beaconValues[(int) Math2.floor(matchedPattern/2)];
		if (valueType == BeaconValues.RANGE && !SUPPORTS_RANGE) {
			Skript.error(valueType.pattern + " can only be used on Paper.");
			return false;
		}
		if (exprs[0] != null) {
			setExpr((Expression<Block>) exprs[0]);
		} else {
			if (!getParser().isCurrentEvent(PlayerChangeBeaconEffectEvent.class, BeaconEffectEvent.class, BeaconActivatedEvent.class, BeaconDeactivatedEvent.class)) {
				Skript.error("There is no beacon in a " + getParser().getCurrentEventName() + " event.");
				return false;
			}
			setExpr(new EventValueExpression<>(Block.class));
		}
		if (valueType == BeaconValues.PRIMARY || valueType == BeaconValues.SECONDARY)
			isEffect = true;

		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event, Block[] source) {
		return get(source, block -> {
			Beacon beacon  = (Beacon) block.getState();
			return switch (valueType) {
				case PRIMARY -> {
					if (SUPPORTS_CHANGE_EVENT && event instanceof PlayerChangeBeaconEffectEvent changeEvent) {
						yield changeEvent.getPrimary();
					} else if (beacon.getPrimaryEffect() != null) {
						yield beacon.getPrimaryEffect().getType();
					}
					yield null;
				}
				case SECONDARY-> {
					if (SUPPORTS_CHANGE_EVENT && event instanceof PlayerChangeBeaconEffectEvent changeEvent) {
						yield changeEvent.getSecondary();
					} else if (beacon.getSecondaryEffect() != null) {
						yield beacon.getSecondaryEffect().getType();
					}
					yield null;
				}
				case RANGE -> beacon.getEffectRange();
				case TIER -> beacon.getTier();
			};
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode != ChangeMode.REMOVE_ALL && valueType != BeaconValues.TIER) {
			if (!isEffect || (isEffect && (mode != ChangeMode.ADD && mode != ChangeMode.REMOVE))) {
				return CollectionUtils.array(Object.class);
			}
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		PotionEffectType providedEffect = null;
		double providedRange = 0;
		if (delta != null && delta[0] != null) {
			if (isEffect && delta[0] instanceof PotionEffectType potionEffectType) {
				providedEffect = potionEffectType;
			} else if (valueType == BeaconValues.RANGE) {
				providedRange = (double) ((long) delta[0]);
			}
		}
		switch (valueType) {
			case RANGE -> changeRange(event, providedRange, mode);
			case PRIMARY -> changePrimary(event, providedEffect, mode);
			case SECONDARY -> changeSecondary(event, providedEffect, mode);
		}
	}

	private void changeBeacons(Event event, Consumer<Beacon> changer) {
		for (Block block : getExpr().getArray(event)) {
			Beacon beacon = (Beacon) block.getState();
			changer.accept(beacon);
			beacon.update(true);
		}
	}

	private void changeRange(Event event, double providedRange, ChangeMode mode) {
		switch (mode) {
			case ADD -> changeBeacons(event, beacon -> beacon.setEffectRange(Math2.fit(0, beacon.getEffectRange() + providedRange, Integer.MAX_VALUE)));
			case REMOVE -> changeBeacons(event, beacon -> beacon.setEffectRange(Math2.fit(0, beacon.getEffectRange() - providedRange, Integer.MAX_VALUE)));
			case SET -> changeBeacons(event, beacon -> beacon.setEffectRange(Math2.fit(0, providedRange, Integer.MAX_VALUE)));
			case DELETE, RESET -> changeBeacons(event, Beacon::resetEffectRange);
		}
	}

	private void changePrimary(Event event, PotionEffectType providedEffect, ChangeMode mode) {
		switch (mode) {
			case SET -> changeBeacons(event, beacon -> beacon.setPrimaryEffect(providedEffect));
			case DELETE, RESET -> changeBeacons(event, beacon -> beacon.setPrimaryEffect(null));
		}
	}

	private void changeSecondary(Event event, PotionEffectType providedEffect, ChangeMode mode) {
		switch (mode) {
			case SET -> changeBeacons(event, beacon -> beacon.setSecondaryEffect(providedEffect));
			case DELETE, RESET -> changeBeacons(event, beacon -> beacon.setSecondaryEffect(null));
		}
	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<Object> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (valueType) {
			case PRIMARY -> "primary beacon effect";
			case SECONDARY -> "secondary beacon effect";
			case RANGE -> "beacon range";
			case TIER -> "beacon tier";
		} + " of " + getExpr().toString(event, debug);
	}

}
