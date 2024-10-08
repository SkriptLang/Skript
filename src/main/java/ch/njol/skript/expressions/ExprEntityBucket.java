package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.util.SimpleExpression;
import org.bukkit.inventory.ItemStack;

@Name("Entity Bucket")
@Description("Gets the bucket that the Entity will be put into such as 'puffer fish bucket'.")
@Examples({
	"on bucket capture entity:",
		"\tif entity bucket is salmon bucket:",
			"\t\tsend \"Congratulations you now have a salmon bucket!\" to player"
})
@Events("Bucket Catch Entity")
@Since("INSERT VERSION")
public class ExprEntityBucket extends SimpleExpression<ItemStack> {

	static {
		if (Skript.classExists("org.bukkit.event.player.PlayerBucketEntityEvent"))
			Skript.registerExpression(ExprEntityBucket.class, ItemStack.class, ExpressionType.SIMPLE, "[the] entity bucket");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerBucketEntityEvent.class)) {
			Skript.error("The 'entity bucket' expression can only be used in the bucket capture entity event");
			return false;
		}
		return true;
	}

	@Override
	protected @Nullable ItemStack[] get(Event event) {
		if (!(event instanceof PlayerBucketEntityEvent))
			return null;
		return new ItemStack[]{((PlayerBucketEntityEvent) event).getEntityBucket()};
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the entity bucket";
	}

}
