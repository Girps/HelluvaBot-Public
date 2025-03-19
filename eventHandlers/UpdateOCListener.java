package eventHandlers;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class UpdateOCListener extends ListenerParent {

	private HashMap<String, String> map; 
	private String oldName; 
    public UpdateOCListener(ExecutorService executor, ScheduledExecutorService sexecutor, Long messageId, Long playerId
			, HashMap<String, String> map, String oldName, SlashCommandInteractionEvent event) {
		super(executor, sexecutor, messageId, playerId);
		this.map = map; 
		this.oldName = oldName; 
		this.sexecutor.schedule(() -> 
		{
			if(!this.pressed.get()) 
			{
				event.getJDA().removeEventListener(this); 
				event.getHook().sendMessage(event.getUser().getAsMention() + 
						" 30 seconds expired! OC/Sona was not updated to the bot!"
						+ " If you have trouble inserting your OC/Sona watch the following video. "
						+ "Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue(); 

			}
		}, 30, TimeUnit.SECONDS); 
    }

	public void onMessageReactionAdd(MessageReactionAddEvent event) 
    {
    	if (event.getUser().getIdLong() == this.playerId &&
    			event.getMessageIdLong() == this.messageId && !this.pressed.get()) 
    	{
    		this.executor.submit(() -> 
    		{
    			try 
    			{ 
    				this.pressed.set(true); 
	    			CharacterSelection select=  new CharacterSelection(); 
	    			// now update the oc
	    			if(!map.containsKey("name") && !map.get("name").equals(oldName)  &&
							select.isAvailable(map.get("name"), event.getGuild().getIdLong())) 
					{
						event.getChannel().sendMessage(event.getUser().getAsMention() + " OC name selected not avaliable pick another name! Update cancelled!").queue(); 
					}
					else 
					{ 
						select.updateOC(oldName,event.getUser().getIdLong(), event.getGuild().getIdLong(), map );
						event.getChannel().sendMessage(event.getUser().getAsMention() + " your OC updated!").queue();
					}
    			}
    			catch(Exception ex) 
    			{
    				ex.printStackTrace(); 
    				event.getChannel().sendMessage("Something went wrong!").queue(); 
    			}
    			finally 
    			{
    				event.getJDA().removeEventListener(this); 
    			}
    		}); 
    	}
    }
}
