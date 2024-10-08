package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Fishing Hook Apply Lure")
@Description("Returns whether the lure enchantment should be applied to reduce the wait time.")
@Examples({
	"on fish:",
		"\tset apply lure enchantment of fishing hook to true"
})
@Events("Fishing")
@Since("INSERT VERSION")
public class ExprFishingApplyLure extends SimplePropertyExpression<FishHook, Boolean> {

	static {
		registerDefault(ExprFishingApplyLure.class, Boolean.class, "apply lure [enchant[ment]]", "fishinghooks");
	}

	@Override
	public @Nullable Boolean convert(FishHook fishHook) {
		return fishHook.getApplyLure();
	}

	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	protected String getPropertyName() {
		return "lure applied of fishing hook";
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET ? CollectionUtils.array(Boolean.class) : null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta[0] == null || !(delta[0] instanceof Boolean apply))
			return;

		for (FishHook fishHook : getExpr().getArray(event))
			fishHook.setApplyLure(apply);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "apply lure of " + getExpr().toString(event, debug);
	}

}
