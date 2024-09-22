package org.skriptlang.skript.bukkit.spark;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow.CpuUsage;
import me.lucko.spark.api.statistic.StatisticWindow.MillisPerTick;
import me.lucko.spark.api.statistic.StatisticWindow.TicksPerSecond;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;

/**
 * Utility class for accessing Spark's API.
 */
public class SparkUtils {

	private static Spark sparkInstance;
	private static DoubleStatistic<CpuUsage> cpuProcess;
	private static DoubleStatistic<CpuUsage> cpuSystem;
	private static GenericStatistic<DoubleAverageInfo, MillisPerTick> mspt;
	private static DoubleStatistic<TicksPerSecond> tps;

	private static Spark spark() {
		if (sparkInstance == null)
			sparkInstance = SparkProvider.get();
		return sparkInstance;
	}

	/**
	 * Checks for MSPT support. This should be checked before using the MSPT methods.
	 * @return whether MSPT statistics are supported.
	 */
	public static boolean supportsMspt() {
		return mspt() != null;
	}

	private static @UnknownNullability GenericStatistic<DoubleAverageInfo, MillisPerTick> mspt() {
		if (mspt == null) {
			mspt = spark().mspt();
			if (mspt == null)
				return null;
		}
		return mspt;
	}

	/**
	 * Get the MSPT stat info block for the given window.
	 * {@link #supportsMspt()} should be checked prior to using this.
	 *
	 * @param window the window for which to gather statistics from.
	 * @return the MSPT info block.
	 */
	public static @Nullable DoubleAverageInfo mspt(MillisPerTick window) {
		GenericStatistic<DoubleAverageInfo, MillisPerTick> mspt = mspt();
		return mspt.poll(window);
	}

	/**
	 * Get the MSPT stat info block for the given windows.
	 * {@link #supportsMspt()} should be checked prior to using this.
	 *
	 * @param windows the windows for which to gather statistics from.
	 * @return the MSPT info blocks.
	 */
	public static DoubleAverageInfo @Nullable [] mspt(MillisPerTick... windows) {
		GenericStatistic<DoubleAverageInfo, MillisPerTick> mspt = mspt();
		return Arrays.stream(windows).map(mspt::poll).toArray(DoubleAverageInfo[]::new);
	}

	private static DoubleStatistic<CpuUsage> cpuProcess() {
		if (cpuProcess == null)
			cpuProcess = spark().cpuProcess();
		return cpuProcess;
	}

	/**
	 * Get the process CPU usage for the given window.
	 * @param window the window for which to gather statistics from.
	 * @return the process cpu usage.
	 */
	public static double cpuProcess(CpuUsage window) {
		return cpuProcess().poll(window);
	}

	/**
	 * Get the process CPU usage for the given windows.
	 * @param windows the windows for which to gather statistics from.
	 * @return the process cpu usages.
	 */
	public static Double[] cpuProcess(CpuUsage... windows) {
		DoubleStatistic<CpuUsage> cpu = cpuProcess();
		return Arrays.stream(windows).map(cpu::poll).toArray(Double[]::new);
	}

	private static DoubleStatistic<CpuUsage> cpuSystem() {
		if (cpuSystem == null)
			cpuSystem = spark().cpuSystem();
		return cpuSystem;
	}

	/**
	 * Get the system CPU usage for the given window.
	 * @param window the window for which to gather statistics from.
	 * @return the system cpu usage.
	 */
	public static double cpuSystem(CpuUsage window) {
		return cpuSystem().poll(window);
	}

	/**
	 * Get the system CPU usage for the given windows.
	 * @param windows the windows for which to gather statistics from.
	 * @return the system cpu usages.
	 */
	public static Double[] cpuSystem(CpuUsage... windows) {
		DoubleStatistic<CpuUsage> cpu = cpuSystem();
		return Arrays.stream(windows).map(cpu::poll).toArray(Double[]::new);
	}

	private static DoubleStatistic<TicksPerSecond> tps() {
		if (tps == null)
			tps = spark().tps();
		return tps;
	}

	/**
	 * Get the TPS for the given windows.
	 * @param window the window for which to gather statistics from.
	 * @return the TPS value.
	 */
	public static double tps(TicksPerSecond window) {
		return tps().poll(window);
	}

	/**
	 * Get the TPS for the given windows.
	 * @param windows the windows for which to gather statistics from.
	 * @return the TPS values.
	 */
	public static Double[] tps(TicksPerSecond... windows) {
		DoubleStatistic<TicksPerSecond> tps = tps();
		return Arrays.stream(windows).map(tps::poll).toArray(Double[]::new);
	}

}
