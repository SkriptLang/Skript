package org.skriptlang.skript.bukkit.pdc.elements;

import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.pdc.PDCUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;
import java.util.Locale;

@Name("Edit Sub Persistent Data Containers")
@Description({
	"Opens a data container that's stored inside another data container, so you can read or change its data tags.",
	"You give the container a name (a key), and the section opens it for editing. If a container with that name doesn't exist yet, an empty one is created for you. When the section ends, your changes are saved back automatically — you don't need to do anything special to commit them.",
	"Inside the section, use <code>event-pdcs</code> to refer to the container you're editing. From there, you can set, get, or delete data tags on it just like you would on any other container.",
	"This is useful when you want to group related data together under one name instead of dumping everything into the top-level container.",
	"Note that the key is automatically lowercased and namespaced, the same way other persistent data syntax handles keys."
})
@Examples("""
	edit data container "my_stuff" of player's tool:
		set data tag "health" of event-pdc to 1
		set data tag "name" of event-pdc to "Some Name"
	""")
@Since("INSERT VERSION")
@Keywords({"pdc", "persistent data container", "custom data", "nbt"})
public class SecEditContainer extends Section {

	private static class EditContainerEvent extends Event {

		private final PersistentDataContainer container;

		public EditContainerEvent(PersistentDataContainer container) {
			this.container = container;
		}

		public PersistentDataContainer getContainer() {
			return container;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new UnsupportedOperationException();
		}
	}

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(Section.class)
			.addPatterns("edit data container %string% of %chunks/worlds/entities/blocks/itemtypes/offlineplayers/persistentdatacontainers%")
			.supplier(SecEditContainer::new)
			.build());

		// I know you're not supposed to be here, but where SHOULD you go?
		EventValues.registerEventValue(EditContainerEvent.class, PersistentDataContainer.class, EditContainerEvent::getContainer);
	}

	private Expression<String> key;
	private Expression<?> targets;
	private Trigger trigger;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		this.key = (Expression<String>) expressions[0];
		this.targets = expressions[1];
		this.trigger = loadCode(sectionNode, "edit data container section", EditContainerEvent.class);
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		String tagName = this.key.getSingle(event);
		if (tagName == null)
			return super.walk(event, false);

		NamespacedKey key = NamespacedUtils.checkValidationAndSend(tagName.toLowerCase(Locale.ENGLISH), this);
		if (key == null)
			return super.walk(event, false);

		for (Object target : this.targets.getArray(event)) {
			PDCUtils.editPersistentDataContainer(target, pdc -> {
				// Get or create new container
				PersistentDataContainer container = pdc.getOrDefault(key, PersistentDataType.TAG_CONTAINER, pdc.getAdapterContext().newPersistentDataContainer());

				// Walk and edit
				EditContainerEvent editEvent = new EditContainerEvent(container);
				Trigger.walk(this.trigger, editEvent);

				// Pass back
				pdc.set(key, PersistentDataType.TAG_CONTAINER, container);
			});
		}
		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "edit data container " + this.key.toString(event, debug) + " of " + this.targets.toString(event, debug);
	}

}
