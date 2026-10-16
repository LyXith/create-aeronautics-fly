package dev.simulated_team.simulated.client;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 把"游戏卡死但没有任何日志"变成可定位的证据。
 *
 * <p>玩家报告"世界卡住、日志里什么都没有"时，客户端日志只是安静地停在最后一行 ——
 * 无论是渲染线程陷进死循环、卡在锁上，还是集成服务器把整个客户端拖住，
 * log4j 都不会写出任何东西。这个看门狗用一个心跳计数器（由客户端 tick 递增）
 * 判断渲染线程是否还在推进；连续 {@link #STALL_SECONDS} 秒没有任何 tick，
 * 就认为游戏已经卡死，把全部线程栈连同状态写进
 * {@code logs/render-stall-<时间戳>.txt}，并在日志里打一条 WARN 指向该文件。
 *
 * <p>只有真正停滞时才有 IO 开销，正常游玩时每 {@link #POLL_MILLIS} 秒只读一个
 * {@code AtomicLong}，可以放心常开。
 */
public final class RenderStallWatchdog {

    private static final Logger LOGGER = LoggerFactory.getLogger("Simulated");

    private static final long POLL_MILLIS = 5_000L;
    /**
     * 判定卡死的停滞时长。给资源重载、地形加载这类正常长耗时留足余量。
     * 可用 {@code -Dsimulated.renderStallSeconds=N} 调短，方便复现时更快拿到线程栈。
     */
    private static final long STALL_SECONDS = Long.getLong("simulated.renderStallSeconds", 90L);
    /**
     * 启动后先观察这么长时间，避开世界加载/模组初始化期的正常忙碌。
     * 可用 {@code -Dsimulated.renderStallWarmupSeconds=N} 调短。
     */
    private static final long WARMUP_SECONDS = Long.getLong("simulated.renderStallWarmupSeconds", 60L);
    /** 同一次卡死只落盘一次，避免线程转储把磁盘写爆。 */
    private static final long DUMP_COOLDOWN_MILLIS = 300_000L;

    private static final AtomicLong TICKS = new AtomicLong();
    /** 渲染线程。由客户端初始化线程记录 —— 在 MC 里就是渲染线程本身。 */
    private static volatile Thread renderThread;
    private static volatile boolean started;

    private RenderStallWatchdog() {
    }

    /** 由 {@code ClientTickEvents.END_CLIENT_TICK} 调用；tick 停止即渲染线程停止推进。 */
    public static void onClientTick() {
        TICKS.incrementAndGet();
    }

    public static void start() {
        if (started) {
            return;
        }
        started = true;
        renderThread = Thread.currentThread();

        final Thread watchdog = new Thread(RenderStallWatchdog::watch, "Simulated-RenderStallWatchdog");
        watchdog.setDaemon(true);
        watchdog.setPriority(Thread.MIN_PRIORITY);
        watchdog.start();
    }

    private static void watch() {
        final long startNanos = System.nanoTime();
        long lastTicks = -1L;
        long lastChangeNanos = startNanos;
        long lastDumpNanos = 0L;

        while (true) {
            try {
                Thread.sleep(POLL_MILLIS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            final long now = System.nanoTime();
            final long ticks = TICKS.get();
            if (ticks != lastTicks) {
                lastTicks = ticks;
                lastChangeNanos = now;
                continue;
            }

            final long stalledForMillis = (now - lastChangeNanos) / 1_000_000L;
            if (stalledForMillis < STALL_SECONDS * 1000L) {
                continue;
            }
            if (now - startNanos < WARMUP_SECONDS * 1_000_000_000L) {
                continue;
            }
            if (now - lastDumpNanos < DUMP_COOLDOWN_MILLIS * 1_000_000L) {
                continue;
            }
            lastDumpNanos = now;

            dump(stalledForMillis / 1000L);
        }
    }

    private static void dump(final long stalledSeconds) {
        final Thread render = renderThread;
        final String renderState = render == null ? "unknown" : render.getState().name();
        final Path directory = FabricLoader.getInstance().getGameDir().resolve("logs");
        final Path target = directory.resolve("render-stall-" + Instant.now().toString().replace(':', '-') + ".txt");

        try {
            Files.createDirectories(directory);

            final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
            final ThreadInfo[] infos = threads.dumpAllThreads(true, true);

            final StringBuilder report = new StringBuilder(16_384);
            report.append("Simulated render stall report\n")
                    .append("=============================\n")
                    .append("渲染线程已经 ").append(stalledSeconds).append(" 秒没有推进任何客户端 tick。\n")
                    .append("渲染线程: ").append(render == null ? "?" : render.getName())
                    .append(" (").append(renderState).append(")\n")
                    .append("生成时间: ").append(Instant.now()).append('\n')
                    .append("请把这个文件和 logs/latest.log 一起提供。\n\n");

            report.append("---- 渲染线程栈 ----\n");
            appendThread(report, render, infos);

            report.append("\n---- 全部线程 ----\n");
            for (final ThreadInfo info : infos) {
                if (info == null || (render != null && info.getThreadId() == render.threadId())) {
                    continue;
                }
                appendThread(report, null, new ThreadInfo[]{info});
            }

            Files.writeString(target, report.toString());

            // WARN 而不是 ERROR：卡死本身不是模组异常，也不该让生产的冒烟测试把日志
            // 里的 ERROR 当成失败（那个检查只放行 Realms / user properties 两类）。
            // 路径写全，玩家即使不翻文件目录也能把它连同 latest.log 一起发出来。
            LOGGER.warn("Render thread stalled for {}s; thread dump written to {}",
                    stalledSeconds, target.toAbsolutePath());
        } catch (final Throwable t) {
            LOGGER.warn("Render thread stalled for {}s, but writing the thread dump failed", stalledSeconds, t);
        }
    }

    private static void appendThread(final StringBuilder report, final Thread render, final ThreadInfo[] infos) {
        for (final ThreadInfo info : infos) {
            if (info == null) {
                continue;
            }
            // render != null 时只挑出渲染线程那一份；为 null 表示"打印全部"。
            if (render != null && info.getThreadId() != render.threadId()) {
                continue;
            }
            report.append(info.toString()).append('\n');
        }
    }
}
