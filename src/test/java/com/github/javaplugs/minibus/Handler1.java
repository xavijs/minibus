package com.github.javaplugs.minibus;

import java.util.concurrent.atomic.AtomicInteger;

public class Handler1 extends EventBusHandler<Event1> {

    AtomicInteger counter = new AtomicInteger();

    @Override
    protected void handle(Event1 event) {
        counter.incrementAndGet();
    }
}
