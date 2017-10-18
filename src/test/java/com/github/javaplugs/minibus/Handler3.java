package com.github.javaplugs.minibus;

import java.util.concurrent.atomic.AtomicInteger;

public class Handler3 extends EventBusHandler<Event3> {

    AtomicInteger counter = new AtomicInteger();

    @Override
    protected void handle(Event3 event) {
        counter.incrementAndGet();
    }

}
