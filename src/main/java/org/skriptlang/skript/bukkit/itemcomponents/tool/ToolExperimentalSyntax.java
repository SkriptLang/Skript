package org.skriptlang.skript.bukkit.itemcomponents.tool;

import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.SimpleExperimentalSyntax;

/**
 * Typed {@link SimpleExperimentalSyntax} for {@link SyntaxElement}s that require {@link Feature#TOOL_COMPONENTS}.
 */
public interface ToolExperimentalSyntax extends SimpleExperimentalSyntax {

	ExperimentData EXPERIMENT_DATA = ExperimentData.createSingularData(Feature.TOOL_COMPONENTS);

	@Override
	default ExperimentData getExperimentData() {
		return EXPERIMENT_DATA;
	}

}
