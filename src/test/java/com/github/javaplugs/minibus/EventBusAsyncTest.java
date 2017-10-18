package com.github.javaplugs.minibus;

import static org.assertj.core.api.Assertions.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EventBusAsyncTest {

    Handler1 h1 = new Handler1();

    Handler2 h2 = new Handler2();

    Handler3 h3 = new Handler3();

    Handler234 h234 = new Handler234();

    EventBusAsync<EventBusEvent> eventBus = new EventBusAsync<>();

    @BeforeClass
    void init() {
        eventBus.subscribe(h1);
        eventBus.subscribe(h2);
        eventBus.subscribe(h3);
        eventBus.subscribe(h234);
    }

    @Test
    void test() throws InterruptedException {

        assertThat(h1.counter).hasValue(1);
        assertThat(h2.counter).hasValue(0);
        assertThat(h3.counter).hasValue(0);
        assertThat(h234.counter).hasValue(0);

        eventBus.publish(new Event1());
        Thread.sleep(1000);

        assertThat(h1.counter).hasValue(1);
        assertThat(h2.counter).hasValue(0);
        assertThat(h3.counter).hasValue(0);
        assertThat(h234.counter).hasValue(0);

        eventBus.publish(new Event2());
        Thread.sleep(1000);

        assertThat(h1.counter).hasValue(1);
        assertThat(h2.counter).hasValue(1);
        assertThat(h3.counter).hasValue(0);
        assertThat(h234.counter).hasValue(1);

        eventBus.publish(new Event3());
        Thread.sleep(1000);

        assertThat(h1.counter).hasValue(1);
        assertThat(h2.counter).hasValue(1);
        assertThat(h3.counter).hasValue(1);
        assertThat(h234.counter).hasValue(2);

        eventBus.publish(new Event4());
        Thread.sleep(1000);

        assertThat(h1.counter).hasValue(1);
        assertThat(h2.counter).hasValue(1);
        assertThat(h3.counter).hasValue(1);
        assertThat(h234.counter).hasValue(3);
    }
}
