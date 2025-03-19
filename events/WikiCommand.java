package events;

import CharactersPack.Character;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.SELECTIONTYPE;
import CharactersPack.SETUPTYPE;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.concurrent.ConcurrentHashMap;


public class WikiCommand extends ListenerAdapter{

	private ExecutorService executor = null; 
	// may add a hashmap
	//public static ConcurrentHashMap<Long, Character> wikiMap = new ConcurrentHashMap<Long, Character>(); 
	
	public WikiCommand(ExecutorService executor)
	{
		this.executor = executor; 
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
	{
		
		// Check valid command
		if(event.getName().equals("wiki")) 
		{
			
			
			this.executor.submit( () -> 
			{
				event.deferReply().queue(); 
				String characterName = event.getOption("character").getAsString(); 
				OffsetDateTime time = event.getTimeCreated(); 
				Date date = Date.from(time.toInstant());
				try 
				{
					CharacterSelection select = new CharacterSelection(); 				
					// We have the fields now create our character object 
					Character charactFound = select.requestSingleCharacter(characterName,event.getGuild().getIdLong(), GAMETYPE.WIKI,SETUPTYPE.LIGHT);

					// We have the character now build an embed for the character 
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setAuthor(charactFound.getName(), charactFound.getUrl());
					builder.setColor(Color.red); 
					builder.setDescription(charactFound.getBasic()); 
					builder.addField("Quote", MarkdownUtil.quote(charactFound.getQuote()),false);
					builder.setThumbnail(charactFound.getDefaultImage()); 
					builder.setFooter(event.getMember().getEffectiveName(), event.getMember().getEffectiveAvatarUrl());  
					// Now send the embed to the server
					event.getHook().sendMessageEmbeds(builder.build()).queue();
				}
				catch(Exception ex) 
				{
					ex.printStackTrace();
					event.getHook().sendMessage(characterName + " not found " ).queue();
				}
				
			}); 
			
		}
		else if(event.getName().equals("wiki-full") )	// wiki full pagnation implementation 
		{
			// Get character name  
			this.executor.submit(() -> 
			{
				event.deferReply().queue(); 
				String characterName = event.getOption("character").getAsString();
				// Now check in database for the character 
				try 
				{
						CharacterSelection select = new CharacterSelection(); 		
						// We have the fields now create our character object 
						Character charactFound = select.requestSingleCharacter(characterName, event.getGuild().getIdLong(),GAMETYPE.WIKI,SETUPTYPE.HEAVY);
						
						// We have the character now build an embed for the character 
						EmbedBuilder builder = new EmbedBuilder(); 
						
						builder.setColor(Color.red); 
						builder.setAuthor(charactFound.getName(), charactFound.getUrl());
						builder.setImage(charactFound.getDefaultImage());
						builder.setDescription(charactFound.getBasic()); 
						builder.addField("Quote",  MarkdownUtil.quote(charactFound.getQuote()),false);
						builder.setFooter("Image: " + "1/" + (charactFound.getImageNonJSONList().size() + "\nWiki: 1/15") , event.getMember().getEffectiveAvatarUrl()); 
						
						// Instantiate list of buttons 
						List<Button> buttons = new ArrayList<Button>();
						buttons.add(Button.secondary("leftwiki", "<<")); 
						buttons.add(Button.primary("left", "<"));
						buttons.add(Button.danger("close", "Close")); 
						buttons.add(Button.primary("right", ">")); 
						buttons.add(Button.secondary("rightwiki", ">>")); 
						
						// add to concurrent hashmap if not existing 
						
						
						
						// Now send the embed to the server
						event.getHook().sendMessageEmbeds(builder.build()).addActionRow(buttons).queue();		
				}
				catch(Exception e) 
				{
					e.printStackTrace();
					event.getHook().sendMessage(characterName + " not found ").queue();
				}
			}); 
			
		}
		
	}
	
	/* Deal with buttons clicked on this event */ 
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) 
	{
		String dir_Str = event.getButton().getId();
		if (dir_Str.equalsIgnoreCase("left") || 
				dir_Str.equalsIgnoreCase("right") || dir_Str.equalsIgnoreCase("close") || 
				dir_Str.equalsIgnoreCase("leftwiki") || dir_Str.equalsIgnoreCase("rightwiki")) 
		{  
			event.deferEdit().queue( (v) ->
			{
				// Implement pagination on embeds called by wikifull command 
				if( !event.getMember().getUser().isBot() ) 
				{
					// Now get title which should hold characters name use it
					
					MessageEmbed old = event.getMessage().getEmbeds().get(0); 
					EmbedBuilder oldBuild = new EmbedBuilder(old); 
					String characterName = old.getAuthor().getName(); // get the name 
					
					// Get page location in the footer 
					String footerData = event.getMessage().getEmbeds().get(0).getFooter().getText();  
					
					// Get number of image 
					String imageNumberStr = footerData.split("\n")[0]; 
					imageNumberStr = footerData.split("/")[0]; 
					imageNumberStr = imageNumberStr.replace("Image: ",""); 
					int pageNumber = Integer.valueOf(imageNumberStr) - 1 ; 
					
					// Get number of wiki 
					String wikiNumberStr = footerData.split("\n")[1]; 
					wikiNumberStr = wikiNumberStr.split("/")[0]; 
					wikiNumberStr = wikiNumberStr.replace("Wiki: ", ""); 
					int wikiNumber = Integer.valueOf(wikiNumberStr); 
					
					// Get the character
					Character charcTarget = null; 
					try 
					{		
				
						CharacterSelection select = new CharacterSelection();
						charcTarget = select.requestSingleCharacter(characterName,event.getGuild().getIdLong(), GAMETYPE.WIKI,SETUPTYPE.HEAVY);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					
					
					// Size of the array  
					int arraySize = charcTarget.getImageNonJSONList().size() - 1; 
					
					// Get the button name called 
					
					
					// Check which buttons were clicked 
					if(dir_Str.equalsIgnoreCase("left")) 
					{
						pageNumber--; 
						// Check if negative or above the bounds
						if(pageNumber < 0) 
						{
							// reset it to arraySize 
							pageNumber = arraySize; 
							
						}
					} // Right decrement number 
					else if(dir_Str.equalsIgnoreCase("right"))
					{
						pageNumber++; 
						 if(pageNumber > arraySize) // if above the size set it 0
						{
							pageNumber = 0; 
						}
					}
					else if(dir_Str.equalsIgnoreCase("close")) // Delete the message 
					{
						event.deferEdit().queue(); 
						event.getMessage().delete().queue(); 
					}
					else if(dir_Str.equalsIgnoreCase("leftwiki")) 
					{
						// Decremenit it 
						wikiNumber--; 
						if(wikiNumber < 1) 
						{
							wikiNumber = 15; 
						}
						
					}
					else // deals with right wiki 
					{
						wikiNumber++; 
						if(wikiNumber > 15) 
						{
							wikiNumber = 1; 
						}
					}
	
					
					
					// We have a proper index now edit the embed
					tweakEmbedWiki(oldBuild, charcTarget ,wikiNumber); 
					oldBuild.setImage(charcTarget.getImageNonJSONList().get(pageNumber)); // <-- gotta fix this 
					oldBuild.setFooter("Image: " + (pageNumber + 1) + "/" + (charcTarget.getImageNonJSONList().size()) + 
							"\nWiki: " + wikiNumber + "/15",event.getMember().getEffectiveAvatarUrl()); 
					
					event.getMessage().editMessageEmbeds(oldBuild.build()).queue( );
				}
			});
		}
	
		
		
	}
	
	// Helper function to help set up embed of the message 
	void tweakEmbedWiki(EmbedBuilder builder, Character charactFound, int wikiNumber) 
	{
		builder.clearFields(); 
		switch(wikiNumber)
		{
		case(1):
			builder.setDescription(charactFound.getBasic()); 
			builder.addField("Quote", MarkdownUtil.quote(charactFound.getQuote()),false);
			break;
		case(2):
			builder.addField(MarkdownUtil.underline("Nicknames"),charactFound.getNickNames(),true);  
			break; 
		case(3):
			builder.addField(MarkdownUtil.underline("Likes"),charactFound.getLikes(),true);
			break; 
		case(4):
			builder.addField(MarkdownUtil.underline("Dislikes"),charactFound.getDisLikes(),true); 
			break;
		case(5): 
			builder.addField(MarkdownUtil.underline("Gender"), charactFound.getGender(), true);
			break; 
		case(6):
			builder.addField(MarkdownUtil.underline("Species"), charactFound.getSpecies(), true);
			break;
		case(7):
			builder.addField(MarkdownUtil.underline("Abilities"), charactFound.getAbilities(),true);
			break; 
		case(8):
			builder.addField(MarkdownUtil.underline("Status"),charactFound.getStatus(),true); 
			break; 
		case(9):
			builder.addField(MarkdownUtil.underline("Occupation"), charactFound.getOccup(),true);
			break; 
		case(10):
			builder.addField(MarkdownUtil.underline("Family"), charactFound.getFamily(),true);
			break; 
		case(11):
			builder.addField(MarkdownUtil.underline("Friends"), charactFound.getFriends(),true);
			break; 
		case(12):
			builder.addField(MarkdownUtil.underline("Romantic Interests"), charactFound.getRomance(),true);
			break; 
		case(13):
			builder.addField(MarkdownUtil.underline("Enemies"),charactFound.getEnemies(),true); 
			break; 	
		case(14):
			builder.addField(MarkdownUtil.underline("Others"),charactFound.getOthers(),true); 
		break;
		case(15):
			builder.addField(MarkdownUtil.underline("Voice actor"), charactFound.getVoiceActors(),true);
			break; 
		}
	}
	
}
