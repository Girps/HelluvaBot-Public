package events;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandManger extends ListenerAdapter {

	private Connection conn; 
	private static ArrayList<String> wikiNames;
	private ArrayList<String> MiscNames; 
	public CommandManger (Connection conn_Arg)
	{
		conn = conn_Arg;
		CharacterSelection select = new CharacterSelection(conn); 
		ArrayList<String> names = null ; 
		try {
			 names = select.getAllCharacterNames(GAMETYPE.WIKI);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		wikiNames = names; 
		
		
	}
	
	
	
	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event)
	{
		
		 // Check type of of command 
		 if( ( event.getName().equals("wikifull") || event.getName().equals("wiki") ) ) 
		 { 
			 // Search the letter and get 25 possible options including that lettter 
			 
			 ArrayList<String> subList = new ArrayList<String>(); 
			 
			 for(int i =0; i < wikiNames.size(); ++i) 
			 {
				 if(wikiNames.get(i).toLowerCase().contains(event.getFocusedOption().getValue().toLowerCase()) || wikiNames.get(i).equalsIgnoreCase(event.getFocusedOption().getValue())) 
				 {
					subList.add(wikiNames.get(i)); 
				 }
			 }
			 // Convert to arrays
			 String [] subset = new String[subList.size()]; 
			 
			 subList.toArray(subset); 
			 
			 List<Command.Choice> options = Stream.of(subset).limit(25)
					 .filter(name -> name.startsWith(event.getFocusedOption().getValue()))
					 .map(name -> new Command.Choice(name, name))
					 .collect(Collectors.toList()); 
			 event.replyChoices(options).queue();   
		 }
		 else if(event.getName().equals("kdm")  ||  event.getFocusedOption().getName().equals("first") || 
					 event.getFocusedOption().getName().equals("second") || event.getFocusedOption().getName().equals("third") )  
		 {
			 CharacterSelection select = new CharacterSelection(conn); 
			 try 
			{
				 ArrayList<String> subList = new ArrayList<String>(); 
				MiscNames = select.getAllCharacterNames(GAMETYPE.KDM);
				for(int i =0; i < MiscNames.size(); ++i) 
				 {
					 if(MiscNames.get(i).toLowerCase().contains(event.getFocusedOption().getValue().toLowerCase()) || MiscNames.get(i).equalsIgnoreCase(event.getFocusedOption().getValue())) 
					 {
						subList.add(MiscNames.get(i)); 
					 }
				 }
				
				 // Convert to arrays
				 String [] subset = new String[subList.size()]; 
				 
				 subList.toArray(subset); 
				 
				 List<Command.Choice> options = Stream.of(subset).limit(25)
						 .filter(name -> name.startsWith(event.getFocusedOption().getValue()))
						 .map(name -> new Command.Choice(name, name))
						 .collect(Collectors.toList()); 
				 event.replyChoices(options).queue();   
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		 else if (event.getName().equals("smashpass") 
			 && ( event.getFocusedOption().getName().equals("character"))) 
		 {
			 CharacterSelection select = new CharacterSelection(conn); 
			 try 
			{
				
				 ArrayList<String> subList = new ArrayList<String>(); 
					MiscNames = select.getAllCharacterNames(GAMETYPE.SMASHPASS);
					for(int i =0; i < MiscNames.size(); ++i) 
					 {
						 if(MiscNames.get(i).toLowerCase().contains(event.getFocusedOption().getValue().toLowerCase()) || MiscNames.get(i).equalsIgnoreCase(event.getFocusedOption().getValue())) 
						 {
							subList.add(MiscNames.get(i)); 
						 }
					 }
					
					 // Convert to arrays
					 String [] subset = new String[subList.size()]; 
					 
					 subList.toArray(subset); 
					 
					 List<Command.Choice> options = Stream.of(subset).limit(25)
							 .filter(name -> name.startsWith(event.getFocusedOption().getValue()))
							 .map(name -> new Command.Choice(name, name))
							 .collect(Collectors.toList()); 
					 event.replyChoices(options).queue();   
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	}
		 
	
	
	@Override 
	public void onReady(ReadyEvent event) 
	{
		System.out.println("Bot start up"); 
	}
	
	
	@Override 
	public void onGuildReady(GuildReadyEvent event) 
	{
			
			// Add command on guild 
				List<CommandData> commandList = new ArrayList<CommandData>(); 
				
				// Create an option
				OptionData characterOption = new OptionData(OptionType.STRING, "character", "Enter a character name",true,true); 
				OptionData characterOptionSMP = new OptionData(OptionType.STRING, "character", "Enter a character name to smash or pass",false,true); 
				OptionData UserOption = new OptionData(OptionType.USER, "user", "Enter a user for the following command", false, false);
				OptionData UserOptionTrade = new OptionData(OptionType.USER, "tradee", "Enter a user to trade with ", true, false);  
				OptionData characterOptionOne = new OptionData(OptionType.STRING, "first", "Enter a first character",false,true);
				OptionData characterOptionTwo = new OptionData(OptionType.STRING, "second", "Enter a second character",false,true);
				OptionData characterOptionThree = new OptionData(OptionType.STRING, "third", "Enter a third character",false,true);
				OptionData optionSelection = new OptionData(OptionType.STRING, "type", "Choose to select", true, false); 
				
				OptionData name = new OptionData(OptionType.STRING, "name", "Enter your sona's name!", true, false);
				OptionData url = new OptionData(OptionType.STRING, "url", "Enter the imgur url which display's your sona's picture!", true, false);
				OptionData kdm = new OptionData(OptionType.STRING, "kdm", "sona in KDM game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F");  
				OptionData smashpass = new OptionData(OptionType.STRING, "smashpass", "sona in Smash or Pass game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F"); 
				OptionData simps = new OptionData(OptionType.STRING, "simps", "sona in simp game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F");
				OptionData  ships = new OptionData(OptionType.STRING, "ships", "sona in ships game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F"); 
				OptionData kins = new OptionData(OptionType.STRING, "kins", "sona in kins game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F"); 
				OptionData waifu = new OptionData(OptionType.STRING, "waifu", "sona in waifu game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F"); 
				

				
				commandList.add(Commands.slash("wikifull", "Display full wiki of the entered character").addOptions(characterOption));
				commandList.add(Commands.slash("wiki", "Display general information on entered character").addOptions(characterOption)); 
				commandList.add(Commands.slash("simps", "Return random character the caller simps for"));
				commandList.add(Commands.slash("smashpass", "Return character the user would smash or pass for").addOptions(characterOptionSMP)); 
				commandList.add(Commands.slash("kins", "Return random character the user kins for")); 
				commandList.add(Commands.slash("waifu", "Return your waifu or return another user's waifu").addOptions(UserOption));
				commandList.add(Commands.slash("waifutrade", "Offer to trade your waifu to another user").addOptions(UserOptionTrade)); 
				commandList.add(Commands.slash("userinfo", "Get general information on specified used").addOptions(UserOption)); 
				commandList.add(Commands.slash("kdm", "Game were you choose 3 characters to kill, date or marry").addOptions(characterOptionOne,characterOptionTwo, characterOptionThree));
				commandList.add(Commands.slash("wikiall", "Display all full wikis of the character on the database only HBAdmins can do this procedure").addOptions(optionSelection.addChoice("all", "all characters").addChoice("major", "major").addChoice("minor", "minor"))); 
				commandList.add(Commands.slash("ships", "Generates 2 random characters in a ship")); 
				commandList.add(Commands.slash("sona", "Return your or other user's sona").addOptions(UserOption)); 
				commandList.add(Commands.slash("insertsona", "Insert your sona add name, imgur url and games it can be part of!").addOptions(name,url,kdm,smashpass,simps,ships,kins,waifu)); 
				commandList.add(Commands.slash("removesona", "Remove sona from the server").addOptions(UserOption)); 
				event.getGuild().updateCommands().addCommands(commandList).queue();
				
	}
}
