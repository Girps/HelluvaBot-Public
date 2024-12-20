package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.SETUPTYPE;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class GuessCommand extends ListenerAdapter{

	private  EventWaiter waiter; 
	public GuessCommand(EventWaiter argWaiter ) 
	{
		waiter = argWaiter; 
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		if(event.getName().equals("guess")) 
		{
			
			// Get 4 random characters and pick one to guess correctly 
			CompletableFuture.supplyAsync( () -> 
			{
				// db get 4 random characters 
				event.deferReply().queue(); 
				CharacterSelection select = new CharacterSelection(); 
				CharactersPack.Character[] chts = null; 
				try 
				{
					chts = select.getRandomCharacters(GAMETYPE.GUESS, SETUPTYPE.LIGHT, event.getGuild().getIdLong(), 4);
				}
				catch (Exception e) 
				{
					throw new CompletionException(e); 
				}
				return chts; 
			}).thenAccept( chrsArray -> 
			{
				// Now build embed and pick character to be answered
				Random gen = new Random(); 
				int rand = gen.nextInt(4); 
				EmbedBuilder builder = new EmbedBuilder();
				builder.setImage(chrsArray[rand].getDefaultImage());
				builder.setTitle("Guess game"); 
				builder.setDescription(MarkdownUtil.italics("Who is this character?")); 
				builder.setColor(Color.LIGHT_GRAY); 
				builder.setFooter(event.getMember().getEffectiveName(), event.getMember().getEffectiveAvatarUrl()); 
				List<Button> buttons = new ArrayList<Button>();
				buttons.add ( Button.secondary(chrsArray[0].getName(), chrsArray[0].getName())); 
				buttons.add ( Button.secondary(chrsArray[1].getName(), chrsArray[1].getName()));
				buttons.add(  Button.secondary(chrsArray[2].getName(), chrsArray[2].getName()));
				buttons.add(  Button.secondary(chrsArray[3].getName(), chrsArray[3].getName()));
				event.getHook().sendMessageEmbeds(builder.build()).addActionRow(buttons).queue( (messageEmbed)
						-> {
				this.waiter.waitForEvent(ButtonInteractionEvent.class, 
						(e) -> !e.getUser().isBot() && e.getMessageIdLong() == messageEmbed.getIdLong(), 
						(e) -> CompletableFuture.runAsync( () -> 
						{
							e.deferEdit().queue(); 
							if ( e.getInteraction().getButton().getLabel().equals(chrsArray[rand].getName()) ) 
							{
								e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds().get(0))
								.setActionRow(buttons.get(0).asDisabled(), buttons.get(1).asDisabled(), buttons.get(2).asDisabled(), buttons.get(3).asDisabled()).queue( );
								e.getChannel().asTextChannel().sendMessage(event.getUser().getAsMention() + " you answered " + MarkdownUtil.bold(chrsArray[rand].getName())  + " you are correct!").queue(); 
							}
							else 
							{
								e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds().get(0))
								.setActionRow(buttons.get(0).asDisabled(), buttons.get(1).asDisabled(), buttons.get(2).asDisabled(), buttons.get(3).asDisabled()).queue( );
								e.getChannel().asTextChannel().sendMessage(event.getUser().getAsMention() + " you answered " + MarkdownUtil.bold(e.getInteraction().getButton().getLabel()) 
								+ " you are wrong! The answer is " + MarkdownUtil.bold(chrsArray[rand].getName() + "!")).queue(); 
							}
							
						}), 1 , TimeUnit.MINUTES, () -> CompletableFuture.runAsync(  () -> 
						{
							messageEmbed.editMessageEmbeds(messageEmbed.getEmbeds().get(0)).setActionRow(buttons.get(0).asDisabled(), buttons.get(1).asDisabled(),buttons.get(2).asDisabled(), buttons.get(3).asDisabled()).queue( msg -> 
							{
								msg.getChannel().asTextChannel().sendMessage(event.getUser().getAsMention() + " your session expired!").queue(); 
							}) ; 
						}));
				}); 
			}).exceptionally((ex) -> 
			{
				System.out.println(ex.getMessage());
				event.getHook().sendMessage("Error Occurred!").queue(); 
				return null;
			});  
		}
	}

}
