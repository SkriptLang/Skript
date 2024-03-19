/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.OptionSection;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.util.FileUtils;
import ch.njol.util.Pair;

public class FeatureConfig {

	public final static OptionSection alterSyntax = new OptionSection("alterSyntax");
	public final static OptionSection features = new OptionSection("features");

	private static Map<String, String> alteredPatterns = new HashMap<>();
	private static List<String> disabledClassNames = new ArrayList<>();
	private static List<String> disabledPatterns = new ArrayList<>();

	private static boolean loaded;

	public static Pair<Boolean, String[]> contains(String className, String... patterns) {
		if (disabledClassNames.contains(className)) {
			Skript.debug("Disabling feature '" + className + "' through the Features.sk config.");
			return new Pair<Boolean, String[]>(true, patterns);
		}

		for (int i = 0 ; i < patterns.length ; i++) {
			if (alteredPatterns.containsKey(patterns[i])) {
				Skript.debug("Altering the pattern '" + patterns[i] + "' to '" + alteredPatterns.get(patterns[i]) + "'.");
				patterns[i] = alteredPatterns.get(patterns[i]);
				return new Pair<Boolean, String[]>(false, patterns);
			} else if (disabledPatterns.contains(patterns[i])) {
				Skript.debug("Disabling the feature '" + className + "' which had the exact pattern: '" + patterns[i] + "'.");
				return new Pair<Boolean, String[]>(true, patterns);
			}
		}
		return new Pair<Boolean, String[]>(false, patterns);
	}

	public static void load(File file) {
		if (loaded)
			return;
		loaded = true;
		File featureFile = new File(Skript.getInstance().getDataFolder(), "features.sk");
		Config mc = null;
		if (featureFile.exists()) {
			try {
				mc = new Config(featureFile, false, false, ":");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try (ZipFile zip = new ZipFile(file)) {
				File saveTo = null;
				ZipEntry entry = zip.getEntry("features.sk");
				if (entry != null) {
					File af = new File(Skript.getInstance().getDataFolder(), entry.getName());
					if (!af.exists())
						saveTo = af;
				} if (saveTo != null) {
					try (InputStream in = zip.getInputStream(entry)) {
						FileUtils.save(in, saveTo);
					}
				}
			} catch (IOException e) {
				if (Skript.debug())
					Skript.exception(e);
			} finally {
				featureFile = new File(Skript.getInstance().getDataFolder(), "features.sk");
				try {
					mc = new Config(featureFile, false, false, ":");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (mc != null) {
			mc.load(FeatureConfig.class);
			// When we need to change values in the future of this file, bump the version node.
			if (mc.get("version").isEmpty() || !mc.get("version").equalsIgnoreCase("1")) {
				Skript.warning("Your features.sk config file is outdated. " +
						"Backup any changes you've made, and delete your features.sk in the Skript folder to update it. " +
						"After re-add any nodes you've changed.");
			}
			SectionNode section = (SectionNode) mc.getMainNode().get("features");
			if (section != null && !section.isEmpty()) {
				for (Node node : section) {		
					if (node.getKey().startsWith("Feature")) {
						disabledPatterns.add(mc.get("features", node.getKey()));
					} else {
						disabledClassNames.add(node.getKey());
						String value = mc.get("features", node.getKey());
						if (!value.equalsIgnoreCase("null"))
							disabledPatterns.add(value);
					}
				}
			}
			SectionNode alter = (SectionNode) mc.getMainNode().get("alterSyntax");
			if (alter != null && !alter.isEmpty()) {
				for (Node node : alter) {	
					alteredPatterns.put(node.getKey(), mc.get("alterSyntax", node.getKey()));
				}
			}
		}
	}

	public static void discard() {
		if (loaded) {
			alteredPatterns.clear();
			disabledPatterns.clear();
			disabledClassNames.clear();
		}
	}

}
