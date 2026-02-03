package org.skriptlang.skript.bukkit.particles;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.SimpleClassSerializer;
import ch.njol.yggdrasil.SimpleClassSerializer.NonInstantiableClassSerializer;
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.particles.elements.effects.EffPlayEffect;
import org.skriptlang.skript.bukkit.particles.elements.expressions.*;
import org.skriptlang.skript.bukkit.particles.particleeffects.ConvergingEffect;
import org.skriptlang.skript.bukkit.particles.particleeffects.DirectionalEffect;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.bukkit.particles.particleeffects.ScalableEffect;
import org.skriptlang.skript.bukkit.particles.registration.DataGameEffects;
import org.skriptlang.skript.bukkit.particles.registration.DataParticles;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Module for particle and game effect related classes and elements.
 */
public class ParticleModule implements AddonModule {

	@Override
	public void init(SkriptAddon addon) {
		registerClasses();
		registerDataSerializers();
		DataGameEffects.getGameEffectInfos();
		DataParticles.getParticleInfos();
	}

	@Override
	public void load(SkriptAddon addon) {
		// load elements!
		SyntaxRegistry registry = addon.syntaxRegistry();
		ModuleOrigin origin = AddonModule.origin(addon, this);
		EffPlayEffect.register(registry, origin);
		ExprGameEffectWithData.register(registry, origin);
		ExprParticleCount.register(registry, origin);
		ExprParticleDistribution.register(registry, origin);
		ExprParticleOffset.register(registry, origin);
		ExprParticleSpeed.register(registry, origin);
		ExprParticleWithData.register(registry, origin);
		ExprParticleWithOffset.register(registry, origin);
		ExprParticleWithSpeed.register(registry, origin);
	}

	/**
	 * Registers particle and game effect related classes.
	 */
	private static void registerClasses() {
		// game effects
		Classes.registerClass(new ClassInfo<>(GameEffect.class, "gameeffect")
			.user("game ?effects?")
			.since("2.14")
			.description("Various game effects that can be played for players, like record disc songs, splash potions breaking, or fake bone meal effects.")
			.name("Game Effect")
			.usage(GameEffect.getAllNamesWithoutData())
			.supplier(() -> {
				Effect[] effects = Effect.values();
				return Arrays.stream(effects).map(GameEffect::new)
					.filter(effect -> effect.getData() == null)
					.iterator();
			})
			.serializer(new Serializer<>() {
				@Override
				public Fields serialize(GameEffect effect) {
					Fields fields = new Fields();
					fields.putPrimitive("name", effect.getEffect().name());
					fields.putObject("data", effect.getData());
					return fields;
				}

				@Override
				public void deserialize(GameEffect effect, Fields fields) {
					assert false;
				}

				@Override
				protected GameEffect deserialize(Fields fields) throws StreamCorruptedException {
					String name = fields.getAndRemovePrimitive("name", String.class);
					GameEffect effect;
					try {
						effect = new GameEffect(Effect.valueOf(name));
					} catch (IllegalArgumentException e) {
						return null;
					}
					effect.setData(fields.getObject("data"));
					return effect;
				}

				@Override
				public boolean mustSyncDeserialization() {
					return false;
				}

				@Override
				protected boolean canBeInstantiated() {
					return false;
				}
			})
			.defaultExpression(new EventValueExpression<>(GameEffect.class))
			.parser(new Parser<>() {
				@Override
				public GameEffect parse(String input, ParseContext context) {
					return GameEffect.parse(input);
				}

				@Override
				public String toString(GameEffect effect, int flags) {
					return effect.toString(flags);
				}

				@Override
				public String toVariableNameString(GameEffect o) {
					return o.getEffect().name();
				}
			}));

		// entity effects
		Classes.registerClass(new EnumClassInfo<>(EntityEffect.class, "entityeffect", "entity effect")
			.user("entity ?effects?")
			.name("Entity Effect")
			.description("Various entity effects that can be played for entities, like wolf howling, or villager happy.")
			.since("2.14"));

		// particles

		// Bukkit Particle enum. Used for Classes.toString, but should not be used directly.
		Classes.registerClass(new ClassInfo<>(Particle.class, "bukkitparticle")
			.name(ClassInfo.NO_DOC)
			.since("2.14")
			.parser(new Parser<>() {
				@Override
				public Particle parse(String input, ParseContext context) {
					throw new IllegalStateException();
				}

				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(Particle particle, int flags) {
					return ParticleEffect.toString(particle, flags);
				}

				@Override
				public String toVariableNameString(Particle particle) {
					return toString(particle, 0);
				}
			}));

		Classes.registerClass(new ClassInfo<>(ParticleEffect.class, "particle")
			.user("particle( ?effect)?s?")
			.since("2.14")
			.description("Various particles.")
			.name("Particle")
			.usage(ParticleEffect.getAllNamesWithoutData())
			.supplier(() -> {
				Particle[] particles = Particle.values();
				return Arrays.stream(particles).map(ParticleEffect::of).iterator();
			})
			.serializer(new ParticleSerializer())
			.defaultExpression(new EventValueExpression<>(ParticleEffect.class))
			.parser(new Parser<>() {
				@Override
				public ParticleEffect parse(String input, ParseContext context) {
					return ParticleEffect.parse(input, context);
				}

				@Override
				public String toString(ParticleEffect effect, int flags) {
					return effect.toString();
				}

				@Override
				public String toVariableNameString(ParticleEffect effect) {
					return effect.particle().name();
				}
			}));

		Classes.registerClass(new ClassInfo<>(ConvergingEffect.class, "convergingparticle")
			.user("converging ?particle( ?effect)?s?")
			.since("2.14")
			.description("A particle effect where particles converge towards a point.")
			.name("Converging Particle Effect")
			.supplier(() -> ParticleUtils.getConvergingParticles().stream()
				.map(ConvergingEffect::new)
				.iterator())
			.serializer(new ParticleSerializer())
			.defaultExpression(new EventValueExpression<>(ConvergingEffect.class))
			.parser(new Parser<>() {
				@Override
				public ConvergingEffect parse(String input, ParseContext context) {
					ParticleEffect effect = ParticleEffect.parse(input, context);
					if (effect instanceof ConvergingEffect convergingEffect)
						return convergingEffect;
					return null;
				}

				@Override
				public String toString(ConvergingEffect effect, int flags) {
					return effect.toString();
				}

				@Override
				public String toVariableNameString(ConvergingEffect effect) {
					return effect.particle().name();
				}
			}));

		Classes.registerClass(new ClassInfo<>(DirectionalEffect.class, "directionalparticle")
			.user("directional ?particle( ?effect)?s?")
			.since("2.14")
			.description("A particle effect which can be given a directional velocity.")
			.name("Directional Particle Effect")
			.supplier(() -> ParticleUtils.getDirectionalParticles().stream()
				.map(DirectionalEffect::new)
				.iterator())
			.serializer(new ParticleSerializer())
			.defaultExpression(new EventValueExpression<>(DirectionalEffect.class))
			.parser(new Parser<>() {
				@Override
				public DirectionalEffect parse(String input, ParseContext context) {
					ParticleEffect effect = ParticleEffect.parse(input, context);
					if (effect instanceof DirectionalEffect convergingEffect)
						return convergingEffect;
					return null;
				}

				@Override
				public String toString(DirectionalEffect effect, int flags) {
					return effect.toString();
				}

				@Override
				public String toVariableNameString(DirectionalEffect effect) {
					return effect.particle().name();
				}
			}));

		Classes.registerClass(new ClassInfo<>(ScalableEffect.class, "scalableparticle")
			.user("scalable ?particle( ?effect)?s?")
			.since("2.14")
			.description("A particle effect which can be scaled up or down.")
			.name("Scalable Particle Effect")
			.supplier(() -> ParticleUtils.getScalableParticles().stream()
				.map(ScalableEffect::new)
				.iterator())
			.serializer(new ParticleSerializer())
			.defaultExpression(new EventValueExpression<>(ScalableEffect.class))
			.parser(new Parser<>() {
				@Override
				public ScalableEffect parse(String input, ParseContext context) {
					ParticleEffect effect = ParticleEffect.parse(input, context);
					if (effect instanceof ScalableEffect convergingEffect)
						return convergingEffect;
					return null;
				}

				@Override
				public String toString(ScalableEffect effect, int flags) {
					return effect.toString();
				}

				@Override
				public String toVariableNameString(ScalableEffect effect) {
					return effect.particle().name();
				}
			})
			.property(Property.SCALE,
				"The scale multiplier to use for a particle. Generally larger numbers will result in larger particles.",
				Skript.instance(),
				//<editor-fold desc="scale handler" default-state=collapsed>
				new ExpressionPropertyHandler<ScalableEffect, Number>() {
					@Override
					public @Nullable Number convert(ScalableEffect propertyHolder) {
						return propertyHolder.hasScale() ? propertyHolder.scale() : null;
					}

					@Override
					public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
						return switch (mode) {
							case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
							default -> null;
						};
					}

					@Override
					public void change(ScalableEffect propertyHolder, Object @Nullable [] delta, ChangeMode mode) {
						double scaleDelta = delta == null ? 1 : ((Number) delta[0]).doubleValue();
						switch (mode) {
							case REMOVE:
								scaleDelta = -scaleDelta;
								// fallthrough
							case ADD:
								if (propertyHolder.hasScale()) // don't set scale if it doesn't have one
									propertyHolder.scale(propertyHolder.scale() + scaleDelta);
								break;
							case SET:
								propertyHolder.scale(scaleDelta);
								break;
							case RESET:
								if (propertyHolder.hasScale()) // don't reset scale if it doesn't have one
									propertyHolder.scale(scaleDelta);
								break;
						}
					}

					@Override
					public @NotNull Class<Number> returnType() {
						return Number.class;
					}
				}
				//</editor-fold>
			));
	}

	/**
	 * Registers data serializers for particle data classes.
	 * Particles need their data classes to be serializable, but we don't really want classinfos for them since
	 * they are not meant to be used directly in Skript. {@link SimpleClassSerializer} is perfect for this.
	 */
	private static void registerDataSerializers() {
		// allow serializing particle data classes
		Variables.yggdrasil.registerSingleClass(Color.class, "particle.color");
		Variables.yggdrasil.registerClassResolver(new NonInstantiableClassSerializer<>(Particle.DustOptions.class, "particle.dustoptions") {
			@Override
			public Fields serialize(Particle.DustOptions object) {
				Fields fields = new Fields();
				fields.putObject("color", object.getColor());
				fields.putPrimitive("size", object.getSize());
				return fields;
			}

			@Override
			protected Particle.DustOptions deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
				Color color = fields.getAndRemoveObject("color", Color.class);
				float size = fields.getAndRemovePrimitive("size", Float.class);
				if (color == null)
					throw new NotSerializableException("Color cannot be null for DustOptions");
				return new Particle.DustOptions(color, size);
			}
		});

		Variables.yggdrasil.registerClassResolver(new NonInstantiableClassSerializer<>(Particle.DustTransition.class, "particle.dusttransition") {
			@Override
			public Fields serialize(Particle.DustTransition object) {
				Fields fields = new Fields();
				fields.putObject("fromColor", object.getColor());
				fields.putObject("toColor", object.getToColor());
				fields.putPrimitive("size", object.getSize());
				return fields;
			}

			@Override
			protected Particle.DustTransition deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
				Color fromColor = fields.getAndRemoveObject("fromColor", Color.class);
				Color toColor = fields.getAndRemoveObject("toColor", Color.class);
				float size = fields.getAndRemovePrimitive("size", Float.class);
				if (fromColor == null || toColor == null)
					throw new NotSerializableException("Colors cannot be null for DustTransition");
				return new Particle.DustTransition(fromColor, toColor, size);
			}
		});

		Variables.yggdrasil.registerClassResolver( new NonInstantiableClassSerializer<>(Vibration.class, "particle.vibration") {
			@Override
			public Fields serialize(Vibration object) {
				Fields fields = new Fields();
				fields.putObject("destination", object.getDestination());
				fields.putPrimitive("arrivalTime", object.getArrivalTime());
				return fields;
			}

			@Override
			protected Vibration deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
				Vibration.Destination destination = fields.getAndRemoveObject("destination", Vibration.Destination.class);
				int arrivalTime = fields.getAndRemovePrimitive("arrivalTime", Integer.class);
				if (destination == null)
					throw new NotSerializableException("Destination cannot be null for Vibration");
				return new Vibration(destination, arrivalTime);
			}
		});

		if (Skript.isRunningMinecraft(1, 21, 9)) {
			Variables.yggdrasil.registerClassResolver(new NonInstantiableClassSerializer<>(Particle.Spell.class, "particle.spell") {
				@Override
				public Fields serialize(Particle.Spell object) {
					Fields fields = new Fields();
					fields.putObject("color", object.getColor());
					fields.putPrimitive("power", object.getPower());
					return fields;
				}

				@Override
				protected Particle.Spell deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
					Color color = fields.getAndRemoveObject("color", Color.class);
					float power = fields.getAndRemovePrimitive("power", Float.class);
					if (color == null)
						throw new NotSerializableException("Color cannot be null for Spell");
					return new Particle.Spell(color, power);
				}
			});
		}

		if (Skript.isRunningMinecraft(1, 21, 4)) {
			Variables.yggdrasil.registerClassResolver(new NonInstantiableClassSerializer<>(Particle.Trail.class, "particle.trail") {
				@Override
				public Fields serialize(Particle.Trail object) {
					Fields fields = new Fields();
					fields.putObject("target", object.getTarget());
					fields.putObject("color", object.getColor());
					fields.putPrimitive("duration", object.getDuration());
					return fields;
				}

				@Override
				protected Particle.Trail deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
					Location target = fields.getAndRemoveObject("target", Location.class);
					Color color = fields.getAndRemoveObject("color", Color.class);
					int duration = 20;
					// allow deserializing old versions without duration
					if (fields.hasField("duration"))
						duration = fields.getAndRemovePrimitive("duration", Integer.class);
					if (target == null)
						throw new NotSerializableException("Target cannot be null for Trail");
					if (color == null)
						throw new NotSerializableException("Color cannot be null for Trail");
					return new Particle.Trail(target, color, duration);
				}
			});
		} else if (Skript.isRunningMinecraft(1, 21, 2)) {
			//<editor-fold desc="Particle.TargetColor serializer for 1.21.2 and 1.21.3" defaultstate="collapsed">
			var targetColorClass = Arrays.stream(Particle.class.getClasses()).filter(c -> c.getSimpleName().equals("TargetColor")).findFirst().orElse(null);
			if (targetColorClass == null)
				throw new RuntimeException("Could not find Particle.TargetColor class for serializer");
			try {
				var constructor = targetColorClass.getDeclaredConstructor(Location.class, Color.class);
				var getTargetMethod = targetColorClass.getDeclaredMethod("getTarget");
				var getColorMethod = targetColorClass.getDeclaredMethod("getColor");
				//noinspection unchecked
				Variables.yggdrasil.registerClassResolver(new NonInstantiableClassSerializer<>((Class<Object>) targetColorClass, "particle.targetcolor") {
					@Override
					public Fields serialize(Object object) {
						Fields fields = new Fields();
						try {
							fields.putObject("target", getTargetMethod.invoke(object));
							fields.putObject("color", getColorMethod.invoke(object));
						} catch (IllegalAccessException | InvocationTargetException e) {
							throw new RuntimeException(e);
						}
						return fields;
					}

					@Override
					protected Object deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
						Location target = fields.getAndRemoveObject("target", Location.class);
						Color color = fields.getAndRemoveObject("color", Color.class);
						if (target == null)
							throw new NotSerializableException("Target cannot be null for Trail");
						if (color == null)
							throw new NotSerializableException("Color cannot be null for Trail");
						try {
							return constructor.newInstance(target, color);
						} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
							throw new RuntimeException(e);
						}
					}
				});

			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
			//</editor-fold>
		}
	}

	/**
	 * Serializer for ParticleEffect.
	 * Does not store receivers/locations, only the particle effect data.
	 */
	static class ParticleSerializer extends Serializer<ParticleEffect> {
		@Override
		public Fields serialize(ParticleEffect effect) {
			Fields fields = new Fields();
			fields.putObject("name", effect.particle().name());
			fields.putPrimitive("count", effect.count());
			fields.putPrimitive("offsetX", effect.offsetX());
			fields.putPrimitive("offsetY", effect.offsetY());
			fields.putPrimitive("offsetZ", effect.offsetZ());
			fields.putPrimitive("extra", effect.extra());
			fields.putObject("data", effect.data());
			fields.putPrimitive("force", effect.force());
			return fields;
		}

		@Override
		public void deserialize(ParticleEffect effect, Fields fields) {
			assert false;
		}

		@Override
		protected ParticleEffect deserialize(Fields fields) throws StreamCorruptedException {
			String name = fields.getAndRemoveObject("name", String.class);
			ParticleEffect effect;
			try {
				effect = ParticleEffect.of(Particle.valueOf(name));
			} catch (IllegalArgumentException e) {
				return null;
			}
			return effect.count(fields.getAndRemovePrimitive("count", Integer.class))
				.offset(fields.getAndRemovePrimitive("offsetX", Double.class),
					fields.getAndRemovePrimitive("offsetY", Double.class),
					fields.getAndRemovePrimitive("offsetZ", Double.class))
				.extra(fields.getAndRemovePrimitive("extra", Double.class))
				.force(fields.getAndRemovePrimitive("force", Boolean.class))
				.data(fields.getAndRemoveObject("data", effect.particle().getDataType()));
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}

		@Override
		protected boolean canBeInstantiated() {
			return false;
		}
	}

	@Override
	public String name() {
		return "particle";
	}

}
