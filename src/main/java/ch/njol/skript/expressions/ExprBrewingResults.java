package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Name("Brewing Results")
@Description("The resulting items in an 'on brew complete' event.")
@Examples({
	"on brew complete:",
		"\tset {_results::*} to the brewing results"
})
@Since("INSERT VERSION")
@Events("Brewing Complete")
public class ExprBrewingResults extends SimpleExpression<ItemStack> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprBrewingResults.class, ItemStack.class, ExpressionType.SIMPLE,
			"[the] brewing results");
	}

	private boolean delayed;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		delayed = isDelayed.isTrue();
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(BrewEvent.class);
	}

	@Override
	protected ItemStack @Nullable [] get(Event event) {
		if (!(event instanceof BrewEvent brewEvent))
			return null;
		return brewEvent.getResults().toArray(ItemStack[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (delayed) {
			Skript.error("The 'brewing results' cannot be changed after the 'brewing complete' event has passed.");
			return null;
		}
		return switch (mode) {
			case SET, DELETE, ADD, REMOVE -> CollectionUtils.array(ItemType[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof BrewEvent brewEvent))
			return;
		List<ItemType> itemTypes = new ArrayList<>();
		if (delta != null) {
			for (Object object : delta) {
				if (object instanceof ItemType itemType)
					itemTypes.add(itemType);
			}
		}
		List<ItemStack> results = brewEvent.getResults();
		switch (mode) {
			case SET -> {
				results.clear();
				results.addAll(itemTypes.stream()
					.map(ItemType::getRandom)
					.filter(itemStack -> !Objects.isNull(itemStack))
					.toList());
			}
			case DELETE -> results.clear();
			case ADD -> {
				results.addAll(itemTypes.stream()
					.map(ItemType::getRandom)
					.filter(itemStack -> !Objects.isNull(itemStack))
					.toList());
			}
			case REMOVE -> {
				List<ItemStack> copy = new ArrayList<>(results);
				for (ItemStack itemStack : copy) {
					for (ItemType itemType : itemTypes) {
						if (itemType.isOfType(itemStack))
							results.remove(itemStack);
					}
				}
			}
		}
		if (results.size() > 3)
			warning("A brewing stand can only contain 3 items; Some items will be ignored.");
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the brewing results";
	}

}
