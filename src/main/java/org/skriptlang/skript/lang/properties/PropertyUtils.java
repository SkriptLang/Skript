package org.skriptlang.skript.lang.properties;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class PropertyUtils {




	public static class PropertyMap<Handler extends PropertyHandler<?>> extends HashMap<Class<?>, Property.PropertyInfo<Handler>> {
		public @Nullable Handler getHandler(Class<?> inputClass) {
			Property.PropertyInfo<Handler> propertyInfo;
			// check if we don't already know the right info for this class
			propertyInfo = get(inputClass);
			if (propertyInfo == null) {
				// no property info found, return null
				return null;
			}
			// get the name using the property handler
			return propertyInfo.handler();
		}

		public Property.PropertyInfo<Handler> get(Class<?> actualClass) {
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
		List<ClassInfo<?>> classInfos = Classes.getClassInfosByProperty(property);
		Class<?>[] classes = classInfos.stream().map(ClassInfo::getC).toArray(Class[]::new);

		//noinspection unchecked,rawtypes
		return LiteralUtils.defendExpression(expr).getConvertedExpression((Class[]) classes);
	}


	public static <Handler extends PropertyHandler<?>> PropertyMap<Handler> getPossiblePropertyInfos(
		Property<Handler> property,
		Expression<?> expr
	) {
		PropertyMap<Handler> propertyInfos = new PropertyMap<>();

		// get all types with a name property
		List<ClassInfo<?>> classInfos = Classes.getClassInfosByProperty(property);

		// for each return type, match to a classinfo w/ name property
		for (Class<?> returnType : expr.possibleReturnTypes()) {
			ClassInfo<?> closestInfo = null;
			for (ClassInfo<?> propertiedClassInfo  : classInfos) {
				if (propertiedClassInfo.getC() == returnType) {
					// exact match, use it
					closestInfo = propertiedClassInfo;
					break;
				}
				if (propertiedClassInfo.getC().isAssignableFrom(returnType)) {
					// closest match so far
					if (closestInfo == null || closestInfo.getC().isAssignableFrom(propertiedClassInfo.getC())) {
						closestInfo = propertiedClassInfo;
					}
				}
			}
			if (closestInfo == null) {
				continue; // no name property
			}

			// get property
			var propertyInfo = closestInfo.getPropertyInfo(property);
			ClassInfo<?> classInfo = Classes.getSuperClassInfo(returnType);
			propertyInfos.put(classInfo.getC(), propertyInfo);
			propertyInfos.put(closestInfo.getC(), propertyInfo);
		}
		return propertyInfos;
	}



}
