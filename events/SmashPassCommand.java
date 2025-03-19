package events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.Character;
import CharactersPack.SETUPTYPE;
import eventHandlers.SmashPassListener;

public class SmashPassCommand extends ListenerAdapter
{
	
	private ExecutorService executor; 
	private ScheduledExecutorService sexecutor; 
	public SmashPassCommand(  ExecutorService executor, ScheduledExecutorService sexecutor)
	{

		this.executor = executor; 
		this.sexecutor = sexecutor; 
	}
	
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		// smash pass case gets a random character
		if (event.getName().equals("smashpass")   ) 
		{
			
			this.executor.submit(() -> 
			{
				event.deferReply().queue(); 
				CharacterSelection set = new CharacterSelection(); 
				Character target =null; 
				try 
				{
					if(event.getOption("character") == null) 
					{
						target = set.getRandomCharacters(GAMETYPE.SMASHPASS, SETUPTYPE.LIGHT, event.getGuild().getIdLong(),1)[0];
					}
					else 
					{
						target =  set.requestSingleCharacter(event.getOption("character").getAsString(),event.getGuild().getIdLong(), GAMETYPE.SMASHPASS,SETUPTYPE.LIGHT);
					}
					
					if(target != null) 
					{
						// get character from db set builder now set up the event waiter 
						 EmbedBuilder build = new EmbedBuilder()
						 .setThumbnail(target.getDefaultImage())
						 .setTitle(target.getName()) 
						 .setDescription(MarkdownUtil.italics("Smash or pass?")) 
						 .setColor(new Color(102,0,153))
						 .setFooter(target.getCreditStr());
						 List<Button> buttons = new ArrayList<Button>();
						 buttons.add(Button.primary("Smash", "smash")); 
						 buttons.add(Button.danger("Pass", "pass")); 
						 
						final Character chtr = target; 
						 // add a listener
						 event.getHook().sendMessageEmbeds(build.build()).addActionRow(buttons).queue( (messageEmbed) -> 
						 {
							 event.getJDA().addEventListener( new SmashPassListener(messageEmbed,this.executor, this.sexecutor,chtr, messageEmbed.getIdLong(), event.getInteraction().getUser().getIdLong())); 
						 }); 
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace(); 
				}
			
			}); 
			
		}
		
	}

}
