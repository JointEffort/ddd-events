package nl.jointeffort.ddd.events;


import org.springframework.context.ApplicationEvent;

public abstract class AbstractDomainEvent extends ApplicationEvent implements DomainEvent {

	private static final long serialVersionUID = 1L;

	public AbstractDomainEvent(Object source) {
		super(source);
	}

}
