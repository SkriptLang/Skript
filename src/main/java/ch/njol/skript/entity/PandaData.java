package ch.njol.skript.entity;

import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PandaData extends EntityData<Panda> {
	
	private static final Gene[] GENES = Gene.values();

	static {
		EntityData.register(PandaData.class, "panda", Panda.class, "panda");

		Classes.registerClass(new EnumClassInfo<>(Gene.class, "gene", "genes")
			.user("(panda )?genes?")
			.name("Gene")
			.description("Represents a Panda's main or hidden gene. " +
				"See <a href='https://minecraft.wiki/w/Panda#Genetics'>genetics</a> for more info.")
			.since("2.4")
			.requiredPlugins("Minecraft 1.14 or newer"));
	}

	private @Nullable Gene mainGene = null;
	private @Nullable Gene hiddenGene = null;
	
	public PandaData() {}
	
	public PandaData(@Nullable Gene mainGene, @Nullable Gene hiddenGene) {
		this.mainGene = mainGene;
		this.hiddenGene = hiddenGene;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (exprs[0] != null) {
			mainGene = (Gene) exprs[0].getSingle();
			if (exprs[1] != null)
				hiddenGene = (Gene) exprs[1].getSingle();
		}
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Panda> entityClass, @Nullable Panda panda) {
		if (panda != null) {
			mainGene = panda.getMainGene();
			hiddenGene = panda.getHiddenGene();
		}
		return true;
	}
	
	@Override
	public void set(Panda panda) {
		Gene gene = mainGene;
		if (gene == null)
			gene = CollectionUtils.getRandom(GENES);
		assert gene != null;
		panda.setMainGene(gene);
		panda.setHiddenGene(hiddenGene != null ? hiddenGene : gene);
	}
	
	@Override
	protected boolean match(Panda panda) {
		if (!dataMatch(mainGene, panda.getMainGene()))
			return false;
		return dataMatch(hiddenGene, panda.getHiddenGene());
	}
	
	@Override
	public Class<? extends Panda> getType() {
		return Panda.class;
	}
	
	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new PandaData();
	}
	
	@Override
	protected int hashCode_i() {
		int prime = 7;
		int result = 0;
		result = result * prime + Objects.hashCode(mainGene);
		result = result * prime + Objects.hashCode(hiddenGene);
		return result;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof PandaData other))
			return false;
		return other.mainGene == mainGene && other.hiddenGene == hiddenGene;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof PandaData other))
			return false;
		if (!dataMatch(mainGene, other.mainGene))
			return false;
		return dataMatch(hiddenGene, other.hiddenGene);
	}
	
	@Override
	public String toString(int flags) {
		StringBuilder builder = new StringBuilder();
		if (mainGene != null)
			builder.append(Language.getList("genes." + mainGene.name())[0]).append(" ");
		if (hiddenGene != null && hiddenGene != mainGene)
			builder.append(Language.getList("genes." + hiddenGene.name())[0]).append(" ");
		builder.append(Language.get("panda"));
		return builder.toString();
	}
	
}
