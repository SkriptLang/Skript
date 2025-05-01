package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
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
import java.util.Arrays;
import java.util.List;

@Name("Brewing Results")
@Description("The result items of an 'on brew complete' event.")
@Examples({
	"on brew complete:",
		"\tset {_results::*} to the brewing results"
})
@Since("INSERT VERSION")
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
		return ((BrewEvent) event).getResults().toArray(ItemStack[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (delayed) {
			Skript.error("Cannot change the 'brewing results' after the event has passed.");
			return null;
		}
		return switch (mode) {
			case SET, DELETE, ADD, REMOVE -> CollectionUtils.array(ItemStack[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof BrewEvent brewEvent))
			return;
		List<ItemStack> itemStacks = delta == null ? new ArrayList<>() : Arrays.stream(((ItemStack[]) delta)).toList();
		List<ItemStack> results = brewEvent.getResults();
		switch (mode) {
			case SET -> {
				results.clear();
				results.addAll(itemStacks);
			}
			case DELETE -> results.clear();
			case ADD -> results.addAll(itemStacks);
			case REMOVE -> results.removeAll(itemStacks);
		}
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
