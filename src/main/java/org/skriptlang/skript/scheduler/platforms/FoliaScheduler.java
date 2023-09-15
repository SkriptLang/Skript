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
package org.skriptlang.skript.scheduler.platforms;

import java.util.concurrent.TimeUnit;

import org.bukkit.plugin.Plugin;
import org.skriptlang.skript.scheduler.AsyncTask;
import org.skriptlang.skript.scheduler.PlatformScheduler;
import org.skriptlang.skript.scheduler.Task;

public class FoliaScheduler implements PlatformScheduler {

	@Override
	public void run(Task task, long delayInTicks) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean cancel(Task task) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAlive(Task task) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void run(AsyncTask task, long delay, TimeUnit unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean cancel(AsyncTask task) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAlive(AsyncTask task) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancelAll(Plugin plugin) {
		// TODO Auto-generated method stub
		
	}

}
