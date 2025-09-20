package ch.njol.skript.examples;

import ch.njol.skript.Skript;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ExampleScriptManager {
	private Set<String> installed;
	private File installedFile;

	public ExampleScriptManager() {}

	private void loadInstalled(File scriptsDir) {
		File parent = scriptsDir.getParentFile();
		installedFile = new File(parent == null ? scriptsDir : parent, ".loaded_examples");
		installed = new LinkedHashSet<>();
		if (!installedFile.exists())
			return;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(installedFile), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty())
					installed.add(line);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to load installed examples", e);
		}
	}

	private void flushInstalled() {
		if (installedFile == null || installed == null)
			return;
		File parent = installedFile.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs()) // failed to create directory for installed examples
			return;
		boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		Path installedPath = installedFile.toPath();
		DosFileAttributeView dosView = null;
		if (isWindows && Files.exists(installedPath)) {
			dosView = Files.getFileAttributeView(installedPath, DosFileAttributeView.class);
			if (dosView != null) {
				try {
					if (dosView.readAttributes().isHidden())
						dosView.setHidden(false);
				} catch (IOException ignored) {}
			}
		}

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(installedFile), StandardCharsets.UTF_8))) {
			for (String entry : installed) {
				writer.write(entry);
				writer.newLine();
			}
		} catch (IOException e) { // failed to save installed examples
			return;
		}

		if (isWindows) {
			try {
				if (dosView != null) {
					dosView.setHidden(true);
				} else {
					Files.setAttribute(installedPath, "dos:hidden", true);
				}
			} catch (Exception ignored) {}
		}
	}

	public void installExamples(String plugin, Collection<ExampleScript> scripts, File scriptsDir) {
		loadInstalled(scriptsDir);
		boolean dirty = false;
		File baseDir = new File(scriptsDir, "-examples/" + plugin);
		for (ExampleScript script : scripts) {
			String key = plugin + "/" + script.name();
			if (installed.add(key)) {
				dirty = true;
				File file = new File(baseDir, script.name());
				file.getParentFile().mkdirs();
				try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
					writer.write(script.content());
				} catch (IOException e) {
					throw new RuntimeException("Failed to write example script " + file, e);
				}
			}
		}
		if (dirty)
			flushInstalled();
	}
}
