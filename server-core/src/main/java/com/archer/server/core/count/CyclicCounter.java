package com.archer.server.core.count;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 循环计数
 *
 * @author Shinobu
 * @since 2019/1/23
 */
public class CyclicCounter {

    private final ArrayList<LongAdder> cycle;

    private final int size;

    private volatile int currentIndex = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

    CyclicCounter(int size) {
        this.cycle = new ArrayList<>(size);
        this.size = size;
        for (int i = 0; i < size; i++) {
            cycle.add(new LongAdder());
        }
    }

    public void increase() {
        cycle.get(currentIndex).increment();
    }

    public LinkedHashMap<String, Integer> toLinkedMap() {
        var result = new LinkedHashMap<String, Integer>();
        var now = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        var index = now + 1 > size ? 0 : now + 1;
        while (index != now) {
            result.put(String.valueOf(index), cycle.get(index).intValue());
            index = index + 1 > size ? 0 : index + 1;
        }
        result.put(String.valueOf(now), cycle.get(now).intValue());
        return result;
    }

    public void tryRotate(Calendar calendar) {
        var now = calendar.get(Calendar.HOUR_OF_DAY);
        if (now == currentIndex) {
            return;
        }

        var counter = cycle.get(now);
        counter.reset();
        currentIndex = now;
    }

}
