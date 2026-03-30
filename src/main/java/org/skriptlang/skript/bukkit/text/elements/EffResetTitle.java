package org.skriptlang.skript.bukkit.text.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import net.kyori.adventure.audience.Audience;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Title - Clear/Reset")
@Description({
	"Clears or resets the title of an audience to the default values.",
	"While both actions remove the title being displayed, <code>reset</code> will also reset the title timings."
})
@Example("reset the titles of all players")
@Example("clear the title")
@Since("2.3, INSERT VERSION (clearing the title)")
public class EffResetTitle extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffResetTitle.class)
			.supplier(EffResetTitle::new)
			.addPatterns("(clear|delete|:reset) [the] title[s] [of %audiences%]",
				"(clear|delete|:reset) [the] %audiences%'[s] title[s]")
			.build());
	}

	private Expression<Audience> audiences;
	private boolean reset;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		audiences = (Expression<Audience>) exprs[0];
		reset = parseResult.hasTag("reset");
		return true;
	}

	@Override
	protected void execute(Event event) {
		Audience audience = Audience.audience(this.audiences.getArray(event));
		if (reset) {
			audience.resetTitle();
		} else {
			audience.clearTitle();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (reset) {
			builder.append("reset");
		} else {
			builder.append("clear");
		}
		builder.append("the");
		if (audiences.isSingle()) {
			builder.append("title");
		} else {
			builder.append("titles");
		}
		builder.append("of", audiences);
		return builder.toString();
	}

}
