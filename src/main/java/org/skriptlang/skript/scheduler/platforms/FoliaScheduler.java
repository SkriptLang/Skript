package org.skriptlang.skript.scheduler.platforms;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
	public void cancelAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> Future<T> submitSafely(Callable<T> callable) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
