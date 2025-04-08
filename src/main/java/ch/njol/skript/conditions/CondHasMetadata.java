package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.metadata.Metadatable;
import org.jetbrains.annotations.Nullable;

@Name("Has Metadata")
@Description("Checks whether a metadata holder has a metadata tag.")
@Examples("if player has metadata value \"healer\":")
@Since("2.2-dev36")
@SuppressWarnings("null")
public class CondHasMetadata extends Condition {

	static {
		Skript.registerCondition(CondHasMetadata.class,
				"%metadataholders% (has|have) metadata [(value|tag)[s]] %strings%",
				"%metadataholders% (doesn't|does not|do not|don't) have metadata [(value|tag)[s]] %strings%"
		);
	}
	
	private Expression<Metadatable> holders;
	private Expression<String> values;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		holders = (Expression<Metadatable>) exprs[0];
		values = (Expression<String>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		return holders.check(e,
				holder -> values.check(e,
						holder::hasMetadata
				), isNegated());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.HAVE, e, debug, holders,
				"metadata " + (values.isSingle() ? "value " : "values ") + values.toString(e, debug));
	}
	
}
