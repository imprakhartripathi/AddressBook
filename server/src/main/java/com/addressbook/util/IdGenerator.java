package com.addressbook.util;

import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {
    private static final AtomicLong COUNTER = new AtomicLong(1);

    private IdGenerator() {
    }

    public static long nextId() {
        return COUNTER.getAndIncrement();
    }
}
