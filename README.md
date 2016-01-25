# Lightweight event bus for java applications

This can be used as an alternative for Guava EventBus.

[![Release](https://jitpack.io/v/javaplugs/minibus.svg)](https://jitpack.io/#javaplugs/minibus)  
[API javadoc](https://jitpack.io/com/github/javaplugs/minibus/-SNAPSHOT/javadoc/)

## Add to your project

You can add this artifact to your project using [JitPack](https://jitpack.io/#javaplugs/minibus).  
All versions list, instructions for gradle, maven, ivy etc. can be found by link above.

To get latest commit use -SNAPSHOT instead version number.

## Getting started

### Installation
1. Add dependency to your project.
2. This library using slf4j-api which output log messages to /dev/null. 
You have to configure proper logger for slf4j in your project to view this messages.

### Create your handlers
Any handler for event bus should implement EventHandler interface.

#### Simple handler
Use it in a case if your handler should be able to process events of only one type.
```java
public class SimpleHandler implements EventHandler<Event>{
    public String getType() {
        return "SIMPLE_EVENT"; // Event type we want to handle here
    }
    public boolean canHandle(String eventType) {
        return true; // As long as getType() return not null this method is not called at all
    }
    public void handle(Event event) {
        System.out.println("I've got an event " + event.getType());
        // And other stuff 
    }
}
```

#### Advanced handler
If you want to handle several types of events,
that you should decide what event type to process using canHandle method.

```java
public class AdvHandler implements EventHandler<Event>{
    public String getType() {
        return null; // Means that now canHandle method is in charge
    }
    public boolean canHandle(String eventType) {
        return eventType.startsWith("USER_");
    }
    public void handle(Event event) {
        // Handle all events with types like: USER_CREATED, USER_DEL, USER_LOGIN etc.
        System.out.println("I've got an event " + event.getType());
        // And other stuff 
    }
}
```

### Initialize EventBus & subscribe handlers
Now you have to create your EventBus and subscribe your handlers.

```java
// This is recommended implementation.
// In a case if you want to handle all events in current thread only 
// you can useEventBusSimple
EventBus<Event> eventBus = new EventBusAsync<>();

// Now just subscribe your listeners here, 
// but keep in mind that handlers subscribed using weak links,
// so you should store hard links to handlers somewhere or make them singletons.
eventBus.subscribe(simpleHandlerInstance);
eventBus.subscribe(advHandlerInstance);
```

### Publish your event
All you need is to create event with valid event type.

```java
Event ev1 = new Event("SIMPLE_EVENT");
ev1.setValue("value_key", 42);
ev1.setValue("value_other_key", "You can put any object here");
eventBus.publish(ev1); // Will be send to SimpleHandler


Event ev2 = new Event("USER_CREATED");
ev2.setValue("id", 42);
eventBus.publish(ev2); // To AdvHandler

// To AdvHandler using event builder
eventBus.publish(
    EventBuilder.create("USER_EMAIL_CONFIRMED")
    .val("id", 4242)
    .build()
);
```

### Advanced usage
Current event bus implementation allows you to use your own event type with help of java generics.

* Create CustomEvent type extending Event
* Optionally create CustomEventHandler interface extending EventHandler<CustomEvent> interface
* Create appropriate handlers
* Initialize EventBus with CustomEvent

That is all. Now you can you your own CustomEvent or maybe several events based on CustomEvent.