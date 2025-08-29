package org.skriptlang.skript.lang.properties;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class PropertyUtils {




	public static class PropertyMap<Handler extends Property.PropertyHandler<?>> extends HashMap<Class<?>, PropertyInfo<Handler>> {
		public @Nullable Handler getHandler(Class<?> inputClass) {
			PropertyInfo<Handler> propertyInfo;
			// check if we don't already know the right info for this class
			propertyInfo = get(inputClass);
			if (propertyInfo == null) {
				// no property info found, return null
				return null;
			}
			// get the name using the property handler
			return propertyInfo.handler();
		}

		public PropertyInfo<Handler> get(Class<?> actualClass) {
			if (super.containsKey(actualClass)) {
				return super.get(actualClass);
			}

			Class<?> closestClass = null;
			for (Class<?> candidateClass : keySet()) {
				// need to make sure we get the closest match
				if (candidateClass.isAssignableFrom(actualClass)) {
					if (closestClass == null || closestClass.isAssignableFrom(candidateClass)) {
						closestClass = candidateClass;
					}
				}
			}

			var propertyInfo = super.get(closestClass);
			// add to properties so we don't have to search again
			put(actualClass, propertyInfo);
			return propertyInfo;
		}

	}

	public static Expression<?> asProperty(Property<?> property, Expression<?> expr) {
		if (expr == null) {
			return null; // no expression to convert
		}

		// get all types with a name property
		List<ClassInfo<?>> namedClassInfos = Classes.getClassInfosByProperty(property);
		Class<?>[] namedClasses = namedClassInfos.stream().map(ClassInfo::getC).toArray(Class[]::new);

		//noinspection unchecked,rawtypes
		return expr.getConvertedExpression((Class[]) namedClasses);
	}


	public static <Handler extends Property.PropertyHandler<?>> PropertyMap<Handler> getPossiblePropertyInfos(
		Property<Handler> property,
		Expression<?> expr
	) {
		PropertyMap<Handler> propertyInfos = new PropertyMap<>();
		// for each return type, check if it has a name property
		for (Class<?> returnType : expr.possibleReturnTypes()) {
			ClassInfo<?> classInfo = Classes.getSuperClassInfo(returnType);
			// get property
			var propertyInfo = classInfo.getPropertyInfo(property);
			if (propertyInfo == null) {
				continue; // no name property
			}
			propertyInfos.put(classInfo.getC(), propertyInfo);
		}
		return propertyInfos;
	}



}
