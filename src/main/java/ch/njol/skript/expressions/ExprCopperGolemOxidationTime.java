package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.ReflectUtils;

import java.lang.reflect.Method;

@Name("Time Until Oxidation")
@Description("""
	The time until a copper golem goes through oxidation til reaching the max oxidation phase. (Normal -> Exposed -> Weathered -> Oxidized)
	Copper golems that are waxed do not go through oxidation.
	Setting or resetting the time until oxidation on a waxed copper golem will remove the waxed state.
	Resetting the time until oxidation uses vanilla behavior of generating a random time.
	""")
@Example("set {_time} to the time until oxidation of last spawned copper golem")
@Example("set the time until oxidation of last spawned copper golem to 10 seconds")
@Example("clear the time until oxidation of last spawned copper golem")
@RequiredPlugins("Minecraft 1.21.9+")
@Since("INSERT VERSION")
public class ExprCopperGolemOxidationTime extends SimplePropertyExpression<Entity, Timespan> {

	private static Method getOxidizingMethod;
	private static Class<?> atTimeClass;
	private static Method getTimeMethod;
	private static Method newTimeMethod;
	private static Method unsetMethod;

	static {
		if (ReflectUtils.classExists("org.bukkit.entity.CopperGolem")) {
			register(ExprCopperGolemOxidationTime.class, Timespan.class, "time until oxidation", "entities");

			getOxidizingMethod = ReflectUtils.getMethod("org.bukkit.entity.CopperGolem", "getOxidizing");
			atTimeClass = ReflectUtils.getClass("org.bukkit.entity.CopperGolem$Oxidizing$AtTime");
			getTimeMethod = ReflectUtils.getMethod(atTimeClass, "time");
			newTimeMethod = ReflectUtils.getMethod("org.bukkit.entity.CopperGolem$Oxidizing", "atTime", long.class);
			unsetMethod = ReflectUtils.getMethod("org.bukkit.entity.CopperGolem$Oxidizing", "unset");
		}
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
		if (entity instanceof CopperGolem golem) {
			Object oxidizing = ReflectUtils.methodInvoke(getOxidizingMethod, golem);
			if (!(atTimeClass.isInstance(oxidizing)))
				return null;
			long worldTime = golem.getWorld().getGameTime();
			//noinspection DataFlowIssue
			long oxidationTime = ReflectUtils.methodInvoke(getTimeMethod, oxidizing);
			if (worldTime > oxidationTime)
				return null;
			return new Timespan(TimePeriod.TICK, oxidationTime - worldTime);
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return CollectionUtils.array(Timespan.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			assert delta != null;
			long ticks = ((Timespan) delta[0]).getAs(TimePeriod.TICK);
			for (Entity entity : getExpr().getArray(event)) {
				if (!(entity instanceof CopperGolem golem))
					continue;
				long worldTime = golem.getWorld().getGameTime();
				golem.setOxidizing(ReflectUtils.methodInvoke(newTimeMethod, null, worldTime + ticks));
			}
		} else if (mode == ChangeMode.RESET) {
			for (Entity entity : getExpr().getArray(event)) {
				if (!(entity instanceof CopperGolem golem))
					continue;
				golem.setOxidizing(ReflectUtils.methodInvoke(unsetMethod));
			}
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "time until oxidation";
	}

}
