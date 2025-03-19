package eventHandlers;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class GuessListener extends ListenerParent{

	private String answer = null; 
	private List<Button> buttons; 
	public GuessListener(ExecutorService executor, ScheduledExecutorService sexecutor, Long messageId, Long playerId
			, String answer,List<Button> buttons ,SlashCommandInteractionEvent ev) 
	{
		super(executor, sexecutor, messageId, playerId);
		this.answer = answer;
		this.buttons = buttons; 
		// remove lisener if not answered on time
		this.sexecutor.schedule(() ->
		{
			if (!this.pressed.get()) 
			{ 
				for(int i =0; i < buttons.size(); ++i) 
				{
					buttons.set(i, buttons.get(i).asDisabled()); 
				}
				
				ev.getChannel().asTextChannel().editMessageById(messageId, " ").setActionRow(buttons).queue() ; 
				ev.getHook().sendMessage("30 seconds passed you didn't answer on time!").queue(); 
				ev.getJDA().removeEventListener(this);
			}
		}, 30, TimeUnit.SECONDS); 
		
	}

	
    public void onButtonInteraction(ButtonInteractionEvent event) 
    {
    	if(event.getMessage().getIdLong() == this.messageId && 
    			event.getUser().getIdLong() == playerId && !this.pressed.get()) 
    	{
    		this.executor.submit( ()-> 
    		{
    			try 
    			{ 
	    			event.deferEdit().queue();
	    			this.pressed.set(true); 
	    			// answered right
					if ( event.getInteraction().getButton().getLabel().equals(answer))  
					{
						event.getMessage().editMessageEmbeds(event.getMessage().getEmbeds().get(0))
						.setActionRow(buttons.get(0).asDisabled(), buttons.get(1).asDisabled(), buttons.get(2).asDisabled(), buttons.get(3).asDisabled()).queue( );
						event.getChannel().asTextChannel().sendMessage(event.getUser().getAsMention() + " you answered " + MarkdownUtil.bold(answer)  + " you are correct!").queue(); 
					}
					else // answered wrong
					{
						event.getMessage().editMessageEmbeds(event.getMessage().getEmbeds().get(0))
						.setActionRow(buttons.get(0).asDisabled(), buttons.get(1).asDisabled(), buttons.get(2).asDisabled(), buttons.get(3).asDisabled()).queue( );
						event.getChannel().asTextChannel().sendMessage(event.getUser().getAsMention() + " you answered " + MarkdownUtil.bold(event.getInteraction().getButton().getLabel()) 
						+ " you are wrong! The answer is " + MarkdownUtil.bold(answer + "!")).queue(); 
					}
    			}
    			 catch(Exception ex) 
    			{
    				  event.getHook().sendMessage("Something went wrong!").queue();
    				  ex.printStackTrace(); 
    			}
    			finally {event.getJDA().removeEventListener(this);}
    		}); 
    	}
    }
}
