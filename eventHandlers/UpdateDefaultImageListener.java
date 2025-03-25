package eventHandlers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class UpdateDefaultImageListener extends ListenerParent{

	public volatile Integer index = 0; 
	public volatile String name = ""; 
	private SlashCommandInteractionEvent ev = null; 
	public UpdateDefaultImageListener(ExecutorService executor, ScheduledExecutorService sexecutor, Long messageId,
			Long playerId, String name, Integer index,SlashCommandInteractionEvent ev) {
		super(executor, sexecutor, messageId, playerId);
		this.index = index; 
		this.name = name; 
		this.ev = ev; 
		
		// remove listener after 30 seconds 
		this.sexecutor.schedule(() -> 
		{
			if(!this.pressed.get()) 
			{
				ev.getJDA().removeEventListener(this); 
				this.pressed.set(true); 
				ev.getHook().sendMessage("30 seconds passed setting new image has expired!").queue(); 
			}
		}, 30, TimeUnit.SECONDS); 
	}
	
    public void onMessageReactionAdd(MessageReactionAddEvent event) 
    {
    
    	if(event.getUser().getIdLong() == playerId && !this.pressed.get() 
    			&& event.getMessageIdLong() == messageId ) 
    	{
    		this.executor.submit(() -> 
    		{
    			try 
    			{ 
	    			CharacterSelection select=  new CharacterSelection(); 
	    			if ( select.setDefCharacterImage(event.getGuild().getIdLong(), name, index)) 
	    			{
	    				ev.getHook().sendMessage(event.getUser().getAsMention() + 
	    						" default character image for " + name + " has been set!").queue();
	    			}
	    			else  
	    			{
	    				ev.getHook().sendMessage( event.getUser().getAsMention() +
	    						" default character image set failed!").queue();
	    			}
    			} 
    			catch(Exception ex) 
    			{
    				ex.printStackTrace(); 
    			}
    			finally 
    			{
    				this.pressed.set(true); 
    				ev.getJDA().removeEventListener(this); 
    			}
    			
    		} ); 
    	}
    }
	

}
