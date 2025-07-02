package ch.njol.skript.lang.util;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.sections.EffSecSpawn.SpawnEvent;
import ch.njol.skript.variables.HintManager;
import ch.njol.skript.variables.HintManager.Backup;
import ch.njol.skript.variables.Variables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * Utility methods for working with {@link Section}s and {@link SectionExpression}s.
 */
public final class SectionUtils {

	private SectionUtils() { }

	/**
	 * This method is used for loading a section into a {@link Trigger} under different context ({@link Event}s).
	 * However, unlike the traditional methods such as {@link Section#loadCode(SectionNode, String, Runnable, Runnable, Class[])},
	 * this method assumes some level of linkage between the returned trigger and the section it was loaded from.
	 * These assumptions are:
	 * <ul>
	 *     <li>Local variables (and at parse time, type hints) will be shared between the two sections.</li>
	 *     <li>Delays within the trigger are not permitted.</li>
	 * </ul>
	 * As a result, this method takes action to ensure that type hints are shared and that delays are not permitted.
	 * At runtime, local variables will need to be copied by the caller
	 *  using a method such as {@link Variables#withLocalVariables(Event, Event, Runnable)}
	 * @param name The name of the section being loaded.
	 * @param triggerSupplier A function to load code using a trigger.
	 *  The function has two runnable arguments. When using a method like {@link Section#loadCode(SectionNode, String, Runnable, Runnable, Class[])},
	 *   the runnable arguments represent the parameters {@code beforeLoading} and {@code afterLoading}, respectively.
	 *   <b>It is imperative that these runnable arguments be passed.<b>
	 *   <b>If the method you are using does not support those arguments, use a different method.</b>
	 * @return The result of {@code triggerSupplier}, or null if some issue occurred.
	 */
	@SuppressWarnings("JavadocReference")
	public static @Nullable Trigger loadLinkedCode(String name, BiFunction<Runnable, Runnable, Trigger> triggerSupplier) {
		AtomicBoolean delayed = new AtomicBoolean(false);
		AtomicReference<Backup> hintBackup = new AtomicReference<>();
		// Copy hints and ensure no delays
		Runnable beforeLoading = () -> ParserInstance.get().getHintManager().enterScope(false);
		Runnable afterLoading = () -> {
			ParserInstance parser = ParserInstance.get();
			delayed.set(!parser.getHasDelayBefore().isFalse());
			HintManager hintManager = parser.getHintManager();
			hintBackup.set(hintManager.backup());
			hintManager.exitScope();
		};

		Trigger trigger = triggerSupplier.apply(beforeLoading, afterLoading);

		if (delayed.get()) {
			Skript.error("Delays can't be used within a '" + name + "' section");
			return null;
		}
		HintManager hintManager = ParserInstance.get().getHintManager();
		hintManager.enterScope(false);
		hintManager.restore(hintBackup.get());
		hintManager.exitScope();

		return trigger;
	}

}
