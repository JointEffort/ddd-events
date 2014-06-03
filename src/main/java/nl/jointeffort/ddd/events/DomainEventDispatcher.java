package nl.jointeffort.ddd.events;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class DomainEventDispatcher implements ApplicationListener<AbstractDomainEvent>, ApplicationContextAware,
		InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(DomainEventDispatcher.class);

	private ApplicationContext applicationContext;
	private MultiMap event2ListenersMap = new MultiValueMap();
	private Map<DomainEventListener, Map<Class<?>, Method>> listener2EventMethodMap = new HashMap<DomainEventListener, Map<Class<?>, Method>>();

	@Override
	public void onApplicationEvent(AbstractDomainEvent event) {
		Class<? extends AbstractDomainEvent> eventClass = event.getClass();
		for (DomainEventListener listener : getInterestedListeners(eventClass)) {
			Method handler = getHandler(listener, eventClass);
			if (handler != null) {
				try {
					handler.invoke(listener, event);
				} catch (Exception e) {
					throw new RuntimeException("Error in domain event handler", e);
				}
			}
		}
	}

	private Method getHandler(DomainEventListener listener, Class<? extends AbstractDomainEvent> eventClass) {
		return listener2EventMethodMap.get(listener).get(eventClass);
	}

	@SuppressWarnings("unchecked")
	private Collection<DomainEventListener> getInterestedListeners(Class<? extends AbstractDomainEvent> eventClass) {
		Collection<DomainEventListener> result = (Collection<DomainEventListener>) event2ListenersMap.get(eventClass);
		if (result == null) {
			result = Collections.EMPTY_LIST;
		}
		return result;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * Build a cached map of all {@link DomainEventListener}s.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, DomainEventListener> listeners = applicationContext.getBeansOfType(DomainEventListener.class,
				false, false);
		for (DomainEventListener listener : listeners.values()) {
			introspect(listener);
		}
	}

	/**
	 * Introspect this listener's methods for {@link EventHandler} annotations.
	 * 
	 * @param listener
	 */
	private void introspect(DomainEventListener listener) {
		for (Method method : listener.getClass().getDeclaredMethods()) {
			EventHandler annotation = method.getAnnotation(EventHandler.class);
			if (annotation != null) {
				registerHandler(listener, method, annotation);
			}
		}
	}

	/**
	 * Register a handler for specific events.
	 */
	private void registerHandler(DomainEventListener listener, Method method, EventHandler annotation) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 1) {
			throw new RuntimeException(method + " is not a valid event handler, it must have exact 1 argument.");
		}
		Class<?> argumentType = parameterTypes[0];
		if (annotationValuesAreIncompatibleWithArgumentType(argumentType, annotation.value())) {
			throw new RuntimeException(
					method
							+ " is not a valid event handler, at least on of the annotation classes is abstract or cannot be cast to the argument type "
							+ argumentType);
		}

		Map<Class<?>, Method> event2MethodMap = listener2EventMethodMap.get(listener);
		if (event2MethodMap == null) {
			event2MethodMap = new HashMap<Class<?>, Method>();
			listener2EventMethodMap.put(listener, event2MethodMap);
		}
		for (Class<?> eventClass : annotation.value()) {
			event2ListenersMap.put(eventClass, listener);
			event2MethodMap.put(eventClass, method);
			LOG.info("Listener {} will handle event {} with method {}.", new Object[] {
					listener.getClass().getSimpleName(), eventClass.getSimpleName(), method.getName() });
		}

	}

	/**
	 * Validates that each of the specified eventClasses is a subtype of the
	 * requiredEventClass type.
	 */
	private boolean annotationValuesAreIncompatibleWithArgumentType(Class<?> requiredEventClass,
			Class<? extends DomainEvent>[] eventClasses) {
		for (Class<?> eventClass : eventClasses) {
			if (Modifier.isAbstract(eventClass.getModifiers()) || !requiredEventClass.isAssignableFrom(eventClass)) {
				return true;
			}
		}
		return false;
	}

}
