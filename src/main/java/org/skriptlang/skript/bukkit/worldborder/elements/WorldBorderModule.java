package org.skriptlang.skript.bukkit.worldborder.elements;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderBoundsChangeFinishEvent;
import io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.bukkit.worldborder.elements.effects.EffWorldBorderExpand;
import org.skriptlang.skript.bukkit.worldborder.elements.expressions.*;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class WorldBorderModule extends HierarchicalAddonModule {

	public WorldBorderModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(WorldBorder.class, "worldborder")
			.user("world ?borders?")
			.name("World Border")
			.description("Represents the border of a world or player.")
			.since("2.11")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(WorldBorder border, int flags) {
					if (border.getWorld() == null)
						return "virtual world border";
					return "world border of world named '" + border.getWorld().getName() + "'";
				}

				@Override
				public String toVariableNameString(WorldBorder border) {
					return toString(border, 0);
				}
			})
			.defaultExpression(new EventValueExpression<>(WorldBorder.class)));
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);
		SyntaxRegistry syntaxRegistry = moduleRegistry(addon);

		syntaxRegistry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "World Border Bounds Change")
				.addEvent(WorldBorderBoundsChangeEvent.class)
				.addPatterns("world[ ]border [bounds] chang(e|ing)")
				.addDescription(
					"Called when a world border changes its bounds, either over time, or instantly.",
					"This event does not get called for virtual borders."
				)
				.addExample("""
					on worldborder bounds change:
						broadcast "The diameter of %event-worldborder% is changing from %past event-number% to %event-number% over the next %event-timespan%
					""")
				.addSince("2.11")
				.build()
		);

		eventValueRegistry.register(EventValue.builder(WorldBorderBoundsChangeEvent.class, WorldBorder.class)
			.getter(WorldBorderBoundsChangeEvent::getWorldBorder)
			.build());
		eventValueRegistry.register(EventValue.builder(WorldBorderBoundsChangeEvent.class, Number.class)
			.getter(WorldBorderBoundsChangeEvent::getNewSize)
			.build());
		eventValueRegistry.register(EventValue.builder(WorldBorderBoundsChangeEvent.class, Timespan.class)
			.getter(event -> new Timespan(event.getDurationTicks()))
			.build());
		eventValueRegistry.register(EventValue.builder(WorldBorderBoundsChangeEvent.class, Number.class)
			.getter(WorldBorderBoundsChangeEvent::getOldSize)
			.time(EventValue.Time.PAST)
			.build());

		syntaxRegistry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "World Border Bounds Finish Change")
				.addEvent(WorldBorderBoundsChangeFinishEvent.class)
				.addPatterns("world[ ]border [bounds] finish chang(e|ing)")
				.addDescription("""	
					Called when a moving world border has finished its move.
					This event does not get called for virtual borders.
					""")
				.addExample("""
					on worldborder bounds finish change:
						broadcast "Over the past %event-timespan%, the diameter of %event-worldborder% went from %past event-number% to %event-number%"
					""")
				.addSince("2.11")
				.build()
		);

		eventValueRegistry.register(EventValue.builder(WorldBorderBoundsChangeFinishEvent.class, WorldBorder.class)
			.getter(WorldBorderBoundsChangeFinishEvent::getWorldBorder)
			.build());
		eventValueRegistry.register(EventValue.builder(WorldBorderBoundsChangeFinishEvent.class, Number.class)
			.getter(WorldBorderBoundsChangeFinishEvent::getNewSize)
			.build());
		eventValueRegistry.register(EventValue.builder(WorldBorderBoundsChangeFinishEvent.class, Number.class)
			.getter(WorldBorderBoundsChangeFinishEvent::getOldSize)
			.time(EventValue.Time.PAST)
			.build());
		eventValueRegistry.register(EventValue.builder(WorldBorderBoundsChangeFinishEvent.class, Timespan.class)
			.getter(event -> new Timespan((long) event.getDuration()))
			.build());

		syntaxRegistry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "World Border Center Change")
				.addEvent(WorldBorderCenterChangeEvent.class)
				.addPatterns("world[ ]border center chang(e|ing)")
				.addDescription("""	
					Called when a world border's center has changed.
					This event does not get called for virtual borders.
					""")
				.addExample("""
					on worldborder center change:
						broadcast "The center of %event-worldborder% has moved from %past event-location% to %event-location%
					""")
				.addSince("2.11")
				.build()
		);

		eventValueRegistry.register(EventValue.builder(WorldBorderCenterChangeEvent.class, WorldBorder.class)
			.getter(WorldBorderCenterChangeEvent::getWorldBorder)
			.build());
		eventValueRegistry.register(EventValue.builder(WorldBorderCenterChangeEvent.class, Location.class)
			.getter(WorldBorderCenterChangeEvent::getNewCenter)
			.build());
		eventValueRegistry.register(EventValue.builder(WorldBorderCenterChangeEvent.class, Location.class)
			.getter(WorldBorderCenterChangeEvent::getOldCenter)
			.time(EventValue.Time.PAST)
			.build());

		register(addon,
			a -> ExprSecCreateWorldBorder.register(a, eventValueRegistry),
			EffWorldBorderExpand::register,
			ExprWorldBorder::register,
			ExprWorldBorderCenter::register,
			ExprWorldBorderDamageAmount::register,
			ExprWorldBorderDamageBuffer::register,
			ExprWorldBorderSize::register,
			ExprWorldBorderWarningDistance::register,
			ExprWorldBorderWarningTime::register
		);
	}

	@Override
	public String name() {
		return "worldborder";
	}

}
