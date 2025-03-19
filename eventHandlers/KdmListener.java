package eventHandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class KdmListener extends ListenerAdapter{

	private ExecutorService executor = null; 
	private ScheduledExecutorService sexecutor = null; 
	
	private HashMap<String, String> maps = new HashMap<String, String>(); 
	private boolean kill = false; 
	private boolean date = false;
	private boolean marry =false;
	private Long playerId ;
	private ArrayList<String> characterNames = null;
	private ArrayList<Long> messageIds = null;
	private volatile  AtomicBoolean roundeOver = new AtomicBoolean(false); 
	private volatile  AtomicBoolean pressed = new AtomicBoolean(false); 

	public KdmListener(Message messageEmbed, 
			ExecutorService exe,ScheduledExecutorService sexecutor , ArrayList<String> characterNames, ArrayList<Long> messageIds , Long playerId) 
	{
		this.executor = exe ; 
		this.sexecutor= sexecutor; 
		this.characterNames = characterNames; 
		this.messageIds = messageIds;
		this.playerId = playerId; 
		// submit a task to delete all messages
		this.sexecutor.schedule(() -> 
		{
			// round not over then delete
			if( !roundeOver.get() )
			{
				messageEmbed.getChannel().deleteMessageById(messageIds.get(0)).queue();
				messageEmbed.getChannel().deleteMessageById(messageIds.get(1)).queue();
				messageEmbed.getChannel().deleteMessageById(messageIds.get(2)).queue();
				messageEmbed.getChannel().getJDA().removeEventListener(this); 
			}
		}, 1, TimeUnit.MINUTES); 
		
	}
	
	@Override
    public void onButtonInteraction(ButtonInteractionEvent event) 
	{
		// check if same game instnace s
		if(messageIds.contains(event.getMessageIdLong()) ||
				playerId == event.getInteraction().getUser().getIdLong()) 
		{
			this.executor.submit(  () ->
			{ 
				try
				{ 
					// now disable button if game is not over 
					if (!this.pressed.get()) 
					{
						this.pressed.set(true);
						String choice= event.getButton().getId(); 
						this.characterNames.remove(event.getMessage().getEmbeds().get(0).getTitle()); 
						this.maps.put( choice, event.getMessage().getEmbeds().get(0).getTitle());
						
						List<Button> buttons = event.getMessage().getButtons(); 
						
						if(choice.equals("kill")) 
						{
							kill = true; 
							buttons.set(0, buttons.get(0).asDisabled()); 
						}
						else if(choice.equals("date")) 
						{
							date = true; 
							buttons.set(1, buttons.get(1).asDisabled()); 
						}
						else 
						{
							// marry
							marry = true; 
							buttons.set(2, buttons.get(2).asDisabled());  
						}
						event.editMessageEmbeds(event.getMessage().getEmbeds().get(0)).setActionRow(event.getMessage().getButtons().get(0).asDisabled(),
								event.getMessage().getButtons().get(1).asDisabled(),event.getMessage().getButtons().get(2).asDisabled()).queue(); 
						
						// now disable the rest
						for(int i =0; i < this.messageIds.size(); ++i) 
						{
							if( this.messageIds.get(i) != event.getMessageIdLong()) 
							{
								event.getChannel().editMessageById(this.messageIds.get(i), " ").setActionRow(buttons).queue();
							}
						}
				
					}
					else if(!this.roundeOver.get())
					{
						event.deferEdit().queue(); 
						roundeOver.set(true); 
						String choice= event.getButton().getId(); 
						// now store result to fields
						this.characterNames.remove(event.getMessage().getEmbeds().get(0).getTitle());
						this.maps.put( choice, event.getMessage().getEmbeds().get(0).getTitle()) ; 
						
						if(choice.equals("kill")) 
						{
							kill = true; 
						}
						else if(choice.equals("date")) 
						{
							date = true; 
						}
						else 
						{
							// marry
							marry = true; 
						}
						// get last character
						String last  = this.characterNames.get(0);
						
						if (!kill) 
						{
							this.maps.put("kill", last); 
						}
						else if(!date) 
						{
							this.maps.put("date", last); 
						}
						else if(!marry)
						{
							this.maps.put("marry", last); 
						}
						
						
						// its over disable all buttons 
						for(int i =0; i < messageIds.size(); ++i) 
						{
							
							event.getChannel().editMessageById(this.messageIds.get(i), " ").setActionRow(event.getMessage().getButtons().get(0).asDisabled(),
									event.getMessage().getButtons().get(1).asDisabled(),event.getMessage().getButtons().get(2).asDisabled()).queue(); 
						}
						// now send the result and remove the event listner
						
						event.getHook().sendMessage(event.getUser().getAsMention() + " would" + " kill " +  MarkdownUtil.bold(this.maps.get("kill")) + " date " 
						+  MarkdownUtil.bold(this.maps.get("date")) + " and " + "marry " +  MarkdownUtil.bold( this.maps.get("marry")) ).queue();
						
					}
				}catch(Exception ex) 
				{
					ex.printStackTrace();
					event.getJDA().removeEventListener(this);

				}

			}
			); 
		 	
		}
	}	

}
