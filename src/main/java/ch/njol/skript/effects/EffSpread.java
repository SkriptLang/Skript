package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Name("Spread Across")
@Description("A general effect to spread a list of objects across a number of settable expressions")
@Examples({
	"spread 1, 2, 3, 4 and 5 across {_one}, {_two}, {_three and four::*} and {_five}",
	"spread (shuffled all players) across {_winner} and {_losers::*}",
})
@Since("INSERT VERSION")
public class EffSpread extends Effect {

	static {
		Skript.registerEffect(EffSpread.class, "spread %objects% across %objects%");
	}

	private Expression<?> objectsToSpread;
	private ExpressionList<?> spreadTarget;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objectsToSpread = LiteralUtils.defendExpression(expressions[0]);
		if (objectsToSpread.isSingle()) {
			Skript.error("You must provide more than one object to spread");
			return false;
		}
		Expression<?> potentialSpreadTarget = LiteralUtils.defendExpression(expressions[1]);
		if (!(potentialSpreadTarget instanceof ExpressionList<?>)) {
			Skript.error("The spread target must be a list of settable expressions");
			return false;
		}
		spreadTarget = (ExpressionList<?>) potentialSpreadTarget;
		if (!spreadTarget.getAnd()) {
			Skript.error("The spread target must be an 'and' list, not an 'or' list");
			return false;
		}
		ClassInfoReference spreadType = new ClassInfoReference(objectsToSpread.getReturnType(), Kleenean.FALSE);
		if (!listChildrenCanBeSet(spreadTarget, spreadType)) {
			Skript.error("All expressions in the spread target list must be settable");
			return false;
		}
		return LiteralUtils.canInitSafely(objectsToSpread, spreadTarget);
	}

	@Override
	protected void execute(Event event) {
		ClassInfoReference multipleSpreadType = new ClassInfoReference(objectsToSpread.getReturnType(), Kleenean.TRUE);
		Object[] objectsToSpread = this.objectsToSpread.getArray(event);
		Expression<?>[] spreadTargets = spreadTarget.getExpressions();
		int finalObjectIndex = objectsToSpread.length - 1;
		int objectIndex = 0;
		for (int targetIndex = 0; targetIndex < spreadTargets.length; targetIndex++) {
			if (objectIndex > finalObjectIndex)
				return;
			Expression<?> spreadTarget = spreadTargets[targetIndex];
			// if this expression can't be set to multiple of the spreadType
			if (!ChangerUtils.acceptsChange(spreadTarget, ChangeMode.SET, multipleSpreadType)) {
				spreadTarget.change(event, new Object[] { objectsToSpread[objectIndex] }, ChangeMode.SET);
				objectIndex++;
			} else {
				int remainingElements = computeRemainingElements(spreadTargets, targetIndex);
				// if there's fewer objects available than remaining, we don't want to be greedy
				if ((objectIndex + remainingElements) > finalObjectIndex) {
					spreadTarget.change(event, new Object[] { objectsToSpread[objectIndex] }, ChangeMode.SET);
					objectIndex++;
				} else {
					// otherwise, take as much as we can leaving only enough for the remaining spread targets
					Object[] objectsSlice = Arrays.copyOfRange(objectsToSpread, objectIndex, objectsToSpread.length - remainingElements);
					objectIndex += objectsSlice.length;
					spreadTarget.change(event, objectsSlice, ChangeMode.SET);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "spread " + objectsToSpread.toString(event, debug) + " across " + spreadTarget.toString(event, debug);
	}

	private boolean listChildrenCanBeSet(ExpressionList<?> list, ClassInfoReference reference) {
		for (Expression<?> child : list.getExpressions()) {
			if (!ChangerUtils.acceptsChange(child, ChangeMode.SET, reference)) {
				return false;
			}
		}
		return true;
	}

	private int computeRemainingElements(Expression<?>[] spreadTargets, int currentTargetIndex) {
		return spreadTargets.length - currentTargetIndex - 1;
	}

}
