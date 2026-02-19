package org.skriptlang.skript.docs;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.addon.SkriptAddon;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class DocumentationAdapterImpl implements DocumentationAdapter {

	private record Scope(String name, Map<String, Object> values) { }

	private final Deque<Scope> scopes = new ArrayDeque<>();

	DocumentationAdapterImpl() {
		scopes.push(new Scope("root", new HashMap<>()));
	}

	DocumentationAdapterImpl(SkriptAddon addon) {
		this();
		addon.syntaxRegistry().write(this);
	}

	@Override
	public void write(String key, Object value) {
		assert !scopes.isEmpty();

		value = adapt(value);

		if (value instanceof Documentable documentable) {
			enterScope(key);
			write(documentable);
			exitScope();
			return;
		}

		scopes.peek().values().put(key, value);
	}

	@Override
	public void enterScope(String key) {
		assert !scopes.isEmpty();
		Map<String, Object> newScopes = new HashMap<>();
		scopes.peek().values().put(key, newScopes);
		scopes.push(new Scope(key, newScopes));
	}

	@Override
	public void exitScope() {
		assert scopes.size() > 1;
		scopes.pop();
	}

	@Override
	public String currentScope() {
		assert !scopes.isEmpty();
		return scopes.peek().name();
	}

	@Override
	public Map<String, Object> dataMap() {
		assert scopes.size() == 1;
		return scopes.peek().values();
	}

	private Object adapt(Object value) {
		return switch (value) {
			case Class<?> clazz -> (Documentable) adapter -> {
				ClassInfo<?> classInfo = Classes.getSuperClassInfo(clazz);
				Documentation documentation = classInfo.documentation();
				adapter.write("id", documentation.id() == null ? classInfo.getCodeName() : documentation.id());
				adapter.write("name", documentation.name());
			};
			case Collection<?> collection -> collection.stream()
				.map(this::adapt)
				.toList();
			case Map<?, ?> map -> map.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> {
					Object adapted = adapt(entry.getValue());
					if (adapted instanceof Documentable documentable) {
						var adapter = new DocumentationAdapterImpl();
						adapter.write(documentable);
						adapted = adapter.dataMap();
					}
					return adapted;
				}));
			default -> value;
		};
	}

}
