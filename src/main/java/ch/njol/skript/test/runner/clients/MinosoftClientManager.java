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
package ch.njol.skript.test.runner.clients;

import ch.njol.skript.Skript;
import org.eclipse.jdt.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MinosoftClientManager implements ClientManager {

	private Path minosoftJarPath;
	private Set<Process> minosoftProcesses = new HashSet<>();

	public MinosoftClientManager() {
		this.minosoftJarPath = findMinosoftJar();
	}

	@Nullable
	private Path findMinosoftJar() {
		Path skriptDataFolder = Skript.getInstance().getDataFolder().toPath();
		String osName = System.getProperty("os.name", "unknown").toLowerCase(Locale.ENGLISH);
		if (osName.contains("windows")) {
			return skriptDataFolder.resolve("minosoft").resolve("minosoft-windows.jar").toAbsolutePath();
		} else if (osName.contains("linux")) {
			return skriptDataFolder.resolve("minosoft").resolve("minosoft-linux.jar").toAbsolutePath();
		}
		return null; // todo: support macs
	}

	@Override
	public boolean isAvailable() {
		return minosoftJarPath != null && Files.exists(minosoftJarPath);
	}

	@Override
	public void launchClient(String username, String serverAddress, String serverPort) throws ClientLaunchException {
		Path javaPath = Paths.get(System.getProperty("java.home"), "bin", "java").toAbsolutePath().normalize();
		ProcessBuilder minosoftProcessBuilder = new ProcessBuilder(javaPath.toString(), "-jar", minosoftJarPath.toString(), "--headless");
		try {
			Process minosoftProcess = minosoftProcessBuilder.start();
			BufferedWriter minosoftCommandWriter = new BufferedWriter(new OutputStreamWriter(minosoftProcess.getOutputStream()));
			minosoftCommandWriter.write("account add offline " + username + '\n');
			minosoftCommandWriter.write("account select " + username + '\n');
			minosoftCommandWriter.write("connect " + serverAddress + ":" + serverPort + '\n');
			minosoftCommandWriter.close();
			minosoftProcesses.add(minosoftProcess);
		} catch (IOException exception) {
			throw new ClientLaunchException(exception);
		}
	}

	@Override
	public int getClientCount() {
		return minosoftProcesses.size();
	}

	@Override
	public void killClients() {
		minosoftProcesses.forEach(Process::destroyForcibly);
	}

}
