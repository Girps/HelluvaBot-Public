package eventHandlers;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class ResetListener extends ListenerParent{

	private volatile HashMap<String, Boolean > map = new HashMap<String, Boolean>(); 
	private volatile SlashCommandInteractionEvent ev; 
	public ResetListener(ExecutorService executor, ScheduledExecutorService sexecutor, HashMap<String, Boolean> optionMap , Long messageId, 
			Long playerId , SlashCommandInteractionEvent ev) {
		super(executor, sexecutor, messageId, playerId);
		this.map = optionMap; 
		this.ev = ev; 
		
		this.sexecutor.schedule( () -> 
		{
			if(!this.pressed.get()) 
			{
				ev.getJDA().removeEventListener(this); 
				ev.getHook().sendMessage("Expired! 30 seconds passed!").queue(); 
			}
		}, 30, TimeUnit.SECONDS);  
	}
	
	
	public void onMessageReactionAdd(MessageReactionAddEvent event) 
    {
    	if (event.getUser().getIdLong() == playerId && 
    			event.getMessageIdLong() == messageId && !this.pressed.get()) 
    	{
    		this.executor.submit(() ->
    		{
    			try
    			{ 
	    			this.pressed.set(true); 
	    			Long serverId = event.getGuild().getIdLong(); 
	    			// make sure 
	    			CharacterSelection select = new CharacterSelection(); 
	    			if (map.get("reset-collect")) 
	    			{
	    				select.removeAllPlayersCollectInGuild(serverId); 
	    			}
	    			if (map.get("reset-money"))
	    			{
	    				select.resetMoney(serverId); 
	    			}
	    			if(map.get("reset-consumable-claims")) 
	    			{
	    				select.resetConsumClaims(serverId); 
	    			}
	    			if(map.get("reset-consumable-rolls"))
	    			{
	    				select.resetConsumRolls(serverId); 
	    			}
	    			this.pressed.set(true); 
	    			this.ev.getHook().sendMessage("Reset applied").queue(); 
    			} 
    			catch(Exception ex) 
    			{
    				ex.printStackTrace(); 
    				event.getChannel().sendMessage("Something went wrong!").queue(); 
    			}
    			finally { event.getJDA().removeEventListener(this);}
    		}); 
    	}
    	
    }
}
