package eventHandlers;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class InsertOCListener extends ListenerParent{
	
	private HashMap<String,String> map= new HashMap<String,String>(); 
	public InsertOCListener(ExecutorService executor, ScheduledExecutorService sexecutor, Long messageId,
			Long playerId, HashMap<String,String> map, SlashCommandInteractionEvent event) {
		super(executor, sexecutor, messageId, playerId);
		this.map = map; 
		this.sexecutor.schedule(() ->
		{
			if(!this.pressed.get()) 
			{
				event.getJDA().removeEventListener(this); 
				// On failure do not add
				event.getHook().sendMessage(event.getUser().getAsMention() + 
						" 30 seconds expired! OC/Sona was not added to the bot! If you have trouble inserting your OC/Sona watch the following video."
						+ " Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue(); 
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
	    			if(!map.containsKey("name") || 
							select.isAvailable(map.get("name"), event.getGuild().getIdLong())) 
					{
						event.getChannel().sendMessage(event.getUser().getAsMention() + 
								" OC name selected not avaliable pick another name! Insert cancelled!").queue(); 
					}
	    			else 
	    			{
	    				select.insertOrginalCharacter(map, event.getUserIdLong(), event.getGuild().getIdLong());
	    				event.getChannel().sendMessage(event.getUser().getAsMention() + " your OC " + MarkdownUtil.bold(map.get("name")) + " has been successfully inserted!" ).queue(); 
	    			}
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
