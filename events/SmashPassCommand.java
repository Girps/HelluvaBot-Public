package events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.Character;
import CharactersPack.SETUPTYPE;

public class SmashPassCommand extends ListenerAdapter
{
	
	private final EventWaiter waiter;
	private static  Connection conn; 
	public SmashPassCommand( Connection arg_Conn, EventWaiter wait)
	{
		waiter = wait; 
		conn = arg_Conn; 
	}
	
	
	@Override
	public void  onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		
		Character target = null; 
		
		// Smash pass case gets a random character 
		if( event.getName().equals("smashpass") && event.getOption("character") == null) 
		{
			// Get random number that corresponds 
			try 
			{
				CharacterSelection set = new CharacterSelection(conn);
				 target = set.getRandomCharacters(GAMETYPE.SMASHPASS, SETUPTYPE.LIGHT, event.getGuild().getIdLong(),1)[0];
				 
				 EmbedBuilder build = new EmbedBuilder(); 
				 build.setThumbnail(target.getDefaultImage()); 
				 build.setTitle(target.getName()); 
				 build.setDescription(MarkdownUtil.italics("Smash or pass?")); 
				 build.setColor(Color.RED);
				 List<Button> buttons = new ArrayList<Button>();
				 buttons.add(Button.primary("Smash", "smash")); 
				 buttons.add(Button.danger("Pass", "pass")); 
				 
				 event.deferReply().queue();
				 event.getHook().sendMessageEmbeds(build.build()).addActionRow(buttons).queue
				 (
						(messageEmbed) ->
						{
						// Lambda predicate, lambda function, TimeUnit , lambda function expiration
						this.waiter.waitForEvent(ButtonInteractionEvent.class,
								(e) -> !e.getUser().isBot() && e.getMessageIdLong() == messageEmbed.getIdLong(),
								(e) -> 
								{
									String verb = "<@" + e.getUser().getId() +"> "+ " would " + e.getInteraction().getButton().getLabel() + " "+ messageEmbed.getEmbeds().get(0).getTitle() + "!";   
									e.deferEdit().queue();
									e.getChannel().asTextChannel().sendMessage(verb).queue(); 
									e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds().get(0)).setActionRow(buttons.get(0).asDisabled(), buttons.get(1).asDisabled()).queue( );
									// Disabled now send a message if they smashed or passed the character 
									
								},1, TimeUnit.MINUTES, 
								() -> 
									{
								messageEmbed.editMessageEmbeds(messageEmbed.getEmbeds().get(0)).setActionRow(buttons.get(0).asDisabled(),buttons.get(1).asDisabled()).queue();  
								messageEmbed.getChannel().asTextChannel().sendMessage("Session expired!").queue(); 
									}
								); 
						 }); 
				  
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				event.deferReply().queue();
				event.getHook().sendMessage("Smashpass command failed!").queue();
			} 
		}	
		else if(event.getName().equals("smashpass") && event.getOption("character") != null)	// Smash pass event gets a single character 	 
		{
			String targetName = event.getOption("character").getAsString(); 
			
			targetName = targetName.trim(); 
			try 
			{ 
				CharacterSelection selection = new CharacterSelection(conn); 
				// Use a query to get the character
				target = selection.requestSingleCharacter(targetName,  event.getGuild().getIdLong(), GAMETYPE.SMASHPASS,SETUPTYPE.LIGHT); 
				// Build embed 
				
				if(target == null) 
				{
					event.deferReply(); 
					event.getHook().sendMessage(targetName + " not in Smashpass command!" ).queue();
				}
				
				EmbedBuilder build = new EmbedBuilder(); 
				build.setThumbnail(target.getDefaultImage()); 
				build.setTitle(target.getName()); 
				build.setDescription(MarkdownUtil.italics("Smash or pass?")); 
				build.setColor(Color.RED);
				List<Button> buttons = new ArrayList<Button>();
				buttons.add(Button.primary("Smash", "smash")); 
				buttons.add(Button.danger("Pass", "pass")); 
				
				event.deferReply().queue(); 
				 event.getHook().sendMessageEmbeds(build.build()).addActionRow(buttons).queue
				 (
						(messageEmbed) ->
						{
						// Lambda predicate, lambda function, TimeUnit , lambda function expiration
						this.waiter.waitForEvent(ButtonInteractionEvent.class,
								(e) -> !e.getUser().isBot() && e.getMessageIdLong() == messageEmbed.getIdLong(),
								(e) -> 
								{
									
									e.deferEdit().queue();
									e.getChannel().asTextChannel().sendMessage("<@" + e.getUser().getId() +"> "+ " would " + MarkdownUtil.bold(e.getInteraction().getButton().getLabel()) + " "+  MarkdownUtil.bold(messageEmbed.getEmbeds().get(0).getTitle()) + "!").queue(); 
									e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds().get(0)).setActionRow(buttons.get(0).asDisabled(), buttons.get(1).asDisabled()).queue( );
									// Disabled now send a message if they smashed or passed the character 
									
								},1, TimeUnit.MINUTES, 
								() -> 
									{
								messageEmbed.editMessageEmbeds(messageEmbed.getEmbeds().get(0)).setActionRow(buttons.get(0).asDisabled(),buttons.get(1).asDisabled()).queue();  
								messageEmbed.getChannel().asTextChannel().sendMessage("Session expired!").queue(); 
									}
								); 
						 });
				
			}
			catch(SQLException e) 
			{
				e.printStackTrace();
				event.getHook().sendMessage(targetName + " not in Smashpass command!" ).queue();
			}
			
		}
		
		
	}
	
}
