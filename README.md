Intro
-----
This is a simple synchronous event handling system, for use in Spring based applications. 

Rationale
---------
Using domain events to notify other components that something of interest happened encourages loose-coupling of components and (among other things) makes it easier to satisfy the [Single Responsibility Principle](http://en.wikipedia.org/wiki/Single_responsibility_principle).

Usage
-----
First, include in the event sender an instance of the Spring's [ApplicationEventPublisher](http://docs.spring.io/spring/docs/3.2.8.RELEASE/javadoc-api/org/springframework/context/ApplicationEventPublisher.html):
```
@Autowired
private ApplicationEventPublisher eventPublisher;
```
Then, publish your `Event`, which should extend `AbstractDomainEvent`:
```
eventPublisher.publishEvent(new MyCustomEvent(loggedInUser, dataOfInterest));
```
To get this event handled, you then create a listener class. This must be a Spring `@Component` and have a void single-argument method with a type of your event (or a superclass):
```
@Component
public class MyEventsListener implements DomainEventListener {
    
    ...
    @EventHandler(MyCustomEvent.class)
    public void handleMyCustomEvent(MyCustomEvent event) {
        ...
    }
    ...
}

```