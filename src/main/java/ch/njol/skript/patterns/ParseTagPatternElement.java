package ch.njol.skript.patterns;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A {@link PatternElement} that applies a parse mark when matched.
 */
public class ParseTagPatternElement extends PatternElement {

	@Nullable
	private String tag;
	private final int mark;

	public ParseTagPatternElement(int mark) {
		this.tag = null;
		this.mark = mark;
	}

	public ParseTagPatternElement(String tag) {
		this.tag = tag;
		int mark = 0;
		try {
			mark = Integer.parseInt(tag);
		} catch (NumberFormatException ignored) { }
		this.mark = mark;
	}

	@Override
	void setNext(@Nullable PatternElement next) {
		if (tag != null && tag.isEmpty()) {
			if (next instanceof LiteralPatternElement) {
				// (:a)
				tag = next.toString().trim();
			} else {
				// Get the inner element from either a group or optional pattern element
				PatternElement inner = null;
				if (next instanceof GroupPatternElement) {
					inner = ((GroupPatternElement) next).getPatternElement();
				} else if (next instanceof OptionalPatternElement) {
					inner = ((OptionalPatternElement) next).getPatternElement();
				}

				if (inner instanceof ChoicePatternElement) {
					// :(a|b) or :[a|b]
					ChoicePatternElement choicePatternElement = (ChoicePatternElement) inner;
					List<PatternElement> patternElements = choicePatternElement.getPatternElements();
					for (int i = 0; i < patternElements.size(); i++) {
						PatternElement patternElement = patternElements.get(i);
						// Prevent a pattern such as :(a|b|) from being turned into (a:a|b:b|:), instead (a:a|b:b|)
						if (patternElement instanceof LiteralPatternElement && !patternElement.toString().isEmpty()) {
							ParseTagPatternElement newTag = new ParseTagPatternElement(patternElement.toString().trim());
							newTag.setNext(patternElement);
							newTag.originalNext = patternElement;
							patternElements.set(i, newTag);
						}
					}
				}
			}
		}
		super.setNext(next);
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		if (tag != null && !tag.isEmpty())
			matchResult.tags.add(tag);
		matchResult.mark ^= mark;
		return matchNext(expr, matchResult);
	}

	@Override
	public String toString() {
		if (tag != null) {
			if (tag.isEmpty())
				return "";
			return tag + ":";
		} else {
			return mark + "¦";
		}
	}

}
