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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.SETUPTYPE;
import eventHandlers.GuessListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class GuessCommand extends ListenerAdapter{

	private ExecutorService executor = null; 
	private ScheduledExecutorService sexecutor = null;  
	public GuessCommand( ExecutorService executor,ScheduledExecutorService sexecutor) 
	{
		this.executor = executor; 
		this.sexecutor = sexecutor; 
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		if(event.getName().equals("guess")) 
		{
			// Get 4 random characters and pick one to guess correctly 
			this.executor.submit(() -> 
			{
				try 
				{
					event.deferReply().queue(); 
					CharacterSelection select = new CharacterSelection(); 
					final CharactersPack.Character[] chrsArray = select.getRandomCharacters(GAMETYPE.GUESS, SETUPTYPE.LIGHT, event.getGuild().getIdLong(), 4);
					// have characters 
					// Now build embed and pick character to be answered
					Random gen = new Random(); 
					int rand = gen.nextInt(4); 
					EmbedBuilder builder = new EmbedBuilder();
					builder.setImage(chrsArray[rand].getDefaultImage());
					builder.setTitle("Guess game"); 
					builder.setDescription(MarkdownUtil.italics("Who is this character?")); 
					builder.setColor(Color.LIGHT_GRAY); 
					builder.setFooter(event.getMember().getEffectiveName( ) + chrsArray[rand].getCreditStr(), event.getMember().getEffectiveAvatarUrl()); 
					List<Button> buttons = new ArrayList<Button>();
					buttons.add ( Button.secondary(chrsArray[0].getName(), chrsArray[0].getName())); 
					buttons.add ( Button.secondary(chrsArray[1].getName(), chrsArray[1].getName()));
					buttons.add(  Button.secondary(chrsArray[2].getName(), chrsArray[2].getName()));
					buttons.add(  Button.secondary(chrsArray[3].getName(), chrsArray[3].getName()));
					event.getHook().sendMessageEmbeds(builder.build()).addActionRow(buttons).queue( (messageEmbed)
							-> { messageEmbed.getJDA().addEventListener(new GuessListener( executor, sexecutor, messageEmbed.getIdLong(), event.getUser().getIdLong()
			, chrsArray[rand].getName(),buttons ,event)); }); 
				}
				catch(Exception ex) 
				{
					event.getHook().sendMessage(ex.getMessage()).queue(); 
				}	
			}); 
		}
	}

}
