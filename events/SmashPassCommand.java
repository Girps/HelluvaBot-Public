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
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.Character;
import CharactersPack.SETUPTYPE;

public class SmashPassCommand extends ListenerAdapter
{
	
	private final EventWaiter waiter;
	
	public SmashPassCommand( EventWaiter wait)
	{
		waiter = wait; 
	}
	
	
	@Override
	public void  onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		
		// Smash pass case gets a random character 
		if( event.getName().equals("smashpass") && event.getOption("character") == null) 
		{
			
			CompletableFuture.supplyAsync( () ->
			{
				event.deferReply().queue();
				CharacterSelection set = new CharacterSelection();
				Character target = null; 
				 try 
				 {
					target = set.getRandomCharacters(GAMETYPE.SMASHPASS, SETUPTYPE.LIGHT, event.getGuild().getIdLong(),1)[0];
				 } 
				 catch (Exception e) {
					// TODO Auto-generated catch block
					throw new CompletionException(e); 
				}
				 return target; 
			}
			).thenAccept( (chtr) ->
			{
				// get character from db set builder now set up the event waiter 
				 EmbedBuilder build = new EmbedBuilder()
				 .setThumbnail(chtr.getDefaultImage())
				 .setTitle(chtr.getName()) 
				 .setDescription(MarkdownUtil.italics("Smash or pass?")) 
				 .setColor(new Color(102,0,153));
				 List<Button> buttons = new ArrayList<Button>();
				 buttons.add(Button.primary("Smash", "smash")); 
				 buttons.add(Button.danger("Pass", "pass")); 
				
				 event.getHook().sendMessageEmbeds(build.build()).addActionRow(buttons).queue
				 (
						(messageEmbed) ->
						{
						// Lambda predicate, lambda function, TimeUnit , lambda function expiration
						this.waiter.waitForEvent(ButtonInteractionEvent.class,
								(e) -> !e.getUser().isBot() && e.getMessageIdLong() == messageEmbed.getIdLong(),
								(e) -> CompletableFuture.runAsync( ()->
								{
									e.deferEdit().queue();
									e.getChannel().asTextChannel().sendMessage( e.getUser().getAsMention() + " would " + MarkdownUtil.bold(e.getInteraction().getButton().getLabel()) + " "+  MarkdownUtil.bold(messageEmbed.getEmbeds().get(0).getTitle()) + "!").queue(); 
									e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds().get(0)).setActionRow(buttons.get(0).asDisabled(), buttons.get(1).asDisabled()).queue( );
									// Disabled now send a message back to the channle
									 
								}),1, TimeUnit.MINUTES, 
								() -> CompletableFuture.runAsync( () -> 
								{
									messageEmbed.editMessageEmbeds(messageEmbed.getEmbeds().get(0)).setActionRow(buttons.get(0).asDisabled(),buttons.get(1).asDisabled()).queue();  
									messageEmbed.getChannel().asTextChannel().sendMessage("Session expired!").queue(); 
								}
								)); 
					}); 
			}).exceptionally(ex -> 
			{
				System.out.println(ex.getMessage()); 
				event.getHook().sendMessage(ex.getMessage()).queue(); 
				return null; 
			});
			System.out.println("MAIN:" + Thread.currentThread().getName()); 

		}	
		else if(event.getName().equals("smashpass") && event.getOption("character") != null)	// Smash pass event gets a single character 	 
		{
			
		CompletableFuture.supplyAsync( () ->
		 {
			 	event.deferReply().queue(); 
		 		CharacterSelection selection = new CharacterSelection(); 
		 		String targetName = event.getOption("character").getAsString(); 
		 		// Use a query to get the character
		 		Character target;
				try
				{
					target = selection.requestSingleCharacter(targetName,event.getGuild().getIdLong(), GAMETYPE.SMASHPASS,SETUPTYPE.LIGHT);
				}
				catch (Exception e) 
				{
					// TODO Auto-generated catch block
					throw new CompletionException(e); 
				} 
		 		return target; 
		 } 
		 ).thenAccept( (character) -> {
			 	
				EmbedBuilder build = new EmbedBuilder()
				.setThumbnail(character.getDefaultImage()) 
				.setTitle(character.getName())
				.setDescription(MarkdownUtil.italics("Smash or pass?")) 
				.setColor(new Color(102,0,153));
				List<Button> buttons = new ArrayList<Button>();
				buttons.add(Button.primary("Smash", "smash")); 
				buttons.add(Button.danger("Pass", "pass")); 
				 event.getHook().sendMessageEmbeds(build.build()).addActionRow(buttons).queue
				 (
						(messageEmbed) ->
						{
						// Lambda predicate, lambda function, TimeUnit , lambda function expiration
						this.waiter.waitForEvent(ButtonInteractionEvent.class,
								(e) -> !e.getUser().isBot() && e.getMessageIdLong() == messageEmbed.getIdLong(),
								(e) -> CompletableFuture.runAsync ( () ->
								{
									e.deferEdit().queue();
									e.getChannel().asTextChannel().sendMessage("<@" + e.getUser().getId() +"> "+ " would " + MarkdownUtil.bold(e.getInteraction().getButton().getLabel()) + " "+  MarkdownUtil.bold(messageEmbed.getEmbeds().get(0).getTitle()) + "!").queue(); 
									e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds().get(0)).setActionRow(buttons.get(0).asDisabled(), buttons.get(1).asDisabled()).queue( );
									// Disabled now send a message if they smashed or passed the character 
									
								}),1, TimeUnit.MINUTES, 
								() -> CompletableFuture.runAsync ( () ->
									{
								messageEmbed.editMessageEmbeds(messageEmbed.getEmbeds().get(0)).setActionRow(buttons.get(0).asDisabled(),buttons.get(1).asDisabled()).queue();  
								messageEmbed.reply(MarkdownUtil.bold("Session expired!")).queue();
									}
								)); 
						 });
		 } )
			.exceptionally( ex -> 
		 {
			System.out.println(ex.getMessage()); 
			event.getHook().sendMessage(ex.getMessage()).queue(); 
			return null; 
		 }); 
		}
	}

}
