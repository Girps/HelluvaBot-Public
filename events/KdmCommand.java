package events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.SETUPTYPE;
import eventHandlers.KdmListener;
import CharactersPack.Character;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class KdmCommand extends ListenerAdapter
{
	private ExecutorService executor;
	private ScheduledExecutorService sexecutor; 
	public KdmCommand(  ExecutorService executor, ScheduledExecutorService sexecutor)
	{	
		this.executor = executor; 
		this.sexecutor = sexecutor; 
	}
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		
		// Check length is one get 3 random characters and check options for show name 
		if(event.getName().equals("kdm") ) 
		{
			
			// submit task
			this.executor.submit(()-> 
			{
				
				try 
				{
					event.deferReply().queue();  
					// get the characters 
					// No entry get 3 random characters
					Character[] chtrs = new Character[3]; 
					CharacterSelection select =  new CharacterSelection(); 
					if(event.getOption("first") == null && event.getOption("second") == null && event.getOption("third") == null) 
					{
						
						chtrs = select.getRandomCharacters(GAMETYPE.KDM, SETUPTYPE.LIGHT, event.getGuild().getIdLong(),3);
						
					}
					else
					{
						int size = event.getOptions().size();
						List<OptionMapping> options = event.getOptions(); 
						// Request by name a character
						for(int i = 0; i < size; ++i) 
						{
							
							chtrs[i] = select.requestSingleCharacter(options.get(i).getAsString(),  event.getGuild().getIdLong(), GAMETYPE.KDM, SETUPTYPE.LIGHT);
							
						}
						// length difference computation 
						int delta = chtrs.length - options.size(); 
						switch(delta) 
						{
						case(1): 
							chtrs[2] = select.getRandomCharacters(GAMETYPE.KDM, SETUPTYPE.LIGHT,  event.getGuild().getIdLong(),1)[0];
							break; 
						case(2): 
							Character[] temp = null;
							temp = select.getRandomCharacters(GAMETYPE.KDM, SETUPTYPE.LIGHT, event.getGuild().getIdLong(),2);
							chtrs[2] = temp[1]; 
							chtrs[1] =temp[0]; 
							break; 
						default: 
							break; 
						}
					}
					// now build messages
					// have characters now send embed and use event waiter
					Color darkRed = new Color(190,0,0); 
					// set up emebeds and send each one
					EmbedBuilder builderOne = new EmbedBuilder(); 
					builderOne.setFooter(chtrs[0].getCreditStr()); 
					builderOne.setTitle(chtrs[0].getName()); 
					builderOne.setColor(darkRed); 
					builderOne.setThumbnail(chtrs[0].getDefaultImage()); 
					builderOne.setDescription(MarkdownUtil.bold("Player : ") + event.getUser().getAsMention()); 
					
					EmbedBuilder builderTwo = new EmbedBuilder(); 
					builderTwo.setFooter(chtrs[1].getCreditStr()); 
					builderTwo.setTitle(chtrs[1].getName()); 
					builderTwo.setColor(darkRed); 
					builderTwo.setThumbnail(chtrs[1].getDefaultImage()); 
					builderTwo.setDescription(MarkdownUtil.bold("Player : ") + event.getUser().getAsMention()); 
					
					EmbedBuilder builderThree = new EmbedBuilder(); 
					builderThree.setFooter(chtrs[2].getCreditStr()); 
					builderThree.setTitle(chtrs[2].getName()); 
					builderThree.setColor(darkRed); 
					builderThree.setThumbnail(chtrs[2].getDefaultImage()); 
					builderThree.setDescription(MarkdownUtil.bold("Player : ") + event.getUser().getAsMention()); 
					
					List<Button> buttons = new ArrayList<Button>();
					buttons.add(Button.danger("kill", "Kill")); 
					buttons.add(Button.primary("date", "Date"));
					buttons.add(Button.success("marry", "Marry")); 
					
					
					event.getHook().sendMessageEmbeds(builderOne.build()).setActionRow(buttons).queue( (firstMessage) -> 
					{
						event.getChannel().sendMessageEmbeds(builderTwo.build()).setActionRow(buttons).queue( (secondMessage) -> 
						{
							event.getChannel().sendMessageEmbeds(builderThree.build()).setActionRow(buttons).queue((thirdMessage)->
							{
								// construct kdm round and push it in hashmap
								Long firstId = firstMessage.getIdLong(); 
								Long secondId = secondMessage.getIdLong(); 
								Long thirdId = thirdMessage.getIdLong(); 
								ArrayList<String> characters = new ArrayList<String>(); 
								characters.add(firstMessage.getEmbeds().get(0).getTitle());
								characters.add(secondMessage.getEmbeds().get(0).getTitle());
								characters.add(thirdMessage.getEmbeds().get(0).getTitle());
								ArrayList<Long> messages=  new ArrayList<Long>(); 
								messages.add(firstId); 
								messages.add(secondId); 
								messages.add(thirdId); 
								// now create a kdm round and push it to the concurrent hashmap 
								event.getJDA().addEventListener(new KdmListener(thirdMessage,this.executor, this.sexecutor,
										characters, messages, event.getUser().getIdLong() )); 
							});  
						}); 
					}); 
					
				}
				catch(Exception ex)
				{
					event.getHook().sendMessage("Something went wrong!"); 
					ex.printStackTrace(); 
				}
			});
			
		}
				
	}
	
}
