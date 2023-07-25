package events;
import java.sql.Connection;
import java.sql.SQLException;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TestCommand extends ListenerAdapter
{
	
	public static String prefix = "$"; 
	public TestCommand(String arg) 
	{	
		prefix = arg; 
	}
	
	// Override method on GuildMessageRecieved 
	@Override
	public void onMessageReceived(MessageReceivedEvent event) 
	{
		
		// if event is triggered by the bot stop it
		if(event.getMessage().getAuthor().isBot()) 
		{
			return; 
		}
		
		
	
		String msg = event.getMessage().getContentRaw(); 
		String userId = event.getAuthor().getId().toString(); 
		
	// Now conditional to check
	if(msg.equalsIgnoreCase( prefix + "hello")) 
	{
		event.getChannel().sendMessage("Hi " + "<" + "@" +userId + ">").queue(); 
	}
	
	// Shut down command 
	if(msg.equalsIgnoreCase(prefix + "shutdown")) 
	{
		System.out.println("Bot is shutdown"); 
		
	}
	
}

}
