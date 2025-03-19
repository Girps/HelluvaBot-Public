package eventHandlers;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class UpdateSona extends ListenerParent{
	
	private HashMap<String, String > map; 
	private String oldName; 
	public UpdateSona(ExecutorService executor, ScheduledExecutorService sexecutor, 
			Long messageId, Long playerId, String oldName , HashMap<String, String > map,SlashCommandInteractionEvent event) 
	{
		super(executor, sexecutor, messageId, playerId);
		this.map= map ;
		this.oldName= oldName; 
		this.sexecutor.schedule(() -> 
		{
			if(!this.pressed.get()) 
			{
				event.getJDA().removeEventListener(this); 
				event.getHook().
				sendMessage(event.getUser().getAsMention() + 
						" 30 seconds expired! OC/Sona was not updated to the bot! "
						+ "If you have trouble inserting your OC/Sona watch the following video. "
						+ "Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue();
			}
		}, 30, TimeUnit.SECONDS); 
	}
	
	public void onMessageReactionAdd(MessageReactionAddEvent event) 
	{
		if(event.getUser().getIdLong() == this.playerId && 
				event.getMessageIdLong() == this.messageId &&
				!this.pressed.get()) 
		{
			this.executor.submit(() -> 
			{
				CharacterSelection select = new CharacterSelection(); 
				try 
				{ 
					this.pressed.set(true);
					if( map.get("name").isBlank() ||  ( !map.get("name").equals(oldName))  &&
							select.isAvailable(map.get("name"), event.getGuild().getIdLong())) 
					{
						event.getChannel().sendMessage(event.getUser().getAsMention() + " Sona name selected not avaliable pick another name! Update cancelled!").queue();
						 
					}
					else 
					{ 
						select.updateSona(event.getUser().getIdLong(), event.getGuild().getIdLong(), map );
						event.getChannel().sendMessage( event.getUser().getAsMention() + " your Sona updated!").queue();
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
