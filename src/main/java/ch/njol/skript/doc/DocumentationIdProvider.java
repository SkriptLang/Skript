package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

public class DocumentationIdProvider {

	/**
	 * Some syntax classes are registered more than once. This method applies a suffix
	 * to the id in order to differentiate them
	 * @param id the potentially conflicting ID
	 * @param collisionCount the number of conflicts this element has
	 * @return the unique ID for the element
	 */
	private static String addCollisionSuffix(String id, int collisionCount) {
		if (collisionCount == 0) {
			return id;
		}
		return id + "-" + (collisionCount + 1);
	}

	/**
	 * Calculates the number of collisions in an iterator
	 * @param potentialCollisions the iterator of potential collisions
	 * @param collisionCriteria a predicate which checks whether a potential collision is really a collision
	 * @param equalsCriteria a predicate which checks whether a potential collision equals the current element we are generating
	 * @return the number of collisions in potentialCollisions up until equalsCriteria was true
	 */
	private static <T> int calculateCollisionCount(Iterator<? extends T> potentialCollisions, Predicate<T> collisionCriteria,
											Predicate<T> equalsCriteria) {
		int collisionCount = 0;
		while (potentialCollisions.hasNext()) {
			T potentialCollision = potentialCollisions.next();
			if (collisionCriteria.test(potentialCollision)) {
				if (equalsCriteria.test(potentialCollision)) {
					break;
				}
				collisionCount += 1;
			}
		}
		return collisionCount;
	}

	/**
	 * Gets the documentation ID of a syntax element
	 * @param syntaxInfo the SyntaxElementInfo to get the ID of
	 * @return the ID of the syntax element
	 */
	public static <T> String getId(SyntaxElementInfo<? extends T> syntaxInfo) {
		Class<?> syntaxClass = syntaxInfo.getElementClass();
		Iterator<? extends SyntaxElementInfo<?>> syntaxElementIterator;
		if (Effect.class.isAssignableFrom(syntaxClass)) {
			syntaxElementIterator = Skript.getEffects().iterator();
		} else if (Condition.class.isAssignableFrom(syntaxClass)) {
			syntaxElementIterator = Skript.getConditions().iterator();
		} else if (Expression.class.isAssignableFrom(syntaxClass)) {
			syntaxElementIterator = Skript.getExpressions();
		} else if (Section.class.isAssignableFrom(syntaxClass)) {
			syntaxElementIterator = Skript.getSections().iterator();
		} else if (Structure.class.isAssignableFrom(syntaxClass)) {
			syntaxElementIterator = Skript.getStructures().iterator();
		} else {
			throw new IllegalStateException("Unsupported syntax type provided");
		}
		int collisionCount = calculateCollisionCount(syntaxElementIterator,
			elementInfo -> elementInfo.getElementClass() == syntaxClass,
			elementInfo -> Arrays.equals(elementInfo.getPatterns(), syntaxInfo.getPatterns()));
		DocumentationId documentationIdAnnotation = syntaxClass.getAnnotation(DocumentationId.class);
		if (documentationIdAnnotation == null) {
			return addCollisionSuffix(syntaxClass.getSimpleName(), collisionCount);
		}
		return addCollisionSuffix(documentationIdAnnotation.value(), collisionCount);
	}

	/**
	 * Gets the documentation ID of a function
	 * @param function the function to get the ID of
	 * @return the documentation ID of the function
	 */
	public static String getId(Function<?> function) {
		int collisionCount = calculateCollisionCount(Functions.getJavaFunctions().iterator(),
			javaFunction -> function.getName().equals(javaFunction.getName()),
			javaFunction -> javaFunction == function);
		return addCollisionSuffix(function.getName(), collisionCount);
	}

	/**
	 * Gets either the explicitly declared documentation ID or code name of a classinfo
	 * @param classInfo the ClassInfo to get the ID of
	 * @return the ID of the ClassInfo
	 */
	private static String getClassInfoId(ClassInfo<?> classInfo) {
		return Objects.requireNonNullElse(classInfo.getDocumentationID(), classInfo.getCodeName());
	}

	/**
	 * Gets the documentation ID of a ClassInfo
	 * @param classInfo the ClassInfo to get the ID of
	 * @return the ID of the ClassInfo
	 */
	public static String getId(ClassInfo<?> classInfo) {
		String classInfoId = getClassInfoId(classInfo);
		int collisionCount = calculateCollisionCount(Classes.getClassInfos().iterator(),
			otherClassInfo -> classInfoId.equals(getClassInfoId(otherClassInfo)),
			otherClassInfo -> classInfo == otherClassInfo);
		return addCollisionSuffix(classInfoId, collisionCount);
	}

	/**
	 * Gets either the explicitly declared documentation ID or default ID of an event
	 * @param eventInfo the event to get the ID of
	 * @return the ID of the event
	 */
	private static String getEventId(SkriptEventInfo<?> eventInfo) {
		return Objects.requireNonNullElse(eventInfo.getDocumentationID(), eventInfo.getId());
	}

	/**
	 * Gets the documentation ID of an event
	 * @param eventInfo the event to get the ID of
	 * @return the ID of the event
	 */
	public static String getId(SkriptEventInfo<?> eventInfo) {
		String eventId = getEventId(eventInfo);
		int collisionCount = calculateCollisionCount(Skript.getEvents().iterator(),
			otherEventInfo -> eventId.equals(getEventId(otherEventInfo)),
			otherEventInfo -> Arrays.equals(otherEventInfo.getPatterns(), eventInfo.getPatterns()));
		return addCollisionSuffix(eventId, collisionCount);
	}

}
