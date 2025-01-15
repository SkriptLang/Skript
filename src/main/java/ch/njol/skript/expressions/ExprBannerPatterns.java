package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.DyeColor;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Name("Banner Patterns")
@Description({
	"Gets or sets the banner patterns of a banner.",
	"In order to set a specific position of a banner, there needs to be that many patterns already on the banner.",
	"This expression will add filler patterns to the banner to allow the specified position to be set.",
	"For Example, setting the 3rd banner pattern of a banner that has no patterns on it, will internally add 3 base patterns, "
	+ "allowing the 3rd banner pattern to be set."
})
@Examples({
	"broadcast banner patterns of {_banneritem}",
	"broadcast 1st banner pattern of block at location(0,0,0)",
	"clear banner patterns of {_banneritem}"
})
@Since("2.10")
public class ExprBannerPatterns extends PropertyExpression<Object, Pattern> {

	static {
		Skript.registerExpression(ExprBannerPatterns.class, Pattern.class, ExpressionType.PROPERTY,
			"[all [[of] the]|the] banner pattern[s] of %itemstacks/itemtypes/slots/blocks%",
			"%itemstacks/itemtypes/slots/blocks%'[s] banner pattern[s]",
			"[the] %integer%[st|nd|rd|th] [banner] pattern of %itemstacks/itemtypes/slots/blocks%",
			"%itemstacks/itemtypes/slots/blocks%'[s] %integer%[st|nd|rd|th] [banner] pattern"
		);
	}

	private Expression<?> objects;
	private Expression<Integer> patternNumber = null;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern <= 1) {
			objects = exprs[0];
		} else if (matchedPattern == 2) {
			//noinspection unchecked
			patternNumber = (Expression<Integer>) exprs[0];
			objects = exprs[1];
		} else {
			//noinspection unchecked
			patternNumber = (Expression<Integer>) exprs[1];
			objects = exprs[0];
		}
		setExpr(objects);
		return true;
	}

	@Override
	protected Pattern @Nullable [] get(Event event, Object[] source) {
		List<Pattern> patterns = new ArrayList<>();
		Integer placement = patternNumber != null ? patternNumber.getSingle(event) : null;
		for (Object object : objects.getArray(event)) {
			if (object instanceof Block block) {
				if (!(block.getState() instanceof Banner banner))
					continue;
				if (placement != null && banner.numberOfPatterns() >= placement) {
					patterns.add(banner.getPattern(placement - 1));
				} else if (placement == null) {
					patterns.addAll(banner.getPatterns());
				}
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack == null || !(itemStack.getItemMeta() instanceof BannerMeta bannerMeta))
					continue;
				if (placement != null && bannerMeta.numberOfPatterns() >= placement) {
					patterns.add(bannerMeta.getPattern(placement - 1));
				} else if (placement == null) {
					patterns.addAll(bannerMeta.getPatterns());
				}
			}
		}
		return patterns.toArray(new Pattern[0]);
	}

	/**
	 * Gets the appropriate {@link Consumer<BannerMeta>} to be used within {@link #change(Event, Object[], ChangeMode)}.
	 * @param mode The {@link ChangeMode} to get the consumer matching the behavior
	 * @param placement The specific pattern to set {@code pattern}
	 * @param pattern The pattern to be applied
	 * @return {@link Consumer<BannerMeta>} to be applied to objects within {@link #change(Event, Object[], ChangeMode)}
	 */
	private Consumer<BannerMeta> getPlacementMetaChanger(ChangeMode mode, int placement, @Nullable Pattern pattern) {
		return switch (mode) {
			case SET -> bannerMeta -> {
				if (bannerMeta.numberOfPatterns() < placement) {
					int toAdd = placement - bannerMeta.numberOfPatterns();
					for (int i = 0; i < toAdd; i++) {
						bannerMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.BASE));
					}
				}
				bannerMeta.setPattern(placement - 1, pattern);
			};
			case DELETE -> bannerMeta -> {
				if (bannerMeta.numberOfPatterns() >= placement)
					bannerMeta.removePattern(placement - 1);
			};
			default -> bannerMeta -> {};
		};
	}

	/**
	 * Gets the appropriate {@link Consumer<Banner>} to be used within {@link #change(Event, Object[], ChangeMode)}.
	 * @param mode The {@link ChangeMode} to get the consumer matching the behavior
	 * @param placement The specific pattern to set {@code pattern}
	 * @param pattern The pattern to be applied
	 * @return {@link Consumer<Banner>} to be applied to objects within {@link #change(Event, Object[], ChangeMode)}
	 */
	private Consumer<Banner> getPlacementBlockChanger(ChangeMode mode, int placement, @Nullable Pattern pattern) {
		return switch (mode) {
			case SET -> banner -> {
				if (banner.numberOfPatterns() < placement) {
					int toAdd = placement - banner.numberOfPatterns();
					for (int i = 0; i < toAdd; i++) {
						banner.addPattern(new Pattern(DyeColor.GRAY, PatternType.BASE));
					}
				}
				banner.setPattern(placement - 1, pattern);
			};
			case DELETE -> banner -> {
				if (banner.numberOfPatterns() >= placement)
					banner.removePattern(placement - 1);
			};
			default -> banner -> {};
		};
	}

	/**
	 * Gets the appropriate {@link Consumer<BannerMeta>} to be used within {@link #change(Event, Object[], ChangeMode)}.
	 * @param mode The {@link ChangeMode} to get the consumer matching the behavior
	 * @param patterns Patterns to be added, removed, or set to corresponding with the {@code mode}
	 * @return {@link Consumer<BannerMeta>} to be applied to objects within {@link #change(Event, Object[], ChangeMode)}
	 */
	private Consumer<BannerMeta> getAllMetaChanger(ChangeMode mode, List<Pattern> patterns) {
		return switch (mode) {
			case SET -> bannerMeta -> {
				bannerMeta.setPatterns(patterns);
			};
			case DELETE -> bannerMeta -> {
				bannerMeta.setPatterns(new ArrayList<>());
			};
			case ADD -> bannerMeta -> {
				patterns.forEach(bannerMeta::addPattern);
			};
			case REMOVE -> bannerMeta -> {
				List<Pattern> current = bannerMeta.getPatterns();
				current.removeAll(patterns);
				bannerMeta.setPatterns(current);
			};
			default -> bannerMeta -> {};
		};
	}

	/**
	 * Gets the appropriate {@link Consumer<Banner>} to be used within {@link #change(Event, Object[], ChangeMode)}.
	 * @param mode The {@link ChangeMode} to get the consumer matching the behavior
	 * @param patterns Patterns to be added, removed, or set to corresponding with the {@code mode}
	 * @return {@link Consumer<Banner>} to be applied to objects within {@link #change(Event, Object[], ChangeMode)}
	 */
	private Consumer<Banner> getAllBlockChanger(ChangeMode mode, List<Pattern> patterns) {
		return switch (mode) {
			case SET -> banner -> {
				banner.setPatterns(patterns);
			};
			case DELETE -> banner -> {
				banner.setPatterns(new ArrayList<>());
			};
			case ADD -> banner -> {
				patterns.forEach(banner::addPattern);
			};
			case REMOVE -> banner -> {
				List<Pattern> current = banner.getPatterns();
				current.removeAll(patterns);
				banner.setPatterns(current);
			};
			default -> banner -> {};
		};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE -> (patternNumber != null) ? CollectionUtils.array(Pattern.class) : CollectionUtils.array(Pattern[].class);
			case REMOVE, ADD -> (patternNumber != null) ? null : CollectionUtils.array(Pattern[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Pattern[] patterns = (Pattern[]) delta;
		int placement = patternNumber == null ? 0 : patternNumber.getSingle(event);
		List<Pattern> patternList = patterns != null ? Arrays.stream(patterns).toList() : new ArrayList<>();

		Consumer<BannerMeta> metaChanger = null;
		Consumer<Banner> blockChanger = null;

		if (placement >= 1) {
			Pattern pattern = patternList.size() == 1 ? patternList.get(0) : null;
			metaChanger = getPlacementMetaChanger(mode, placement, pattern);
			blockChanger = getPlacementBlockChanger(mode, placement, pattern);
		} else {
			metaChanger = getAllMetaChanger(mode, patternList);
			blockChanger = getAllBlockChanger(mode, patternList);
		}

		for (Object object : objects.getArray(event)) {
			if (object instanceof Block block && block.getState() instanceof Banner banner) {
				blockChanger.accept(banner);
				banner.update(true, false);
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack == null || !(itemStack.getItemMeta() instanceof BannerMeta bannerMeta))
					continue;
				metaChanger.accept(bannerMeta);
				itemStack.setItemMeta(bannerMeta);
				if (object instanceof Slot slot) {
					slot.setItem(itemStack);
				} else if (object instanceof ItemType itemType) {
					itemType.setItemMeta(bannerMeta);
				} else if (object instanceof ItemStack itemStack1) {
					itemStack1.setItemMeta(bannerMeta);
				}
			}
		}

	}

	@Override
	public boolean isSingle() {
		return patternNumber != null && getExpr().isSingle();
	}

	@Override
	public Class<Pattern> getReturnType() {
		return Pattern.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (patternNumber != null) {
			builder.append("banner pattern", patternNumber);
		} else {
			builder.append("banner patterns");
		}
		builder.append("of", objects);
		return builder.toString();
	}

}
