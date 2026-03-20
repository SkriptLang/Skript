package org.skriptlang.skript.lang.parsing.sites;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ExprInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.util.Kleenean;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.lang.parsing.constraints.Constraints;
import org.skriptlang.skript.lang.parsing.constraints.LiteralConstraint;
import org.skriptlang.skript.lang.parsing.constraints.ReturnTypeConstraint;
import org.skriptlang.skript.lang.parsing.constraints.ReturnTypes;

import java.util.List;
import java.util.Set;

public class ExpressionSite extends AbstractParsingSite {

	private ReturnTypes returnTypes;
	private boolean optional;
	private boolean allowLiterals;
	private boolean allowNonLiterals;
	private boolean allowSimplifiedLiterals;
	private TimeState timeState;

	public ExpressionSite() {
		this.returnTypes = new ReturnTypes();
		this.optional = false;
		this.allowLiterals = true;
		this.allowNonLiterals = true;
		this.allowSimplifiedLiterals = true;
		this.timeState = TimeState.PRESENT;
	}

	public ExpressionSite(ExprInfo info) {
		this.returnTypes = new ReturnTypes();
		for (int i = 0; i < info.classes.length; i++) {
			this.returnTypes.add(new ClassInfoReference(info.classes[i], Kleenean.get(info.isPlural[i])));
		}
		this.allowLiterals = (info.flagMask & SkriptParser.PARSE_LITERALS) != 0;
		this.allowNonLiterals = (info.flagMask & SkriptParser.PARSE_EXPRESSIONS) != 0;
		this.optional = info.isOptional;
		this.timeState = TimeState.fromInt(info.time);
		this.allowSimplifiedLiterals = true;
	}

	public ExpressionSite(Class<?>... returnTypes) {
		this();
		returnTypes(returnTypes);
	}

	@Override
	protected Constraints buildConstraints() {
		return Constraints.of(
			new ReturnTypeConstraint(returnTypes),
			new LiteralConstraint(allowLiterals, allowNonLiterals, allowSimplifiedLiterals));
	}

	public ExpressionSite returnTypes(List<ClassInfoReference> returnTypes) {
		this.returnTypes = new ReturnTypes();
		this.returnTypes.add(returnTypes.toArray(ClassInfoReference[]::new));
		invalidateConstraints();
		return this;
	}

	public ExpressionSite returnTypes(ClassInfoReference... returnTypes) {
		this.returnTypes = new ReturnTypes();
		this.returnTypes.add(returnTypes);
		invalidateConstraints();
		return this;
	}

	public ExpressionSite returnTypes(@NotNull ClassInfo<?>[] returnType, boolean[] isPlural) {
		Preconditions.checkNotNull(returnType, "returnType is null");
		Preconditions.checkNotNull(isPlural, "isPlural is null");
		Preconditions.checkArgument(returnType.length == isPlural.length, "returnType and isPlural must have the same length");
		Preconditions.checkArgument(returnType.length > 0, "returnType must have at least one element");
		this.returnTypes = new ReturnTypes();
		for (int i = 0; i < returnType.length; i++) {
			Preconditions.checkNotNull(returnType[i], "returnType[%s] is null", i);
			this.returnTypes.add(new ClassInfoReference(returnType[i], Kleenean.get(isPlural[i])));
		}
		invalidateConstraints();
		return this;
	}

	public ExpressionSite returnTypes(ClassInfo<?>... returnTypes) {
		Preconditions.checkNotNull(returnTypes, "returnType is null");
		Preconditions.checkArgument(returnTypes.length > 0, "returnType must have at least one element");
		this.returnTypes = new ReturnTypes();
		for (int i = 0; i < returnTypes.length; i++) {
			Preconditions.checkNotNull(returnTypes[i], "returnType[%s] is null", i);
			this.returnTypes.add(new ClassInfoReference(returnTypes[i], Kleenean.UNKNOWN));
		}
		invalidateConstraints();
		return this;
	}

	public ExpressionSite returnTypes(Class<?>... returnTypes) {
		Preconditions.checkNotNull(returnTypes, "returnType is null");
		Preconditions.checkArgument(returnTypes.length > 0, "returnType must have at least one element");
		this.returnTypes = new ReturnTypes();
		for (int i = 0; i < returnTypes.length; i++) {
			Preconditions.checkNotNull(returnTypes[i], "returnType[%s] is null", i);
			ClassInfo<?> info = Classes.getSuperClassInfo(returnTypes[i]);
			this.returnTypes.add(new ClassInfoReference(info, Kleenean.UNKNOWN));
		}
		invalidateConstraints();
		return this;
	}

	public @UnmodifiableView Set<ClassInfoReference> getReturnTypes() {
		return returnTypes.asSet();
	}

	public ExpressionSite optional(boolean optional) {
		this.optional = optional;
		return this;
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

	public ExpressionSite allowLiterals(boolean allowLiterals) {
		this.allowLiterals = allowLiterals;
		invalidateConstraints();
		return this;
	}

	public boolean allowsLiterals() {
		return allowLiterals;
	}

	public ExpressionSite allowNonLiterals(boolean allowNonLiterals) {
		this.allowNonLiterals = allowNonLiterals;
		invalidateConstraints();
		return this;
	}

	public boolean allowsNonLiterals() {
		return allowNonLiterals;
	}

	public ExpressionSite allowSimplifiedLiterals(boolean allowSimplifiedLiterals) {
		this.allowSimplifiedLiterals = allowSimplifiedLiterals;
		invalidateConstraints();
		return this;
	}

	public boolean allowsSimplifiedLiterals() {
		return allowSimplifiedLiterals;
	}

	public ExpressionSite timeState(int timeState) {
		this.timeState = TimeState.fromInt(timeState);
		return this;
	}

	public ExpressionSite timeState(TimeState timeState) {
		this.timeState = timeState;
		return this;
	}

	public TimeState timeState() {
		return timeState;
	}

	public ExprInfo toExprInfo() {
		ClassInfo<?>[] classes = new ClassInfo<?>[returnTypes.size()];
		boolean[] isPlural = new boolean[returnTypes.size()];
		int i = 0;
		for (ClassInfoReference ref : returnTypes) {
			classes[i] = ref.getClassInfo();
			isPlural[i] = ref.isPlural().isTrue();
			i++;
		}
		ExprInfo info = new ExprInfo(classes, isPlural);
		info.isOptional = optional;
		if (allowLiterals) {
			info.flagMask |= SkriptParser.PARSE_LITERALS;
		}
		if (allowNonLiterals) {
			info.flagMask |= SkriptParser.PARSE_EXPRESSIONS;
		}
		info.time = timeState.getValue();
		return info;
	}

	public enum TimeState {
		PAST(-1),
		PRESENT(0),
		FUTURE(1);

		private final int value;

		TimeState(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static TimeState fromInt(int value) {
			for (TimeState state : values()) {
				if (state.getValue() == value) {
					return state;
				}
			}
			throw new IllegalArgumentException("Invalid TimeState value: " + value);
		}
	}

}
