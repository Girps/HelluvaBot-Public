package eventHandlers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import net.dv8tion.jda.api.hooks.ListenerAdapter;


public abstract class ListenerParent extends ListenerAdapter {

	protected Long playerId; 
	protected Long messageId; 
	protected ExecutorService executor; 
	protected ScheduledExecutorService sexecutor = null; 
	protected volatile  AtomicBoolean pressed = new AtomicBoolean(false); 
	
	
	public ListenerParent(ExecutorService executor, ScheduledExecutorService sexecutor, 
			Long messageId, Long playerId) 
	{
		this.executor = executor; 
		this.sexecutor = sexecutor; 
		this.messageId = messageId; 
		this.playerId = playerId; 
	}
	
}
