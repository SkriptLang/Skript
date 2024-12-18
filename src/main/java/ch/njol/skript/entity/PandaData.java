package ch.njol.skript.entity;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Language;

public class PandaData extends EntityData<Panda> {
	
	static {
		if (Skript.isRunningMinecraft(1, 14))
			EntityData.register(PandaData.class, "panda", Panda.class, "panda");
	}
	
	@Nullable
	private Gene mainGene = null, hiddenGene = null;
	
	public PandaData() {}
	
	public PandaData(@Nullable Gene mainGene, @Nullable Gene hiddenGene) {
		this.mainGene = mainGene;
		this.hiddenGene = hiddenGene;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs[0] != null)
			mainGene = (Gene) exprs[0].getSingle();
		if (exprs[1] != null)
			hiddenGene = (Gene) exprs[1].getSingle();
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Panda> c, @Nullable Panda panda) {
		if (panda != null) {
			mainGene = panda.getMainGene();
			hiddenGene = panda.getHiddenGene();
		}
		return true;
	}
	
	@Override
	public void set(Panda entity) {
		Gene gen = mainGene;
		if (gen == null)
			gen = Gene.values()[ThreadLocalRandom.current().nextInt(0, 7)];
		assert gen != null;
		entity.setMainGene(gen);
		entity.setHiddenGene(hiddenGene != null ? hiddenGene : gen);
	}
	
	@Override
	protected boolean match(Panda entity) {
		if (hiddenGene != null) {
			if(mainGene != null)
				return mainGene == entity.getMainGene() && hiddenGene == entity.getHiddenGene();
			else
				return hiddenGene == entity.getHiddenGene();
		} else {
			if(mainGene != null)
				return mainGene == entity.getMainGene();
			else
				return true;
		}
	}
	
	@Override
	public Class<? extends Panda> getType() {
		return Panda.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new PandaData(mainGene, hiddenGene);
	}
	
	@Override
	protected int hashCode_i() {
		int prime = 7;
		int result = 0;
		result = result * prime + (mainGene != null ? mainGene.hashCode() : 0);
		result = result * prime + (hiddenGene != null ? hiddenGene.hashCode() : 0);
		return result;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> data) {
		if (!(data instanceof PandaData))
			return false;
		PandaData d = (PandaData) data;
		return d.mainGene == mainGene && d.hiddenGene == hiddenGene;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		if (!(data instanceof PandaData))
			return false;
		PandaData d = (PandaData) data;
		return (mainGene == null || mainGene == d.mainGene) && (hiddenGene == null || hiddenGene == d.hiddenGene);
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
