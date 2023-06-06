package events;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventWaiterCommand extends ListenerAdapter	
{
	private static final String EMOTE = "U+2764U+fe0f"; 
	private final EventWaiter waiter; 
	
	public EventWaiterCommand(EventWaiter arg) 
	{
		this.waiter = arg; 
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) 
	{
		final TextChannel chan = event.getChannel().asTextChannel();
		
		if(event.getMessage().getContentRaw().equalsIgnoreCase("!!eventwaiter") == false )
		{
			return; 
		} 
		
		chan.sendMessage("React with " + Emoji.fromUnicode(EMOTE) )
		.queue( (message) -> 
		{
			message.addReaction(Emoji.fromUnicode(EMOTE)).queue(); 
			
			this.waiter.waitForEvent(
					MessageReactionAddEvent.class, 
					(e) -> e.getMessageIdLong() == message.getIdLong() && !e.getUser().isBot(), // Compare message id and message reacted with 
					(e) -> 	
					{
					
						// Should run when I react 
						System.out.println("Reacted on time"); 
						chan.sendMessageFormat("%#s was the first to react", e.getUser()).queue(); 
					},
						// Waited 10 seconds call this function
						10L,TimeUnit.SECONDS, 
						() -> chan.sendMessage("You waited too long").queue()
					); 
			
		}); 
		
	}
	
}
	

