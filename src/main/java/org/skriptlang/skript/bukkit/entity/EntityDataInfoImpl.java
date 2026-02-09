package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.localization.Noun;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.bukkit.entity.EntityData.EntityDataPatterns;
import org.skriptlang.skript.bukkit.entity.EntityData.PatternGroup;
import org.skriptlang.skript.bukkit.entity.EntityDataInfo.Builder;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.skriptlang.skript.bukkit.entity.EntityData.m_age_pattern;

final class EntityDataInfoImpl<B extends Builder<B, Data, E>, Data extends EntityData<E>, E extends Entity>
	implements EntityDataInfo<Data, E> {

	private final Class<Data> dataClass;
	private final SequencedCollection<String> patterns;

	private final String dataName;
	private final EntityDataPatterns<?> dataPatterns;
	private final int defaultIndex;
	private final PatternGroup<?> defaultGroup;
	private final @Nullable EntityType entityType;
	private final Class<? extends E> entityClass;

	private final Map<Integer, PatternGroup<?>> matchedPatternToGroup = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> matchedPatternToGroupPattern = new ConcurrentHashMap<>();

	private final SyntaxInfo<Data> defaultInfo;

	EntityDataInfoImpl(
		SyntaxInfo<Data> defaultInfo,
		Class<Data> dataClass,
		String dataName,
		EntityDataPatterns<?> dataPatterns,
		int defaultIndex,
		@Nullable EntityType entityType,
		Class<? extends E> entityClass
	) {
		assert entityClass != null && dataPatterns.getPatternGroups().length != 0;
		assert defaultIndex < dataPatterns.getPatternGroups().length;
		this.defaultInfo = defaultInfo;
		this.dataClass = dataClass;
		this.dataName = dataName;
		this.dataPatterns = dataPatterns;
		this.defaultIndex = defaultIndex;
		this.defaultGroup = dataPatterns.getPatternGroup(defaultIndex);
		if (entityType == null) {
			this.entityType = EntityUtils.toBukkitEntityType(entityClass);
		} else {
			this.entityType = entityType;
		}
		this.entityClass = entityClass;

		List<String> allPatterns = new ArrayList<>();
		for (PatternGroup<?> group : dataPatterns.getPatternGroups()) {
			int patternCount = 0;
			for (String pattern : group.patterns()) {
				pattern = pattern.replace("<age>", m_age_pattern.toString());
				// correlates '#init.matchedPattern' to 'PatternGroup'
				matchedPatternToGroup.put(allPatterns.size(), group);
				// correlates '#init.matchedPattern' to pattern index in 'PatternGroup'
				matchedPatternToGroupPattern.put(allPatterns.size(), patternCount);
				allPatterns.add(pattern);
				patternCount++;
			}
		}
		this.patterns = allPatterns;
	}

	@Override
	public PatternGroup<?> groupFromMatchedPattern(int matchedPattern) {
		return matchedPatternToGroup.get(matchedPattern);
	}

	@Override
	public int matchedGroupPattern(int matchedPattern) {
		return matchedPatternToGroupPattern.get(matchedPattern);
	}

	@Override
	public @Unmodifiable SequencedCollection<String> patterns() {
		return patterns;
	}

	@Override
	public String dataName() {
		return dataName;
	}

	@Override
	public EntityDataPatterns<?> dataPatterns() {
		return dataPatterns;
	}

	@Override
	public PatternGroup<?> defaultGroup() {
		return defaultGroup;
	}

	@Override
	public int defaultGroupIndex() {
		return defaultIndex;
	}

	@Override
	public SequencedCollection<Noun> names() {
		return dataPatterns.getNames();
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
	public Origin origin() {
		return defaultInfo.origin();
	}

	@Override
	public Class<Data> type() {
		return dataClass;
	}

	@Override
	public Priority priority() {
		return defaultInfo.priority();
	}

	@Override
	public Data instance() {
		return defaultInfo.instance();
	}

	@Override
	public Builder<B, Data, E> toBuilder() {
		Builder<B, Data, E> builder = new BuilderImpl<>(type(), dataName);
		defaultInfo.toBuilder().applyTo(builder);
		builder.dataPatterns(dataPatterns)
			.defaultGroupIndex(defaultIndex)
			.entityType(entityType)
			.entityClass(entityClass);
		return builder;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EntityDataInfo<?,?> other))
			return false;
		if (!dataClass.equals(other.type()))
			return false;
		if (!dataName.equals(other.dataName()))
			return false;
		if (!dataPatterns.equals(other.dataPatterns()))
			return false;
		if (defaultIndex != other.defaultGroupIndex())
			return false;
		return entityClass.equals(other.entityClass());
	}

	@SuppressWarnings("unchecked")
	final static class BuilderImpl<B extends Builder<B, Data, E>, Data extends EntityData<E>, E extends Entity>
		implements Builder<B, Data, E> {

		private final Class<Data> dataClass;

		private final String dataName;
		private EntityDataPatterns<?> dataPatterns;
		private int defaultGroupIndex = 0;
		private @Nullable EntityType entityType;
		private Class<? extends E> entityClass;

		private final SyntaxInfo.Builder<?, Data> defaultBuilder;

		BuilderImpl(Class<Data> dataClass, String dataName) {
			this.dataClass = dataClass;
			this.dataName = dataName;
			this.defaultBuilder = SyntaxInfo.builder(dataClass)
				.addPattern("");
		}

		@Override
		public B dataPatterns(EntityDataPatterns<?> dataPatterns) {
			this.dataPatterns = dataPatterns;
			return (B) this;
		}

		@Override
		public B defaultGroupIndex(int index) {
			this.defaultGroupIndex = index;
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
		public B origin(Origin origin) {
			defaultBuilder.origin(origin);
			return (B) this;
		}

		@Override
		public B supplier(@Nullable Supplier<Data> supplier) {
			defaultBuilder.supplier(supplier);
			return (B) this;
		}

		@Override
		public B priority(@Nullable Priority priority) {
			defaultBuilder.priority(priority);
			return (B) this;
		}

		@Override
		public EntityDataInfo<Data, E> build() {
			return new EntityDataInfoImpl<>(
				defaultBuilder.build(),
				dataClass,
				dataName,
				dataPatterns,
				defaultGroupIndex,
				entityType,
				entityClass
			);
		}

		@Override
		public void applyTo(SyntaxInfo.Builder<?, ?> builder) {
			defaultBuilder.applyTo(builder);
			//noinspection rawtypes - Should be safe, generics will not influence this
			if (builder instanceof EntityDataInfo.Builder other) {
				other.dataPatterns(dataPatterns)
					.defaultGroupIndex(defaultGroupIndex)
					.entityType(entityType)
					.entityClass(entityClass);
			}
		}
	}

}

