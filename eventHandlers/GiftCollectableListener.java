package eventHandlers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class GiftCollectableListener extends ListenerParent{
	
	Long recieverId = null; 
	SlashCommandInteractionEvent ev = null; 
	String giftCharacterName = null; 
	public GiftCollectableListener(ExecutorService executor, ScheduledExecutorService sexecutor, Long messageId,
			Long playerId, Long recieverId, String giftCharacterName, SlashCommandInteractionEvent ev) {
		super(executor, sexecutor, messageId, playerId);
		// TODO Auto-generated constructor stub
		this.recieverId = recieverId; 
		this.ev = ev; 
		this.giftCharacterName = giftCharacterName; 
		this.sexecutor.schedule( () ->
		{
			if(!this.pressed.get())
			{
				ev.getHook().sendMessage("30 seconds passed gift expired!").queue();
				ev.getJDA().removeEventListener(this); 
			}
		}, 30, TimeUnit.SECONDS); 
	}

	public void onMessageReactionAdd(MessageReactionAddEvent event) 
	{
		if(event.getUser().getIdLong() == recieverId &&
				!this.pressed.get()) 
		{
			// now give
			try { 
			CharacterSelection select=  new CharacterSelection(); 
			select.giveCharacter(playerId, recieverId,event.getGuild().getIdLong(), giftCharacterName); 
			ev.getHook().sendMessage("Gift successful!").queue(); 
			} 
			catch(Exception e) 
			{
				event.getChannel().sendMessage("Something went wrong!").queue();
				e.printStackTrace(); 
			}
			finally 
			{
				ev.getJDA().removeEventListener(this);
				this.pressed.set(true);
			}
		}
	}

}
