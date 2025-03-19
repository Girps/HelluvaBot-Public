package eventHandlers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import CharactersPack.Character;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class SmashPassListener extends ListenerAdapter {

	private ExecutorService executor = null; 
	private ScheduledExecutorService sexecutor = null; 
	private Character character= null;
	private Long messageId = null; 
	private Long playerId= null; 
	private volatile  AtomicBoolean pressed = new AtomicBoolean(false); 
	public SmashPassListener(Message messageEmbed, 
			ExecutorService exe,ScheduledExecutorService sexecutor , Character character, Long messageId , Long playerId) 
	{
		this.character = character; 
		this.executor= exe; 
		this.sexecutor = sexecutor; 
		this.playerId = playerId; 
		this.messageId = messageId;
		
		this.sexecutor.schedule( () -> 
		{
			if(!pressed.get()) 
			{ 
				messageEmbed.getJDA().removeEventListener(this);
				messageEmbed.editMessageEmbeds(messageEmbed.getEmbeds().get(0))
				.setActionRow(messageEmbed.getButtons().get(0).asDisabled(), messageEmbed.getButtons().get(1).asDisabled()).queue( );
			} 
			
		}, 30, TimeUnit.SECONDS); 
		
	}; 
	
	@Override
    public void onButtonInteraction(ButtonInteractionEvent event) 
	{
	
		// check if player id and this message id
		if (event.getMessageIdLong() == this.messageId && 
				event.getInteraction().getUser().getIdLong()== this.playerId ) 
		{
			this.executor.submit( () ->
			{
				try { 
			pressed.set(true); 
			event.deferEdit().queue();
			event.getHook().sendMessage( event.getUser().getAsMention() + " would " + 
			MarkdownUtil.bold(event.getInteraction().getButton().getLabel()) + " "+  
					MarkdownUtil.bold(event.getMessage().getEmbeds().get(0).getTitle()) + "!").queue(); 
			event.getMessage().editMessageEmbeds(event.getMessage()
					.getEmbeds().get(0)).setActionRow(event.getMessage().getButtons().get(0).asDisabled(), event.getMessage().getButtons().get(1).asDisabled()).queue( );
				} 
				catch(Exception ex) 
				{
					event.getHook().sendMessage("Something went wrong!").queue(); 
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
