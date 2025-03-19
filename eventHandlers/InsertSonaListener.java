package eventHandlers;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class InsertSonaListener extends ListenerParent {
	private HashMap<String, String> map; 
	public InsertSonaListener(ExecutorService executor, ScheduledExecutorService sexecutor, Long messageId,
			Long playerId , HashMap<String, String> map,SlashCommandInteractionEvent ev) {
		super(executor, sexecutor, messageId, playerId);
		this.map = map;
		this.sexecutor.schedule(() -> 
		{
			if(!this.pressed.get()) 
			{ 
				ev.getJDA().removeEventListener(this);
				ev.getHook().sendMessage(ev.getUser().getAsMention() + " 30 seconds expired! OC/Sona was not added to the bot!"
						+ " If you have trouble inserting your OC/Sona watch the following video. "
						+ "Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue(); 
			}
		}, 30, TimeUnit.SECONDS); 
	}

	
	
	public void onMessageReactionAdd(MessageReactionAddEvent event) 
	{
		if (event.getMessageIdLong() == this.messageId &&
				event.getUser().getIdLong() == playerId && !this.pressed.get()) 
		{
			this.executor.submit(() -> 
			{
				try 
				{ 
					this.pressed.set(true);
	    			CharacterSelection select=  new CharacterSelection(); 
					// now insert sona 
					if(map.get("name").isBlank() ||
							select.isAvailable(map.get("name"), event.getGuild().getIdLong())) 
					{
						event.getChannel().sendMessage(event.getUser().getAsMention() + 
								" Sona name selected not avaliable pick another name! Insert cancelled!").queue();
					}
					else 
					{ 
						select.insertSona(map.get("name"), event.getUser().getIdLong(), map.get("url"),
								event.getGuild().getIdLong(), map.get("kdm"), map.get("smashpass"), map.get("simps")
									, map.get("ships"), map.get("kins"), 
									map.get("waifu"), map.get("favorite"), 
									map.get("guess"), map.get("collect"));
						event.getChannel().sendMessage(event.getUser().getAsMention() + " your Sona " + MarkdownUtil.bold(map.get("name")) + 
								" has been successfully inserted!" ).queue(); 
					}
				} 
				catch(Exception ex) 
				{
					event.getChannel().sendMessage("Something went wrong!").queue(); 
					ex.printStackTrace(); 
				}
				finally 
				{
					event.getJDA().removeEventListener(this); 
				}
			}); 
		}
	}
}
