package ch.njol.skript.conditions;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.registrations.Classes;

@Name("Object Can Be Copied")
@Description("Whether an object can be copied.")
@Example("if a diamond can be copied: # True")
@Example("if the last spawned entity can be copied: # False")
@Since("INSERT VERSION")
@Keywords({"copy"})
public class CondCanCopy extends PropertyCondition<Object> {

	static {
		register(CondCanCopy.class, PropertyType.CAN, "be copied", "objects");
	}

	@Override
	public boolean check(Object object) {
		ClassInfo<?> classInfo;
		if (object instanceof ClassInfo<?> info) {
			classInfo = info;
		} else {
			classInfo = Classes.getSuperClassInfo(object.getClass());
		}
		return classInfo.getCloner() != null;
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.CAN;
	}

	@Override
	protected String getPropertyName() {
		return "be copied";
	}

}
