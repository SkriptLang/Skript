package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Language.LanguageListenerPriority;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.localization.Noun;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.bukkit.entity.EntityDataInfo.Builder;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.skriptlang.skript.bukkit.entity.EntityData.LANGUAGE_NODE;
import static org.skriptlang.skript.bukkit.entity.EntityData.m_age_pattern;

final class EntityDataInfoImpl<B extends Builder<B, Data, E>, Data extends EntityData<E>, E extends Entity>
	implements EntityDataInfo<Data, E>, LanguageChangeListener {

	private static Priority estimatePriority(Collection<String> patterns) {
		Priority priority = SyntaxInfo.SIMPLE;
		for (String pattern : patterns) {
			char[] chars = pattern.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				if (chars[i] == '%') {
					if (i > 0 && chars[i - 1] == '\\') { // skip escaped percentages
						continue;
					}
					// "%thing% %thing%" or "%thing% [%thing%]"
					if ((i > 1 && chars[i - 2] == '%') || (i > 2 && chars[i - 3] == '%')) {
						return SyntaxInfo.PATTERN_MATCHES_EVERYTHING;
					}
					priority = SyntaxInfo.COMBINED;
				}
			}
		}
		return priority;
	}

	private final Origin origin;
	private final Class<Data> dataClass;
	private final @Nullable Supplier<Data> supplier;
	private @Nullable Priority priority;
	private SequencedCollection<String> patterns = new ArrayList<>();

	private final String dataName;
	private final SequencedCollection<String> codeNames = new ArrayList<>();
	private final String defaultCodeName;
	private final int defaultIndex;
	private final @Nullable EntityType entityType;
	private final Class<? extends E> entityClass;

	private final Noun[] names;
	private final Map<String, Integer> codeNamePlacements = new ConcurrentHashMap<>();
	private final Map<Integer, String> matchedPatternToCodeName = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> matchedPatternToCodeNamePattern = new ConcurrentHashMap<>();

	EntityDataInfoImpl(
		Origin origin,
		Class<Data> dataClass,
		@Nullable Supplier<Data> supplier,
		@Nullable Priority priority,
		String dataName,
		SequencedCollection<String> codeNames,
		int defaultIndex,
		@Nullable EntityType entityType,
		Class<? extends E> entityClass
	) {
		assert entityClass != null && !codeNames.isEmpty();
		assert defaultIndex < codeNames.size();
		this.origin = origin;
		this.dataClass = dataClass;
		this.supplier = supplier;
		this.priority = priority;
		this.dataName = dataName;
		this.codeNames.addAll(codeNames);
		this.defaultCodeName = new ArrayList<>(codeNames).get(defaultIndex);
		this.defaultIndex = defaultIndex;
		if (entityType == null) {
			this.entityType = EntityUtils.toBukkitEntityType(entityClass);
		} else {
			this.entityType = entityType;
		}
		this.entityClass = entityClass;

		this.names = new Noun[codeNames.size()];
		int count = 0;
		for (String codeName : codeNames) {
			names[count] = new Noun(LANGUAGE_NODE + "." + codeName + ".name");
			codeNamePlacements.put(codeName, count);
			count++;
		}

		Language.addListener(this, LanguageListenerPriority.LATEST);
	}

	@Override
	public void onLanguageChange() {
		List<String> allPatterns = new ArrayList<>();
		matchedPatternToCodeName.clear();
		matchedPatternToCodeNamePattern.clear();
		for (String codeName : codeNames) {
			if (Language.keyExistsDefault(LANGUAGE_NODE + "." + codeName + ".pattern")) {
				String pattern = Language.get(LANGUAGE_NODE + "." + codeName + ".pattern")
					.replace("<age>", m_age_pattern.toString());
				matchedPatternToCodeName.put(allPatterns.size(), codeName);
				matchedPatternToCodeNamePattern.put(allPatterns.size(), 0);
				allPatterns.add(pattern);
			} else if (!Language.keyExistsDefault(LANGUAGE_NODE + "." + codeName + ".patterns.0")) {
				throw new IllegalStateException("lang section for '" + codeName + "' should contain 'pattern' or a 'patterns' section");
			} else {
				int multiCount = 0;
				while (Language.keyExistsDefault(LANGUAGE_NODE + "." + codeName + ".patterns." + multiCount)) {
					String pattern = Language.get(LANGUAGE_NODE + "." + codeName + ".patterns." + multiCount)
						.replace("<age>", m_age_pattern.toString());
					// correlates '#init.matchedPattern' to 'codeName'
					matchedPatternToCodeName.put(allPatterns.size(), codeName);
					// correlates '#init.matchedPattern' to pattern in code name
					matchedPatternToCodeNamePattern.put(allPatterns.size(), multiCount);
					allPatterns.add(pattern);
					multiCount++;
				}
			}
		}
		patterns = allPatterns;
		if (this.priority == null) {
			this.priority = estimatePriority(patterns);
		}
	}

	@Override
	public String dataName() {
		return dataName;
	}

	@Override
	public Noun[] names() {
		return names;
	}

	@Override
	public int codeNamePlacement(String codeName) {
		return codeNamePlacements.get(codeName);
	}

	@Override
	public String codeNameFromMatchedPattern(int matchedPattern) {
		return matchedPatternToCodeName.get(matchedPattern);
	}

	@Override
	public int matchedCodeNamePattern(int matchedPattern) {
		return matchedPatternToCodeNamePattern.get(matchedPattern);
	}

	@Override
	public Origin origin() {
		return origin;
	}

	@Override
	public Class<Data> type() {
		return dataClass;
	}

	@Override
	public Data instance() {
		try {
			return supplier == null ? dataClass.getDeclaredConstructor().newInstance() : supplier.get();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public @Unmodifiable SequencedCollection<String> patterns() {
		return patterns;
	}

	@Override
	public Priority priority() {
		return priority;
	}

	@Override
	public SequencedCollection<String> codeNames() {
		return codeNames;
	}

	@Override
	public String defaultCodeName() {
		return defaultCodeName;
	}

	@Override
	public int defaultCodeNameIndex() {
		return defaultIndex;
	}

	@Override
	public @Nullable EntityType entityType() {
		return entityType;
	}

	@Override
	public Class<? extends E> entityClass() {
		return entityClass;
	}

	@Override
	public Builder<B, Data, E> toBuilder() {
		//noinspection unchecked
		return (Builder<B, Data, E>) new BuilderImpl<>(dataClass, dataName)
			.origin(origin)
			.supplier(supplier)
			.priority(priority)
			.addCodeNames(codeNames)
			.defaultCodeName(defaultIndex)
			.entityType(entityType)
			.entityClass(entityClass);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EntityDataInfo<?,?> other))
			return false;
		if (!dataClass.equals(other.type()))
			return false;
		if (!dataName.equals(other.dataName()))
			return false;
		if (!codeNames.equals(other.codeNames()))
			return false;
		if (defaultIndex != other.defaultCodeNameIndex())
			return false;
		return entityClass.equals(other.entityClass());
	}

	@SuppressWarnings("unchecked")
	final static class BuilderImpl<B extends Builder<B, Data, E>, Data extends EntityData<E>, E extends Entity>
		implements Builder<B, Data, E> {

		private final Class<Data> dataClass;
		private Origin origin = Origin.UNKNOWN;
		private @Nullable Supplier<Data> supplier = null;
		private @Nullable Priority priority;

		private final String dataName;
		private final SequencedCollection<String> codeNames = new ArrayList<>();
		private int defaultCodeName = 0;
		private @Nullable EntityType entityType;
		private Class<? extends E> entityClass;

		BuilderImpl(Class<Data> dataClass, String dataName) {
			this.dataClass = dataClass;
			this.dataName = dataName;
		}

		@Override
		public B origin(Origin origin) {
			this.origin = origin;
			return (B) this;
		}

		@Override
		public B supplier(@Nullable Supplier<Data> supplier) {
			this.supplier = supplier;
			return (B) this;
		}

		@Override
		public B priority(@Nullable Priority priority) {
			this.priority = priority;
			return (B) this;
		}

		@Override
		public B addCodeName(String codeName) {
			this.codeNames.add(codeName);
			return (B) this;
		}

		@Override
		public B addCodeNames(String... codeNames) {
			this.codeNames.addAll(Arrays.stream(codeNames).toList());
			return (B) this;
		}

		@Override
		public B addCodeNames(Collection<String> codeNames) {
			this.codeNames.addAll(codeNames);
			return (B) this;
		}

		@Override
		public B defaultCodeName(int index) {
			this.defaultCodeName = index;
			return (B) this;
		}

		@Override
		public B entityType(@Nullable EntityType entityType) {
			this.entityType = entityType;
			return (B) this;
		}

		@Override
		public B entityClass(Class<? extends E> entityClass) {
			this.entityClass = entityClass;
			return (B) this;
		}

		@Override
		public EntityDataInfo<Data, E> build() {
			return new EntityDataInfoImpl<>(
				origin,
				dataClass,
				supplier,
				priority,
				dataName,
				codeNames,
				defaultCodeName,
				entityType,
				entityClass
			);
		}

		@Override
		public void applyTo(SyntaxInfo.Builder<?, ?> builder) {

		}
	}

}

