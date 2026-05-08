package org.skriptlang.skript.docs;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import com.google.common.collect.ImmutableMap;
import org.skriptlang.skript.addon.SkriptAddon;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

class DocumentationAdapterImpl implements DocumentationAdapter {

	private record Scope(String name, Map<String, Object> values) { }

	private final SkriptAddon addon;
	private final BiConsumer<DocumentationAdapter, Documentable> writeHandler;

	private final Deque<Scope> scopes = new ArrayDeque<>();

	private final Map<Documentable, String> idMap = new HashMap<>();

	DocumentationAdapterImpl(SkriptAddon addon, boolean generate) {
		this(addon, (a, b) -> { }, generate);
	}

	DocumentationAdapterImpl(SkriptAddon addon, BiConsumer<DocumentationAdapter, Documentable> writeHandler, boolean generate) {
		this.addon = addon;
		this.writeHandler = writeHandler;
		scopes.push(new Scope("root", new HashMap<>()));
		if (generate) {
			write(addon.syntaxRegistry());
			Classes.write(this);
			write(Skript.experiments());
			write(FunctionRegistry.getRegistry());
		}
	}

	@Override
	public SkriptAddon addon() {
		return addon;
	}

	@Override
	public void write(Documentable documentable) {
		if (documentable.canWrite(this)) {
			documentable.preWrite(this);
			documentable.write(this);
			writeHandler.accept(this, documentable);
			idMap.put(documentable, currentScope());
			documentable.postWrite(this);
		}
	}

	@Override
	public void write(String key, Object value) {
		value = adapt(value);

		if (value instanceof Documentable documentable) {
			enterScope(key);
			write(documentable);
			exitScope();
			return;
		}

		scopes.getFirst().values().put(key, value);
	}

	@Override
	public void enterScope(String key) {
		Map<String, Object> newScopes = new HashMap<>();
		Scope scope = scopes.getFirst();

		// scope conflict resolution
		// append number to end of key
		if (scope.values().containsKey(key)) {
			int id = 2;
			while (scope.values().containsKey(key + "-" + id)) {
				id++;
			}
			key = key + "-" + id;
		}

		scope.values().put(key, newScopes);
		scopes.push(new Scope(key, newScopes));
	}

	@Override
	public void exitScope() {
		var scope = scopes.pop();
		if (scope.values().isEmpty()) {
			scopes.getFirst().values().remove(scope.name);
		}
	}

	@Override
	public String currentScope() {
		return scopes.getFirst().name();
	}

	@Override
	public Map<String, Object> dataMap() {
		if (scopes.size() != 1) {
			throw new SkriptAPIException("Attempted to access data map before all scopes have been exited");
		}
		//noinspection unchecked
		return (Map<String, Object>) filter(resolveReferences(scopes.peek().values()));
	}

	private record ReferenceImpl(Documentable referenced) implements Reference { }

	@Override
	public Reference reference(Documentable documentable) {
		return new ReferenceImpl(documentable);
	}

	private Object resolveReferences(Object value) {
		return switch (value) {
			case Reference reference -> {
				var builder = ImmutableMap.builder();
				builder.put("id", idMap.get(reference.referenced()));
				if (reference.referenced() instanceof DocumentationDocumentable documentationDocumentable) {
					Documentation documentation = documentationDocumentable.documentation();
					builder.put("name", documentation.name());
				}
				yield builder.build();
			}
			case Collection<?> collection -> collection.stream()
				.map(this::resolveReferences)
				.toList();
			case Map<?, ?> map -> map.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> resolveReferences(entry.getValue())));
			case null, default -> value;
		};
	}

	private Object adapt(Object value) {
		return switch (value) {
			case Class<?> clazz -> {
				ClassInfo<?> classInfo = Classes.getSuperClassInfo(Utils.getComponentType(clazz));
				if (Documentation.isNoDocs(classInfo.documentation())) {
					// try to use docs for superclass
					Class<?> superClass = classInfo.getC().getSuperclass();
					if (superClass != null) {
						yield adapt(superClass);
					}
					// if it doesn't have a superclass, check interfaces
					for (Class<?> clazzInterface : classInfo.getC().getInterfaces()) {
						classInfo = Classes.getExactClassInfo(clazzInterface);
						if (classInfo != null && !Documentation.isNoDocs(classInfo.documentation())) {
							break;
						}
					}
					// otherwise, fallback to Object
					if (classInfo == null) {
						classInfo = Classes.getExactClassInfo(Object.class);
					}
				}
				yield reference(classInfo);
			}
			case Collection<?> collection -> collection.stream()
				.map(element -> {
					Object adapted = adapt(element);
					if (adapted instanceof Documentable documentable) {
						var adapter = new DocumentationAdapterImpl(addon, false);
						adapter.write(documentable);
						adapted = adapter.dataMap();
					}
					return adapted;
				})
				.toList();
			case Map<?, ?> map -> map.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> {
					Object adapted = adapt(entry.getValue());
					if (adapted instanceof Documentable documentable) {
						var adapter = new DocumentationAdapterImpl(addon, false);
						adapter.write(documentable);
						adapted = adapter.dataMap();
					}
					return adapted;
				}));
			case null, default -> value;
		};
	}

	private Object filter(Object value) {
		// TODO this is not a good approach
		return switch (value) {
			case Collection<?> collection -> collection.stream()
				.map(this::filter)
				.filter(Objects::nonNull)
				.toList();
			case Map<?, ?> map -> {
				if (map.containsKey("origin")) {
					//noinspection unchecked
					if (!((Map<String, Object>) (map.get("origin"))).get("name").equals(addon().name())) {
						yield null;
					}
				}
				yield map.entrySet()
					.stream()
					.map(entry -> {
						Object filteredValue = filter(entry.getValue());
						if (filteredValue == null) {
							return null;
						}
						return Map.entry(entry.getKey(), filteredValue);
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			}
			case null, default -> value;
		};
	}

}
