package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.MetaSyntaxElement;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.script.Annotation;

/**
 * @author moderocky
 */
@Name("Annotation (Code)")
@Description({
	"A special metadata note visible to the next real line of code.",
	"This does nothing by itself, but may change the behaviour of the following line.",
	"If the annotation does not exist (or the following line does not use it) then it will have no effect.",
	"There is no penalty for using annotations that do not exist."
})
@Examples({
	"on join:",
		"\t@suppress warnings",
		"\t@suppress errors",
		"\tbroadcast \"hello there!\""
})
@Since("INSERT VERSION")
public class EffAnnotate extends Effect implements MetaSyntaxElement {

	static {
		Skript.registerEffect(EffAnnotate.class, "@<.+>");
	}

	private @UnknownNullability Annotation annotation;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		String text = result.regexes.get(0).group().trim();
		if (text.isEmpty()) {
			Skript.error("You must specify a text to annotate.");
			return false;
		}
		this.annotation = Annotation.create(text);
		this.getParser().addAnnotation(annotation);
		Skript.debug("found annotation: " + annotation);
		return true;
	}

	@Override
	protected void execute(Event event) {
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return '@' + annotation.value();
	}

}
