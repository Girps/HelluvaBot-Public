package events;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandManger extends ListenerAdapter {


	private static ArrayList<String> wikiNames;
	private ArrayList<String> MiscNames; 
	private ArrayList<String> cmds; 
	private boolean debug  ; 
	public CommandManger ( )
	{
		
		cmds = new ArrayList<String>(Arrays.asList("Collect command", "Frame command", "Guess command", "Kdm command", 
				"Simps command", "Smashpass command", "Sonas command", "UserInfo command", "Favorite command", "Waifu command", 
				"Wiki command" , "Oc command", "Credits"));
		debug = false; 
	}
	
	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event)
	{
		
		 // Check type of of command 
		 if( ( event.getName().equals("wiki-full") || event.getName().equals("wiki") ) ) 
		 { 
			 // Search the letter and get 25 possible options including that lettter 
			 
			 CharacterSelection select = new CharacterSelection(); 
				ArrayList<String> names = null ; 
				 names = select.getAllCharacterNames(GAMETYPE.WIKI,0);
				wikiNames = names; 
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
		 
		 else if(event.getName().equals("help") || 
				 event.getFocusedOption().getName().equals("command") )  
	 {
		
		 ArrayList<String> subList = new ArrayList<String>(); 
		MiscNames = cmds; 
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
		 
	 } 
		 else if(event.getName().equals("kdm")  ||  event.getFocusedOption().getName().equals("first") || 
					 event.getFocusedOption().getName().equals("second") || event.getFocusedOption().getName().equals("third") )  
		 {
			 CharacterSelection select = new CharacterSelection(); 
			
				 ArrayList<String> subList = new ArrayList<String>(); 
				MiscNames = select.getAllCharacterNames(GAMETYPE.KDM,event.getGuild().getIdLong());
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
		
		 }
 		 else if (event.getName().equals("smashpass") 
			 && ( event.getFocusedOption().getName().equals("character"))) 
		 {
			 CharacterSelection select = new CharacterSelection(); 
			
				 ArrayList<String> subList = new ArrayList<String>(); 
					MiscNames = select.getAllCharacterNames(GAMETYPE.SMASHPASS,event.getGuild().getIdLong());
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
				
			
		 }
		 else if ( event.getName().equals("add-favorite") && event.getFocusedOption().getName().equals("character")  ) 
		 {
			 CharacterSelection select = new CharacterSelection();  
			
				
				 ArrayList<String> subList = new ArrayList<String>(); 
					MiscNames = select.getAllCharacterNames(GAMETYPE.FAVORITES, event.getGuild().getIdLong());
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
				
		
		 }
		 else if ( ( event.getName().equals("remove-favorite")  || event.getName().equals("swap-favorite-rank") ) 
				 &&  ( event.getFocusedOption().getName().equals("character") || event.getFocusedOption().getName().equals("first-character") ||
						 event.getFocusedOption().getName().equals("second-character")) ) 
		 {
			 CharacterSelection select = new CharacterSelection();  
			
				
				 ArrayList<String> subList = new ArrayList<String>(); 
					MiscNames = select.getFavListNames(event.getUser().getIdLong(), event.getGuild().getIdLong());
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
				
		
			 
		 }
		 else if ( event.getName().equals("my-oc") || event.getName().equals("remove-my-oc") || event.getName().equals("set-default-oc") &&   event.getFocusedOption().getName().equals("customcharacter") ) 
		 {
			 Long id = event.getUser().getIdLong(); 
			 CharacterSelection select = new CharacterSelection();  
			
				 ArrayList<String> subList = new ArrayList<String>(); 
					MiscNames = select.getUsersOCName(id, event.getGuild().getIdLong()); 
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
				
			
			 
		 }
		 else if ( event.getName().equals("search-oc") || event.getName().equals("remove-user-oc")  &&   event.getFocusedOption().getName().equals("customcharacter") ) 
		 {
			 CharacterSelection select = new CharacterSelection();  
			
			
				
				 ArrayList<String> subList = new ArrayList<String>();  
					MiscNames = select.getUsersOCName(Long.valueOf(event.getInteraction().getOptionsByName("user").get(0).getAsString()), event.getGuild().getIdLong()); 
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
			
			 
		 }
		 else if ( event.getName().equals("release")  &&  event.getFocusedOption().getName().equals("character") ) 
		 {
			 CharacterSelection select = new CharacterSelection();  
			
			
				 ArrayList<String> subList = new ArrayList<String>();  
					MiscNames = select.getCollectNamesOfUser(event.getUser().getIdLong(), event.getGuild().getIdLong()); 
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
				
		
			 
		 }
		 else if ( event.getName().equals("force-release") &&   event.getFocusedOption().getName().equals("character") ) 
		 {
			 CharacterSelection select = new CharacterSelection();  
			
			
				 ArrayList<String> subList = new ArrayList<String>();  
					MiscNames = select.getCollectNamesOfUser(Long.valueOf(event.getInteraction().getOptionsByName("user").get(0).getAsString()), event.getGuild().getIdLong()); 
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
				
		
			 
		 }
		 else if (  ( event.getName().equals("collect-trade")  || event.getName().equals("set-default-collect")  ) &&   ( event.getFocusedOption().getName().equals("trader-character") || event.getFocusedOption().getName().equals("tradee-character") 
				 || event.getFocusedOption().getName().equals("character") ) ) 
		 {
			 CharacterSelection select = new CharacterSelection();  
			
			
				 ArrayList<String> subList = new ArrayList<String>(); 
				 if(event.getInteraction().getOption("user") != null) 
				 { 
					MiscNames = select.getCollectNamesOfUser(Long.valueOf(event.getInteraction().getOptionsByName("user").get(0).getAsString()), event.getGuild().getIdLong()); 
				 }
				 else 
				 {
					 MiscNames = select.getCollectNamesOfUser(event.getUser().getIdLong(), event.getGuild().getIdLong());
				 }
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
				
			
			 
		 }
		 else if ( event.getName().equals("add-wish") &&   event.getFocusedOption().getName().equals("character")  ) 
		 {
			 CharacterSelection select = new CharacterSelection();  

			
				
				 ArrayList<String> subList = new ArrayList<String>(); 
				
					 MiscNames = select.getAllCharacterNames(GAMETYPE.COLLECT, event.getGuild().getIdLong());

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
		
			 
		 }
		 else if ( event.getName().equals("remove-wish") &&   event.getFocusedOption().getName().equals("character")  ) 
		 {
			 CharacterSelection select = new CharacterSelection();  
			
				
				 ArrayList<String> subList = new ArrayList<String>(); 
				
					 MiscNames = select.getWishListNames(event.getUser().getIdLong(), event.getGuild().getIdLong()); 
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
		 }
	}
		 
	
	
	
	@Override
	public void onGuildJoin(GuildJoinEvent event) 
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
		OptionData requiredUserOp = new OptionData(OptionType.USER, "user", "Enter a user", true, false);  
		OptionData name = new OptionData(OptionType.STRING, "name", "Enter your sona's name!", true, false);
		OptionData url = new OptionData(OptionType.STRING, "url", "Enter the imgur url which display's your sona's picture!", true, false);
		OptionData kdm = new OptionData(OptionType.STRING, "kdm", "sona in KDM game?", true, false)
				.addChoice("True", "T")
				.addChoice("False", "F");  
		OptionData smashpass = new OptionData(OptionType.STRING, "smashpass", "character in Smash or Pass game?", true, false)
				.addChoice("True", "T")
				.addChoice("False", "F"); 
		OptionData simps = new OptionData(OptionType.STRING, "simps", "character in simp game?", true, false)
				.addChoice("True", "T")
				.addChoice("False", "F");
		OptionData  ships = new OptionData(OptionType.STRING, "ships", "character in ships game?", true, false)
				.addChoice("True", "T")
				.addChoice("False", "F"); 
		OptionData kins = new OptionData(OptionType.STRING, "kins", "character in kins game?", true, false)
				.addChoice("True", "T")
				.addChoice("False", "F"); 
		OptionData waifu = new OptionData(OptionType.STRING, "waifu", "character in waifu game?", true, false)
				.addChoice("True", "T")
				.addChoice("False", "F"); 
		OptionData fav = new OptionData(OptionType.STRING, "favorite", "character in favorite list?", true, false)
				.addChoice("True", "T")
				.addChoice("False", "F"); 
		OptionData guess = new OptionData(OptionType.STRING, "guess", "character be in guess game?", true, false)
				.addChoice("True", "T")
				.addChoice("False", "F"); 
		OptionData collect = new OptionData(OptionType.STRING, "collect", "character be in collect game?", true, false)
				.addChoice("True", "T")
				.addChoice("False", "F"); 
		
		
		OptionData removeCharacter = new OptionData(OptionType.STRING, "character", "character to remove" , true, true); 
		OptionData removeCollectChar = new OptionData(OptionType.STRING, "character", "collect character to remove" , true, true); 
		OptionData CollectChar = new OptionData(OptionType.STRING, "character", "to set as default image" , true, true); 
		OptionData WishChar = new OptionData(OptionType.STRING, "character", "add character to wishlist" , true, true); 

		OptionData UserOneCollectChar = new OptionData(OptionType.STRING, "trader-character", "your character to trade" , true, true); 
		OptionData UserTwoCollectChar = new OptionData(OptionType.STRING, "tradee-character", "user's character to trade with" , true, true); 
		
		OptionData	title = new OptionData(OptionType.STRING, "title", "Name of your list", true, false); 
		
		OptionData customCharacterOp = new OptionData(OptionType.STRING, "customcharacter", "Original Character Name", false, true); 
		OptionData customCharacterOp2 = new OptionData(OptionType.STRING, "customcharacter", "Original Character Name", true, true); 
		
		OptionData favOne =  new OptionData(OptionType.STRING, "first-character", "Favorite character", true, true ); 
		OptionData favTwo =  new OptionData(OptionType.STRING, "second-character", "Favorite character", true, true ); 

		OptionData cmd =  new OptionData(OptionType.STRING, "command", "Main commands", true, true ); 

		commandList.add(Commands.slash("wiki-full", "Display full wiki of the entered character").addOptions(characterOption));
		commandList.add(Commands.slash("wiki", "Display general information on entered character").addOptions(characterOption)); 
		commandList.add(Commands.slash("simps", "Return random character the caller simps for"));
		commandList.add(Commands.slash("smashpass", "Return character the user would smash or pass for").addOptions(characterOptionSMP)); 
		commandList.add(Commands.slash("kins", "Return random character the user kins for")); 
		commandList.add(Commands.slash("waifu", "Return your waifu or return another user's waifu").addOptions(UserOption));
		commandList.add(Commands.slash("waifu-trade", "Offer to trade your waifu to another user").addOptions(UserOptionTrade)); 
		commandList.add(Commands.slash("user-info", "Get general information on specified used").addOptions(UserOption)); 
		commandList.add(Commands.slash("kdm", "Game were you choose 3 characters to kill, date or marry").addOptions(characterOptionOne,characterOptionTwo, characterOptionThree));
		commandList.add(Commands.slash("wikiall", "Display all full wikis of the character on the database only HBAdmins can do this procedure").addOptions(optionSelection.addChoice("all", "all characters").addChoice("major", "major").addChoice("minor", "minor"))); 
		commandList.add(Commands.slash("ships", "Generates 2 random characters in a ship")); 
		commandList.add(Commands.slash("sona", "Return your or other user's sona").addOptions(UserOption)); 
		commandList.add(Commands.slash("insert-sona", "Insert your sona add name, imgur url and games it can be part of!").addOptions(name,url,kdm,smashpass,simps,ships,kins,waifu, fav,guess,collect)); 
		commandList.add(Commands.slash("remove-sona", "Remove sona from the server").addOptions(UserOption)); 
		
		commandList.add(Commands.slash("add-favorite", "Insert your top 10 favorite characters!").addOptions(characterOption));  
		commandList.add(Commands.slash("favorites", "Returns a list of your favorite characters").addOptions(UserOption)); 
		commandList.add(Commands.slash("remove-favorite", "Remove a specific character from your list").addOptions(removeCharacter)); 
		commandList.add(Commands.slash("change-favorites-title", "Select a number to update a character with").addOptions(title)); 
		commandList.add(Commands.slash("clear-favorites", "Clears your list of favorite characters")); 
		
		commandList.add(Commands.slash("insert-oc", "Insert an original character to current server you can only have 10 ocs per server!").addOptions(name,url,kdm,smashpass,simps,ships,kins,waifu, fav, guess, collect)); 
		commandList.add(Commands.slash("remove-all-ocs","Remove all your ocs in this server" ).addOptions(UserOption)); 
		commandList.add(Commands.slash("my-oc", "Enter name of your original character").addOptions(customCharacterOp)); 
		commandList.add(Commands.slash("search-oc", "Enter name of original character").addOptions(requiredUserOp, customCharacterOp));
		commandList.add(Commands.slash("remove-my-oc", "Remove your specific OC from this server").addOptions(customCharacterOp2)); 
		commandList.add(Commands.slash("guess", "Image of character returned for you to guess")); 
		commandList.add(Commands.slash("frame", "Get random frame from helluva boss"));
		commandList.add(Commands.slash("remove-user-oc","Admin command to remova another user's oc").addOptions(requiredUserOp,customCharacterOp)); 
		
		commandList.add(Commands.slash("roll","Roll and a random character to claim!")); 
		commandList.add(Commands.slash("collection","Return your collection from collect game" ).addOptions(UserOption)); 
		commandList.add(Commands.slash("reset-collect","Admin command to reset the collect game" )); 
		commandList.add(Commands.slash("release","Release a character from their collect list " ).addOptions(removeCollectChar)); 
		commandList.add(Commands.slash("force-release","Admin command to remova another user's collect character").addOptions(requiredUserOp,removeCollectChar)); 
		commandList.add(Commands.slash("collect-trade","Trade collectible with another user!").addOptions(UserOneCollectChar,requiredUserOp,UserTwoCollectChar)); 
		commandList.add(Commands.slash("set-default-collect","Set default image in collect list!").addOptions(CollectChar)); 
		commandList.add(Commands.slash("wish-list","Get wish list!").addOptions(UserOption)); 
		commandList.add(Commands.slash("add-wish","Add a character to the wishlist!").addOptions(WishChar)); 
		commandList.add(Commands.slash("remove-wish","Add a character to the wishlist!").addOptions(WishChar)); 
		commandList.add(Commands.slash("clear-wishes","Remove all wishes from your wishlist!")); 
		commandList.add(Commands.slash("swap-favorite-rank","Swap rank of each favorite character!").addOptions(favOne, favTwo)); 
		commandList.add(Commands.slash("set-default-oc","Set default character picture in your oc!").addOptions(customCharacterOp2)); 
		commandList.add(Commands.slash("help","Get infromation about each command").addOptions(cmd)); 

		if(!debug) 
		{ 
			event.getJDA().updateCommands().addCommands(commandList).queue();
		}
		else 
		{
			System.out.println("DEBUG"); 

			event.getGuild().updateCommands().addCommands(commandList).queue();

		}
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
				OptionData requiredUserOp = new OptionData(OptionType.USER, "user", "Enter a user", true, false);  
				OptionData name = new OptionData(OptionType.STRING, "name", "Enter your sona's name!", true, false);
				OptionData url = new OptionData(OptionType.STRING, "url", "Enter the imgur url which display's your sona's picture!", true, false);
				OptionData kdm = new OptionData(OptionType.STRING, "kdm", "sona in KDM game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F");  
				OptionData smashpass = new OptionData(OptionType.STRING, "smashpass", "character in Smash or Pass game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F"); 
				OptionData simps = new OptionData(OptionType.STRING, "simps", "character in simp game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F");
				OptionData  ships = new OptionData(OptionType.STRING, "ships", "character in ships game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F"); 
				OptionData kins = new OptionData(OptionType.STRING, "kins", "character in kins game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F"); 
				OptionData waifu = new OptionData(OptionType.STRING, "waifu", "character in waifu game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F"); 
				OptionData fav = new OptionData(OptionType.STRING, "favorite", "character in favorite list?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F"); 
				OptionData guess = new OptionData(OptionType.STRING, "guess", "character be in guess game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F"); 
				OptionData collect = new OptionData(OptionType.STRING, "collect", "character be in collect game?", true, false)
						.addChoice("True", "T")
						.addChoice("False", "F"); 
				
				
				OptionData removeCharacter = new OptionData(OptionType.STRING, "character", "character to remove" , true, true); 
				OptionData removeCollectChar = new OptionData(OptionType.STRING, "character", "collect character to remove" , true, true); 
				OptionData CollectChar = new OptionData(OptionType.STRING, "character", "to set as default image" , true, true); 
				OptionData WishChar = new OptionData(OptionType.STRING, "character", "add character to wishlist" , true, true); 

				OptionData UserOneCollectChar = new OptionData(OptionType.STRING, "trader-character", "your character to trade" , true, true); 
				OptionData UserTwoCollectChar = new OptionData(OptionType.STRING, "tradee-character", "user's character to trade with" , true, true); 
				
				OptionData	title = new OptionData(OptionType.STRING, "title", "Name of your list", true, false); 
				
				OptionData customCharacterOp = new OptionData(OptionType.STRING, "customcharacter", "Original Character Name", false, true); 
				OptionData customCharacterOp2 = new OptionData(OptionType.STRING, "customcharacter", "Original Character Name", true, true); 
				
				OptionData favOne =  new OptionData(OptionType.STRING, "first-character", "Favorite character", true, true ); 
				OptionData favTwo =  new OptionData(OptionType.STRING, "second-character", "Favorite character", true, true ); 
				OptionData cmd =  new OptionData(OptionType.STRING, "command", "Main commands", true, true ); 

				
				commandList.add(Commands.slash("wiki-full", "Display full wiki of the entered character").addOptions(characterOption));
				commandList.add(Commands.slash("wiki", "Display general information on entered character").addOptions(characterOption)); 
				commandList.add(Commands.slash("simps", "Return random character the caller simps for"));
				commandList.add(Commands.slash("smashpass", "Return character the user would smash or pass for").addOptions(characterOptionSMP)); 
				commandList.add(Commands.slash("kins", "Return random character the user kins for")); 
				commandList.add(Commands.slash("waifu", "Return your waifu or return another user's waifu").addOptions(UserOption));
				commandList.add(Commands.slash("waifu-trade", "Offer to trade your waifu to another user").addOptions(UserOptionTrade)); 
				commandList.add(Commands.slash("user-info", "Get general information on specified used").addOptions(UserOption)); 
				commandList.add(Commands.slash("kdm", "Game were you choose 3 characters to kill, date or marry").addOptions(characterOptionOne,characterOptionTwo, characterOptionThree));
				commandList.add(Commands.slash("wikiall", "Display all full wikis of the character on the database only HBAdmins can do this procedure").addOptions(optionSelection.addChoice("all", "all characters").addChoice("major", "major").addChoice("minor", "minor"))); 
				commandList.add(Commands.slash("ships", "Generates 2 random characters in a ship")); 
				commandList.add(Commands.slash("sona", "Return your or other user's sona").addOptions(UserOption)); 
				commandList.add(Commands.slash("insert-sona", "Insert your sona add name, imgur url and games it can be part of!").addOptions(name,url,kdm,smashpass,simps,ships,kins,waifu, fav,guess,collect)); 
				commandList.add(Commands.slash("remove-sona", "Remove sona from the server").addOptions(UserOption)); 
				
				commandList.add(Commands.slash("add-favorite", "Insert your top 10 favorite characters!").addOptions(characterOption));  
				commandList.add(Commands.slash("favorites", "Returns a list of your favorite characters").addOptions(UserOption)); 
				commandList.add(Commands.slash("remove-favorite", "Remove a specific character from your list").addOptions(removeCharacter)); 
				commandList.add(Commands.slash("change-favorites-title", "Select a number to update a character with").addOptions(title)); 
				commandList.add(Commands.slash("clear-favorites", "Clears your list of favorite characters")); 
				
				commandList.add(Commands.slash("insert-oc", "Insert an original character to current server you can only have 10 ocs per server!").addOptions(name,url,kdm,smashpass,simps,ships,kins,waifu, fav, guess, collect)); 
				commandList.add(Commands.slash("remove-all-ocs","Remove all your ocs in this server" ).addOptions(UserOption)); 
				commandList.add(Commands.slash("my-oc", "Enter name of your original character").addOptions(customCharacterOp)); 
				commandList.add(Commands.slash("search-oc", "Enter name of original character").addOptions(requiredUserOp, customCharacterOp));
				commandList.add(Commands.slash("remove-my-oc", "Remove your specific OC from this server").addOptions(customCharacterOp2)); 
				commandList.add(Commands.slash("guess", "Image of character returned for you to guess")); 
				commandList.add(Commands.slash("frame", "Get random frame from helluva boss"));
				commandList.add(Commands.slash("remove-user-oc","Admin command to remova another user's oc").addOptions(requiredUserOp,customCharacterOp)); 
				
				commandList.add(Commands.slash("roll","Roll and a random character to claim!")); 
				commandList.add(Commands.slash("collection","Return your collection from collect game" ).addOptions(UserOption)); 
				commandList.add(Commands.slash("reset-collect","Admin command to reset the collect game" )); 
				commandList.add(Commands.slash("release","Release a character from their collect list " ).addOptions(removeCollectChar)); 
				commandList.add(Commands.slash("force-release","Admin command to remova another user's collect character").addOptions(requiredUserOp,removeCollectChar)); 
				commandList.add(Commands.slash("collect-trade","Trade collectible with another user!").addOptions(UserOneCollectChar,requiredUserOp,UserTwoCollectChar)); 
				commandList.add(Commands.slash("set-default-collect","Set default image in collect list!").addOptions(CollectChar)); 
				commandList.add(Commands.slash("wish-list","Get wish list!").addOptions(UserOption)); 
				commandList.add(Commands.slash("add-wish","Add a character to the wishlist!").addOptions(WishChar)); 
				commandList.add(Commands.slash("remove-wish","Add a character to the wishlist!").addOptions(WishChar)); 
				commandList.add(Commands.slash("clear-wishes","Remove all wishes from your wishlist!")); 
				commandList.add(Commands.slash("swap-favorite-rank","Swap rank of each favorite character!").addOptions(favOne, favTwo)); 
				commandList.add(Commands.slash("set-default-oc","Set default character picture in your oc!").addOptions(customCharacterOp2)); 
				commandList.add(Commands.slash("help","Get infromation about each command").addOptions(cmd)); 
				if(!debug) 
				{ 
					event.getJDA().updateCommands().addCommands(commandList).queue();
				}
				else 
				{
					System.out.println("DEBUG"); 
					event.getGuild().updateCommands().addCommands(commandList).queue();

				}
				
	}
}
