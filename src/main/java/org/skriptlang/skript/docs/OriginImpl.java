package org.skriptlang.skript.docs;

import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.docs.Origin.AddonOrigin;

final class OriginImpl {

	public static final class UnknownOrigin implements Origin {

		public UnknownOrigin() { }

		@Override
		public String name() {
			return "unknown";
		}

	}

	public record AddonOriginImpl(SkriptAddon addon) implements AddonOrigin {

		public AddonOriginImpl(SkriptAddon addon) {
			this.addon = addon.unmodifiableView();
		}

	}

}
