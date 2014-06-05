package nl.jointeffort.ddd.events;

public class SampleEvent extends AbstractDomainEvent {

	private static final long serialVersionUID = 1L;
	
	private int x;
	
	public SampleEvent(Object source, int x) {
		super(source);
		this.x = x;
	}

	public int getX() {
		return x;
	}
}
