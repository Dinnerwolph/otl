package fr.dinnerwolph.otl.base.utils;


import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Dinnerwolph
 */
public class MemoryUtils {

    public static void a() {
        long i = Runtime.getRuntime().maxMemory();
        long j = Runtime.getRuntime().totalMemory();
        long k = Runtime.getRuntime().freeMemory();
        long l = j - k;
        List<String> list = Lists.newArrayList(String.format("Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMb(l), bytesToMb(i)), String.format("Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMb(j)), "");
        list.forEach(strings -> {
            System.out.println(strings);
        });
    }

    private static long bytesToMb(long bytes) {
        return bytes / 1024L / 1024L;
    }
}
