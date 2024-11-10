package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.SoundUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Name("Entity Sound")
@Description("Gets the sound that a given entity will make in a specific scenario.")
@Examples({
	"play sound (hurt sound of player) at player",
	"set {_sounds::*} to death sounds of (all mobs in radius 10 of player)"
})
@Since("INSERT VERSION")
@RequiredPlugins("Spigot 1.19.2+")
public class ExprEntitySound extends SimpleExpression<String> {

	public enum SoundType {
		DAMAGE {
			@Override
			public @Nullable Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
				return entity.getHurtSound();
			}
		},
		DEATH {
			@Override
			public @Nullable Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
				return entity.getDeathSound();
			}
		},
		FALL {
			@Override
			public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
				if (height != -1)
					return entity.getFallDamageSound(height);
				else
					return bigOrSpeedy ? entity.getFallDamageSoundBig() : entity.getFallDamageSoundSmall();
			}
		},
		SWIM {
			@Override
			public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
				return entity.getSwimSound();
			}
		},
		SPLASH {
			@Override
			public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
				return bigOrSpeedy ? entity.getSwimHighSpeedSplashSound() : entity.getSwimSplashSound();
			}
		},
		EAT {
			@Override
			public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
				return entity.getEatingSound(item);
			}
		},
		DRINK {
			@Override
			public Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
				return entity.getDrinkingSound(item);
			}
		},
		AMBIENT {
			@Override
			public @Nullable Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy) {
				return entity instanceof Mob mob ? mob.getAmbientSound() : null;
			}
		};

		public abstract @Nullable Sound getSound(LivingEntity entity, int height, ItemStack item, boolean bigOrSpeedy);
	}

	private static final Patterns<SoundType> patterns = new Patterns<>(new Object[][]{
		{"[the] (damage|hurt) sound[s] of %livingentities%", SoundType.DAMAGE},
		{"%livingentities%'[s] (damage|hurt) sound[s]", SoundType.DAMAGE},

		{"[the] death sound[s] of %livingentities%", SoundType.DEATH},
		{"%livingentities%'[s] death sound[s]", SoundType.DEATH},

		{"[the] [high:(tall|high)|(low|normal)] fall damage sound[s] [from [[a] height [of]] %-number%] of %livingentities%", SoundType.FALL},
		{"%livingentities%'[s] [high:(tall|high)|low:(low|normal)] fall [damage] sound[s] [from [[a] height [of]] %-number%]", SoundType.FALL},

		{"[the] swim[ming] sound[s] of %livingentities%", SoundType.SWIM},
		{"%livingentities%'[s] swim[ming] sound[s]", SoundType.SWIM},

		{"[the] [fast:(fast|speedy)] splash sound[s] of %livingentities%", SoundType.SPLASH},
		{"%livingentities%'[s] [fast:(fast|speedy)] splash sound[s]", SoundType.SPLASH},

		{"[the] eat[ing] sound[s] of %livingentities% [(with|using|[while] eating [a]) %-itemtype%]", SoundType.EAT},
		{"%livingentities%'[s] eat[ing] sound[s]", SoundType.EAT},

		{"[the] drink[ing] sound[s] of %livingentities% [(with|using|[while] drinking [a]) %-itemtype%]", SoundType.DRINK},
		{"%livingentities%'[s] drink[ing] sound[s]", SoundType.DRINK},

		{"[the] ambient sound[s] of %livingentities%", SoundType.AMBIENT},
		{"%livingentities%'[s] ambient sound[s]", SoundType.AMBIENT}
	});

	static {
		if (Skript.methodExists(LivingEntity.class, "getDeathSound"))
			Skript.registerExpression(ExprEntitySound.class, String.class, ExpressionType.COMBINED, patterns.getPatterns());
	}

	private boolean bigOrSpeedy;
	private SoundType soundType;
	private Expression<Number> height;
	private Expression<LivingEntity> entities;
	private Expression<ItemType> item;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		soundType = patterns.getInfo(matchedPattern);
		bigOrSpeedy = parseResult.hasTag("high") || parseResult.hasTag("fast");
		if (soundType == SoundType.FALL)
			height = (Expression<Number>) exprs[0];
		if (soundType == SoundType.EAT || soundType == SoundType.DRINK)
			item = (Expression<ItemType>) exprs[1];
		entities = (Expression<LivingEntity>) ((soundType == SoundType.FALL) ? exprs[1] : exprs[0]);
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
        int height = this.height == null ? -1 : this.height.getOptionalSingle(event).orElse(-1).intValue();

		ItemStack defaultItem = new ItemStack(soundType == SoundType.EAT ? Material.COOKED_BEEF : Material.POTION);
        ItemStack item = this.item == null ? defaultItem : this.item.getOptionalSingle(event).map(ItemType::getRandom).orElse(defaultItem);

		return entities.stream(event)
			.map(entity -> soundType.getSound(entity, height, item, bigOrSpeedy))
			.filter(Objects::nonNull)
			.distinct()
			.map(sound -> SoundUtils.getKey(sound).getKey())
			.toArray(String[]::new);
	}

	@Override
	public boolean isSingle() {
		return entities.isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String sound = "unknown";
		switch (soundType) {
			case DAMAGE, DEATH, SWIM, AMBIENT -> sound = soundType.name().toLowerCase();
			case FALL -> {
                if (this.height == null) {
					sound = bigOrSpeedy ? "high fall damage" : "normal fall damage";
				} else {
					sound = "fall damage from a height of " + this.height.toString(event, debug);
				}
			}
			case SPLASH -> sound = bigOrSpeedy ? "speedy splash" : "splash";
			case EAT, DRINK -> {
				String action = soundType == SoundType.EAT ? "eating" : "drinking";
                if (this.item == null) {
					sound = action;
				} else {
					sound = action + " " + this.item.toString(event, debug);
				}
			}
		}
		return sound + " sound of " + entities.toString(event, debug);
	}

}
