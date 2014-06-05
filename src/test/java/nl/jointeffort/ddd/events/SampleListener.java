package nl.jointeffort.ddd.events;

import org.springframework.stereotype.Component;

@Component
public class SampleListener implements DomainEventListener {

	private int y;
	
	@EventHandler({SampleEvent.class})
	public void onSampleEvent(SampleEvent event) {
		y = event.getX();
	}
	
	public int getY() {
		return y;
	}
}
