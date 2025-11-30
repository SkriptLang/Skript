package org.skriptlang.skript.bukkit.particles;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.SimpleClassSerializer;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.bukkit.particles.particleeffects.*;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleInfo.DataSupplier;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleInfo.ToString;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ParticleModule {

	public static final List<ParticleInfo<?>> DATA_PARTICLE_INFOS = new ArrayList<>();

	public static void load () throws IOException {
		registerClasses();
		registerDataSerializers();
		registerDataParticles();

		// load elements!
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.particles", "elements");
	}

	private static void registerClasses() {
		// gane effects
		Classes.registerClass(new ClassInfo<>(GameEffect.class, "gameeffect")
			.user("game ?effects?")
			.since("INSERT VERSION")
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
			.since("INSERT VERSION"));

		// particles

		// Bukkit Particle enum. Used for Classes.toString, but should not be used directly.
		Classes.registerClass(new ClassInfo<>(Particle.class, "bukkitparticle")
			.name(ClassInfo.NO_DOC)
			.since("INSERT VERSION")
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
			.since("INSERT VERSION")
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
			.since("INSERT VERSION")
			.description("A particle effect where particles converge towards a point.")
			.name("Converging Particle Effect")
			.supplier(() -> ParticleUtils.getConvergingParticles().stream()
				.map(ConvergingEffect::new)
				.iterator())
			.serializer(new ParticleSerializer())
			.defaultExpression(new EventValueExpression<>(ConvergingEffect.class)));

		Classes.registerClass(new ClassInfo<>(DirectionalEffect.class, "directionalparticle")
			.user("directional ?particle( ?effect)?s?")
			.since("INSERT VERSION")
			.description("A particle effect which can be given a directional velocity.")
			.name("Directional Particle Effect")
			.supplier(() -> ParticleUtils.getDirectionalParticles().stream()
				.map(DirectionalEffect::new)
				.iterator())
			.serializer(new ParticleSerializer())
			.defaultExpression(new EventValueExpression<>(DirectionalEffect.class)));

		Classes.registerClass(new ClassInfo<>(ScalableEffect.class, "scalableparticle")
			.user("scalable ?particle( ?effect)?s?")
			.since("INSERT VERSION")
			.description("A particle effect which can be scaled up or down.")
			.name("Scalable Particle Effect")
			.supplier(() -> ParticleUtils.getScalableParticles().stream()
				.map(ScalableEffect::new)
				.iterator())
			.serializer(new ParticleSerializer())
			.defaultExpression(new EventValueExpression<>(ScalableEffect.class)));
	}

	private static <F, D> void registerParticle(Particle particle, String pattern, D defaultData, Function<F, D> dataFunction, ToString<D> toStringFunction) {
		registerParticle(particle, pattern, (event, expressions, parseResult) -> {
			if (expressions[0] == null)
				return defaultData; // default data if none is provided
			//noinspection unchecked
			D data = dataFunction.apply((F) expressions[0].getSingle(event));
			if (data == null)
				return defaultData; // default data if none is provided
			return data;
		}, toStringFunction);
	}

	private static <D> void registerParticle(Particle particle, String pattern, DataSupplier<D> dataSupplier, ToString<D> toStringFunction) {
		DATA_PARTICLE_INFOS.add(new ParticleInfo<>(particle, pattern, dataSupplier, toStringFunction));
	}

	private static void registerDataParticles() {

		// colors

		registerParticle(Particle.EFFECT, "[a[n]] %color% effect particle[s] (of|with) power %number%",
				//<editor-fold desc="spell lambda">
				(event, expressions, parseResult) -> {
					ch.njol.skript.util.Color color = (ch.njol.skript.util.Color) expressions[0].getSingle(event);
					if (color == null)
						color = ColorRGB.fromBukkitColor(org.bukkit.Color.WHITE); // default color if none is provided
					Number power = (Number) expressions[1].getSingle(event);
					if (power == null)
						power = 1.0; // default power if none is provided
					return new Particle.Spell(color.asBukkitColor(), power.floatValue());
				},
				//</editor-fold>
				spell -> Classes.toString(ColorRGB.fromBukkitColor(spell.getColor())) + " effect particle of power " + spell.getPower());

		registerParticle(Particle.ENTITY_EFFECT, "[a[n]] %color% (potion|entity) effect particle[s]", org.bukkit.Color.WHITE,
				color -> ((ch.njol.skript.util.Color) color).asBukkitColor(),
				color -> Classes.toString(ColorRGB.fromBukkitColor(color)) + " potion effect particle");

		registerParticle(Particle.FLASH, "[a[n]] %color% flash particle[s]", org.bukkit.Color.WHITE,
				color -> ((ch.njol.skript.util.Color) color).asBukkitColor(),
				color -> Classes.toString(ColorRGB.fromBukkitColor(color)) + " flash particle");

		registerParticle(Particle.TINTED_LEAVES, "[a[n]] %color% tinted leaves particle[s]", org.bukkit.Color.WHITE,
				color -> ((ch.njol.skript.util.Color) color).asBukkitColor(),
				color -> Classes.toString(ColorRGB.fromBukkitColor(color)) + " tinted leaves particle");

		registerParticle(Particle.DUST, "[a[n]] %color% dust particle[s] [of size %number%]",
				//<editor-fold desc="dust options lambda" defaultstate="collapsed">
				(event, expressions, parseResult) -> {
					org.bukkit.Color bukkitColor;
					ch.njol.skript.util.Color color = (ch.njol.skript.util.Color) expressions[0].getSingle(event);
					if (color == null) {
						bukkitColor = org.bukkit.Color.WHITE; // default color if none is provided
					} else {
						bukkitColor = color.asBukkitColor();
					}

					Number size = (Number) expressions[1].getSingle(event);
					if (size == null || size.doubleValue() <= 0) {
						size = 1.0; // default size if none is provided or invalid
					}

					return new Particle.DustOptions(bukkitColor, size.floatValue());
				}, //</editor-fold>
				dustOptions -> Classes.toString(ColorRGB.fromBukkitColor(dustOptions.getColor())) + " dust particle of size " + dustOptions.getSize());

		// dust color transition particle
		registerParticle(Particle.DUST_COLOR_TRANSITION, "[a[n]] %color% dust particle[s] [of size %number%] that transitions to %color%",
				//<editor-fold desc="dust color transition options lambda" defaultstate="collapsed">
				(event, expressions, parseResult) -> {
					org.bukkit.Color bukkitColor;
					ch.njol.skript.util.Color color = (ch.njol.skript.util.Color) expressions[0].getSingle(event);
					if (color == null) {
						bukkitColor = org.bukkit.Color.WHITE; // default color if none is provided
					} else {
						bukkitColor = color.asBukkitColor();
					}

					Number size = (Number) expressions[1].getSingle(event);
					if (size == null || size.doubleValue() <= 0) {
						size = 1.0; // default size if none is provided or invalid
					}

					ch.njol.skript.util.Color toColor = (ch.njol.skript.util.Color) expressions[2].getSingle(event);
					org.bukkit.Color bukkitToColor;
					if (toColor == null) {
						bukkitToColor = org.bukkit.Color.WHITE; // default transition color if none is provided
					} else {
						bukkitToColor = toColor.asBukkitColor();
					}

					return new Particle.DustTransition(bukkitColor, bukkitToColor, size.floatValue());
				}, //</editor-fold>
				dustTransition -> Classes.toString(ColorRGB.fromBukkitColor(dustTransition.getColor())) +
						" dust particle of size " + dustTransition.getSize() +
						" that transitions to " + Classes.toString(ColorRGB.fromBukkitColor(dustTransition.getToColor())));

		// blockdata

		DataSupplier<BlockData> blockdataData = (event, expressions, parseResult) -> {
			//<editor-fold desc="blockdataData lambda" defaultstate="collapsed">
			Object object = expressions[0].getSingle(event);
			if (object instanceof ItemType itemType) {
				ItemStack random = itemType.getRandom();
				return Bukkit.createBlockData(random != null ? random.getType() : itemType.getMaterial());
			} else if (object instanceof BlockData blockData) {
				return blockData;
			}
			return Bukkit.createBlockData(Material.AIR); // default block if none is provided
			//</editor-fold>
		};

		registerParticle(Particle.BLOCK, "[a[n]] %itemtype/blockdata% block particle[s]", blockdataData,
				blockData -> Classes.toString(blockData) + " block particle");

		registerParticle(Particle.BLOCK_CRUMBLE, "[a[n]] %itemtype/blockdata% [block] crumble particle[s]", blockdataData,
				blockData -> Classes.toString(blockData) + " block crumble particle");

		registerParticle(Particle.BLOCK_MARKER, "[a[n]] %itemtype/blockdata% [block] marker particle[s]", blockdataData,
				blockData -> Classes.toString(blockData) + " block marker particle");

		registerParticle(Particle.DUST_PILLAR, "[a[n]] %itemtype/blockdata% dust pillar particle[s]", blockdataData,
				blockData -> Classes.toString(blockData) + " dust pillar particle");

		registerParticle(Particle.FALLING_DUST, "[a] falling %itemtype/blockdata% dust particle[s]", blockdataData,
				blockData -> "falling " + Classes.toString(blockData) + " dust particle");

		registerParticle(Particle.DRAGON_BREATH, "[a] dragon breath particle[s] [of power %-number%]", 0.5f, input -> input,
				power -> "dragon breath particle of power " + power);

		// misc

		registerParticle(Particle.ITEM, "[an] %itemtype% item particle[s]",
				//<editor-fold desc="item stack data lamba" defaultstate="collapsed">
				(event, expressions, parseResult) -> {
					ItemType itemType = (ItemType) expressions[0].getSingle(event);
					if (itemType == null)
						return new ItemStack(Material.AIR); // default item if none is provided
					return itemType.getRandom();
				}, //</editor-fold>
				itemStack -> Classes.toString(itemStack) + " item particle");

		registerParticle(Particle.SCULK_CHARGE, "[a] sculk charge particle[s] [with [a] roll angle [of] %-number%]",
				//<editor-fold desc="charge lambda" defaultstate="collapsed">
				(event, expressions, parseResult) -> {
					if (expressions[0] == null)
						return 0.0f; // default angle if none is provided
					Number angle = (Number) expressions[0].getSingle(event);
					if (angle == null)
						return 0.0f; // default angle if none is provided
					return (float) Math.toRadians(angle.floatValue());
				}, //</editor-fold>
				angle -> "sculk charge particle with roll angle " + Math.toDegrees(angle) + " degrees");

		registerParticle(Particle.TRAIL, "[a[n]] %color% trail particle moving to[wards] %location% [over [a duration of] %-timespan%]",
				//<editor-fold desc="trail lambda" defaultstate="collapsed">
				(event, expressions, parseResult) -> {
					org.bukkit.Color bukkitColor;
					ch.njol.skript.util.Color color = (ch.njol.skript.util.Color) expressions[0].getSingle(event);
					if (color == null) {
						bukkitColor = org.bukkit.Color.WHITE; // default color if none is provided
					} else {
						bukkitColor = color.asBukkitColor();
					}

					Location targetLocation = (Location) expressions[1].getSingle(event);
					if (targetLocation == null)
						return null;

					Number durationTicks= 20;
					if (expressions[2] != null) {
						Timespan duration = (Timespan) expressions[2].getSingle(event);
						if (duration != null)
							durationTicks = duration.getAs(Timespan.TimePeriod.TICK);
					}

					return new Particle.Trail(targetLocation, bukkitColor, durationTicks.intValue());
				}, //</editor-fold>
				trail -> Classes.toString(ColorRGB.fromBukkitColor(trail.getColor())) +
						" trail particle leading to " + Classes.toString(trail.getTarget()) +
						" over " + trail.getDuration() + " ticks");

		registerParticle(Particle.VIBRATION, "[a] vibration particle moving to[wards] %entity/location% over [a duration of] %timespan%",
				//<editor-fold desc="vibration lambda">
				(event, expressions, parseResult) -> {
					Object target = expressions[0].getSingle(event);
					Vibration.Destination destination;
					if (target instanceof Location location) {
						destination = new Vibration.Destination.BlockDestination(location);
					} else if (target instanceof Entity entity) {
						destination = new Vibration.Destination.EntityDestination(entity);
					} else {
						return null;
					}

					int duration;
					Timespan timespan = (Timespan) expressions[1].getSingle(event);
					if (timespan == null) {
						duration = 20; // default duration of 1 second if none is provided
					} else {
						duration = (int) timespan.getAs(Timespan.TimePeriod.TICK);
					}
					return new Vibration(destination, duration);
				}, //</editor-fold>
				vibration -> "vibration particle moving to " +
						(vibration.getDestination() instanceof Vibration.Destination.BlockDestination blockDestination ?
							Classes.toString(blockDestination.getLocation()) :
							Classes.toString(((Vibration.Destination.EntityDestination) vibration.getDestination()).getEntity())
						) +
						" over " + vibration.getArrivalTime() + " ticks");
	}

	private static void registerDataSerializers() {
		// allow serializing particle data classes
		Variables.yggdrasil.registerSingleClass(Color.class, "particle.color");
		Variables.yggdrasil.registerClassResolver(new SimpleClassSerializer.NonInstantiableClassSerializer<>(Particle.DustOptions.class, "particle.dustoptions") {
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

		Variables.yggdrasil.registerClassResolver(new SimpleClassSerializer.NonInstantiableClassSerializer<>(Particle.DustTransition.class, "particle.dusttransition") {
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

		Variables.yggdrasil.registerClassResolver( new SimpleClassSerializer.NonInstantiableClassSerializer<>(Vibration.class, "particle.vibration") {
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

		Variables.yggdrasil.registerClassResolver(new SimpleClassSerializer.NonInstantiableClassSerializer<>(Particle.Spell.class, "particle.spell") {
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

		Variables.yggdrasil.registerClassResolver( new SimpleClassSerializer.NonInstantiableClassSerializer<>(Particle.Trail.class, "particle.trail") {
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
				int duration = fields.getAndRemovePrimitive("duration", Integer.class);
				if (target == null)
					throw new NotSerializableException("Target cannot be null for Trail");
				if (color == null)
					throw new NotSerializableException("Color cannot be null for Trail");
				return new Particle.Trail(target, color, duration);
			}
		});
	}



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

}
