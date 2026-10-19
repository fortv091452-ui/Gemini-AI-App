package com.example.data

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

data class MemoryStats(
    val totalMemMb: Long,
    val availMemMb: Long,
    val usedMemMb: Long,
    val usedPercent: Int,
    val isLowMemory: Boolean,
    val thresholdMb: Long
)

data class DeviceProfile(
    val modelName: String,
    val manufacturer: String,
    val androidVersion: String,
    val apiLevel: Int,
    val cpuCores: Int,
    val cpuAbi: String,
    val isPrestigioProfileActive: Boolean
)

data class WolfiusCommand(
    val title: String,
    val category: String,
    val command: String,
    val explanation: String,
    val isAdbScript: Boolean = true
)

object WolfiusSystemManager {

    fun getMemoryStats(context: Context): MemoryStats {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)

        val totalMb = memInfo.totalMem / (1024 * 1024)
        val availMb = memInfo.availMem / (1024 * 1024)
        val usedMb = (totalMb - availMb).coerceAtLeast(0)
        val percent = if (totalMb > 0) ((usedMb.toDouble() / totalMb) * 100).toInt() else 0
        val thresholdMb = memInfo.threshold / (1024 * 1024)

        return MemoryStats(
            totalMemMb = totalMb,
            availMemMb = availMb,
            usedMemMb = usedMb,
            usedPercent = percent,
            isLowMemory = memInfo.lowMemory,
            thresholdMb = thresholdMb
        )
    }

    suspend fun performRootlessTrim(context: Context): Long = withContext(Dispatchers.Default) {
        val before = getMemoryStats(context).availMemMb
        
        // 1. JVM Garbage Collection
        System.gc()
        System.runFinalization()
        System.gc()

        // 2. Clear app internal cache directories
        try {
            context.cacheDir.deleteRecursively()
            context.externalCacheDir?.deleteRecursively()
        } catch (_: Exception) {}

        // 3. Short wait for system allocator
        SystemClock.sleep(120)

        val after = getMemoryStats(context).availMemMb
        val freed = (after - before).coerceAtLeast(0)
        freed
    }

    fun getDeviceProfile(context: Context, forcePrestigioPreset: Boolean): DeviceProfile {
        val cores = Runtime.getRuntime().availableProcessors()
        return DeviceProfile(
            modelName = if (forcePrestigioPreset) "Prestigio MultiPad 2 Pro Duo 7.0 (PMP5670C)" else Build.MODEL,
            manufacturer = if (forcePrestigioPreset) "Prestigio" else Build.MANUFACTURER,
            androidVersion = if (forcePrestigioPreset) "4.1.1 Jelly Bean (Simulated Target)" else Build.VERSION.RELEASE,
            apiLevel = if (forcePrestigioPreset) 16 else Build.VERSION.SDK_INT,
            cpuCores = cores,
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "armeabi-v7a",
            isPrestigioProfileActive = forcePrestigioPreset
        )
    }

    suspend fun checkNetworkLatency(): Long = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("8.8.8.8", 53), 2000)
            }
            System.currentTimeMillis() - start
        } catch (_: Exception) {
            -1L
        }
    }

    fun getRootlessCommands(): List<WolfiusCommand> {
        return listOf(
            WolfiusCommand(
                title = "Очистка кэша всех приложений (Non-Root)",
                category = "Память",
                command = "adb shell pm trim-caches 256M",
                explanation = "Безопасно освобождает до 256 МБ ОЗУ и хранилища без root прав."
            ),
            WolfiusCommand(
                title = "Ускорение анимаций в 2 раза (0.5x)",
                category = "Быстродействие",
                command = "adb shell settings put global window_animation_scale 0.5 && adb shell settings put global transition_animation_scale 0.5 && adb shell settings put global animator_duration_scale 0.5",
                explanation = "Убирает задержки интерфейса на слабых процессорах Cortex-A9 и Mali-400."
            ),
            WolfiusCommand(
                title = "Анализ расхода памяти процессом",
                category = "Память",
                command = "adb shell dumpsys meminfo --oom",
                explanation = "Показывает распределение ОЗУ и пороги Low Memory Killer (LMK)."
            ),
            WolfiusCommand(
                title = "Принудительный аппаратный рендеринг GPU",
                category = "Быстродействие",
                command = "adb shell setprop debug.hwui.renderer opengl",
                explanation = "Снижает нагрузку на 2-ядерный CPU RK3066, перенося отрисовку на Mali-400."
            ),
            WolfiusCommand(
                title = "Ограничение фоновых процессов",
                category = "Энергосбережение",
                command = "adb shell settings put global max_phantom_processes 4",
                explanation = "Защищает от вылетов по нехватке памяти на устройствах с 1 ГБ ОЗУ."
            ),
            WolfiusCommand(
                title = "Таймаут экрана 30 секунд",
                category = "Батарея",
                command = "adb shell settings put system screen_off_timeout 30000",
                explanation = "Продлевает автономность батареи 4100 мАч на планшетах Prestigio."
            )
        )
    }
}
