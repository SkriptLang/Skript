package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Using Experimental Feature")
@Description({
	"Place at the top of a script file to enable an optional experimental feature.",
	"Experimental features may change behavior in Skript and may contain bugs. Use at your own discretion.",
	"A list of the available experimental features can be found in the changelog for your version of Skript."
})
@Examples({
	"using 1.21",
	"using the experiment my-cool-addon-feature",
	"using feature_1, feature_2"
})
@Since("2.9.0, INSERT VERSION (multiple in one)")
public class StructUsing extends Structure {

	public static final Priority PRIORITY = new Priority(15);

	static {
		Skript.registerSimpleStructure(StructUsing.class, "using [[the] experiment[s]] <.+>");
	}

	private Experiment[] experiments;
	private String experimentCodeNames;

	@Override
	public boolean init(Literal<?> @NotNull [] arguments, int pattern, ParseResult result, @Nullable EntryContainer container) {
		this.enableExperiment(result.regexes.get(0).group());
		return true;
	}

	private void enableExperiment(String pattern) {
		String[] names = pattern.split(",");
		List<Experiment> experiments =  new ArrayList<>();
		ParserInstance parser = getParser();
		for (String name : names) {
			String trimmed = name.trim();
			if (trimmed.isEmpty())
				continue;
			Experiment experiment = Skript.experiments().find(trimmed);
			experiments.add(experiment);
			switch (experiment.phase()) {
				case MAINSTREAM ->
					Skript.warning("The experimental feature '" + name + "' is now included by default and is no longer required.");
				case DEPRECATED ->
					Skript.warning("The experimental feature '" + name + "' is deprecated and may be removed in future versions.");
				case UNKNOWN ->
					Skript.warning("The experimental feature '" + name + "' was not found.");
			}
			parser.addExperiment(experiment);
		}
		this.experiments = experiments.toArray(Experiment[]::new);
		experimentCodeNames = StringUtils.join(Arrays.stream(this.experiments).map(Experiment::codeName).toArray(), ", ");
	}

	@Override
	public boolean load() {
		return true;
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "using " + experimentCodeNames;
	}

}
