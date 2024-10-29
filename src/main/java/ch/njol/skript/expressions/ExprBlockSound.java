package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Name("Block Sound")
@Description({
	"Gets the sound that a given block, blockdata, or itemtype will use in a specific scenario.",
	"This will return a string in the form of \"SOUND_EXAMPLE\", which can be used in the play sound syntax.",
	"",
	"Check out <a href=\"https://minecraft.wiki/w/Sounds.json\">this website</a> for a list of sounds in Minecraft, " +
		"or <a href=\"https://minecraft.wiki/w/Sound\">this one</a> to go to the Sounds wiki page."
})
@Examples({
	"play sound (break sound of dirt) at all players",
	"set {_sounds::*} to place sounds of dirt, grass block, blue wool and stone"
})
@Since("INSERT VERSION")
public class ExprBlockSound extends SimpleExpression<String> {

	public enum SoundType {
		BREAK {
			@Override
			public Sound getSound(SoundGroup group) {
				return group.getBreakSound();
			}
		},

		FALL {
			@Override
			public Sound getSound(SoundGroup group) {
				return group.getFallSound();
			}
		},

		HIT {
			@Override
			public Sound getSound(SoundGroup group) {
				return group.getHitSound();
			}
		},

		PLACE {
			@Override
			public Sound getSound(SoundGroup group) {
				return group.getPlaceSound();
			}
		},

		STEP {
			@Override
			public Sound getSound(SoundGroup group) {
				return group.getStepSound();
			}
		};

		public abstract @Nullable Sound getSound(SoundGroup group);
	}

	static {
		SimplePropertyExpression.register(ExprBlockSound.class, String.class, "(1:break|2:fall|3:hit|4:place|5:step) sound[s]", "blocks/blockdatas/itemtypes");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private SoundType soundType;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> objects;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		soundType = SoundType.values()[parseResult.mark - 1];
		objects = exprs[0];
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		return objects.stream(event)
			.map(this::convertAndGetSound)
			.filter(Objects::nonNull)
			.distinct()
			.map(Sound::name)
			.toArray(String[]::new);
	}

	private @Nullable SoundGroup getSoundGroup(Object object) {
		if (object instanceof Block block) {
			return block.getBlockData().getSoundGroup();
		} else if (object instanceof BlockData data) {
			return data.getSoundGroup();
		} else if (object instanceof ItemType item) {
			if (item.hasBlock())
				return item.getMaterial().createBlockData().getSoundGroup();
		}
		return null;
	}

	private @Nullable Sound convertAndGetSound(Object object) {
		SoundGroup group = getSoundGroup(object);
		if (group == null)
			return null;
		return this.soundType.getSound(group);
	}

	@Override
	public boolean isSingle() {
		return objects.isSingle();
	}

	@Override
	public @NotNull Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		return this.soundType.name().toLowerCase() + " sound of " + objects.toString(event, debug);
	}

}
