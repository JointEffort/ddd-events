package nl.jointeffort.ddd.events;

import static org.testng.Assert.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
@ContextConfiguration("/test-context.xml")
public class DomainEventDispatcherTest extends AbstractTestNGSpringContextTests {

	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	@Autowired
	private SampleListener listener;
	
	@BeforeMethod
	public void setup() {
		
	}
	
	@Test
	public void testSimplePublish() {
		Object source = new Object();
		eventPublisher.publishEvent(new SampleEvent(source, 3));
		assertEquals(listener.getY(), 3);
	}
}
