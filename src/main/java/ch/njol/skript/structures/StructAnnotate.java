package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.MetaSyntaxElement;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.test.runner.TestMode;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Annotation;
import org.skriptlang.skript.lang.structure.Structure;

@Name("Annotation (Structure)")
@Description({
	"A special metadata note visible to the next structure.",
	"This does nothing by itself, but may change the behaviour of the following feature.",
	"If the annotation does not exist (or the following feature does not use it) then it will have no effect.",
	"There is no penalty for using annotations that do not exist."
})
@Examples({
	"@my cool annotation",
	"command /test:",
	"",
	"@my other cool annotation",
	"on load:"
})
@Since("INSERT VERSION")
public class StructAnnotate extends Structure implements MetaSyntaxElement {

	static {
		Skript.registerSimpleStructure(StructAnnotate.class, "@<.+>");
	}

	private @UnknownNullability Annotation annotation;

	@Override
	public boolean init(Literal<?>[] arguments, int pattern, ParseResult result,
						@Nullable EntryContainer container) {
		String text = result.regexes.get(0).group().trim();
		if (text.isEmpty()) {
			Skript.error("You must specify a text to annotate.");
			return false;
		}
		this.annotation = Annotation.create(text);
		this.getParser().addAnnotation(annotation);
		Skript.debug("found root-level annotation: " + annotation);
		return true;
	}

	@Override
	public boolean load() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return '@' + annotation.value();
	}

}
