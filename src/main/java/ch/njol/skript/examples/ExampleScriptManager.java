package ch.njol.skript.examples;

import ch.njol.skript.Skript;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ExampleScriptManager {
	private Set<String> installed;
	private File installedFile;

	public ExampleScriptManager() {
	}

	private void loadInstalled(File scriptsDir) {
		installedFile = new File(scriptsDir.getParentFile(), ".loaded_examples");
		installed = new HashSet<>();
		if (!installedFile.exists())
			return;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(installedFile), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				installed.add(line);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to load installed examples", e);
		}
	}

	private void flushInstalled() {
		if (installedFile == null)
			return;
		File parent = installedFile.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs()) {
			Skript.warning("Failed to create directory for installed examples at " + parent.getAbsolutePath());
			return;
		}
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(installedFile), StandardCharsets.UTF_8))) {
			for (String entry : installed) {
				writer.write(entry);
				writer.newLine();
			}
		} catch (IOException e) {
			Skript.warning("Failed to save installed examples to " + installedFile + ": " + e.getMessage());
			return;
		}
		if (System.getProperty("os.name").startsWith("Windows")) {
			try {
				Files.setAttribute(installedFile.toPath(), "dos:hidden", true);
			} catch (Exception ignored) {}
		}
	}

	public void installExamples(String plugin, Collection<ExampleScript> scripts, File scriptsDir) {
		if (installed == null)
			loadInstalled(scriptsDir);
		File baseDir = new File(scriptsDir, "-examples/" + plugin);
		for (ExampleScript script : scripts) {
			String key = plugin + "/" + script.name();
			if (installed.add(key)) {
				File file = new File(baseDir, script.name());
				file.getParentFile().mkdirs();
				try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
					writer.write(script.content());
				} catch (IOException e) {
					throw new RuntimeException("Failed to write example script " + file, e);
				}
			}
		}
		flushInstalled();
	}
}
