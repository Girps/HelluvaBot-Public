package events;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandManger extends ListenerAdapter {

	private ArrayList<String> cmds; 
	private boolean debug; 
	private ExecutorService executor = null; 
	List<CommandData> commandList = null; 
	
	// constructor 
	public CommandManger ( ExecutorService executor)
	{
		this.executor = executor; 
		
		cmds = new ArrayList<String>(Arrays.asList("Collect command", "Frame command", "Guess command", "Kdm command", 
				"Simps command", "Smashpass command", "Sonas command", "UserInfo command", "Favorite command", "Waifu command", 
				"Wiki command" , "Oc command", "Terms Of Service"));
		debug = false; 
		
				// initalize commands
				 commandList = new ArrayList<CommandData>(); 
				
					
					// Create an option
					OptionData characterOption = new OptionData(OptionType.STRING, "character", "Enter a character name",true,true); 
					OptionData characterOptionSMP = new OptionData(OptionType.STRING, "character", "Enter a character name to smash or pass",false,true); 
					OptionData UserOption = new OptionData(OptionType.USER, "user", "Enter a user for the following command", false, false);
					OptionData UserOptionTrade = new OptionData(OptionType.USER, "tradee", "Enter a user to trade with ", true, false);  
					OptionData characterOptionOne = new OptionData(OptionType.STRING, "first", "Enter a first character",false,true);
					OptionData characterOptionTwo = new OptionData(OptionType.STRING, "second", "Enter a second character",false,true);
					OptionData characterOptionThree = new OptionData(OptionType.STRING, "third", "Enter a third character",false,true);
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
					
					// Optional fields 
					OptionData nameOp = new OptionData(OptionType.STRING, "name", "Enter your characters new name!", false, false);
					OptionData urlOp = new OptionData(OptionType.STRING, "url", "Enter the new imgur url which display's your sona's picture!", false, false);
					OptionData kdmOp = new OptionData(OptionType.STRING, "kdm", "sona in KDM game?", false, false)
							.addChoice("True", "T")
							.addChoice("False", "F");  
					OptionData smashpassOp = new OptionData(OptionType.STRING, "smashpass", "character in Smash or Pass game?", false, false)
							.addChoice("True", "T")
							.addChoice("False", "F"); 
					OptionData simpsOp = new OptionData(OptionType.STRING, "simps", "character in simp game?", false, false)
							.addChoice("True", "T")
							.addChoice("False", "F");
					OptionData  shipsOp = new OptionData(OptionType.STRING, "ships", "character in ships game?", false, false)
							.addChoice("True", "T")
							.addChoice("False", "F"); 
					OptionData kinsOp = new OptionData(OptionType.STRING, "kins", "character in kins game?", false, false)
							.addChoice("True", "T")
							.addChoice("False", "F"); 
					OptionData waifuOp = new OptionData(OptionType.STRING, "waifu", "character in waifu game?", false, false)
							.addChoice("True", "T")
							.addChoice("False", "F"); 
					OptionData favOp = new OptionData(OptionType.STRING, "favorite", "character in favorite list?", false, false)
							.addChoice("True", "T")
							.addChoice("False", "F"); 
					OptionData guessOp = new OptionData(OptionType.STRING, "guess", "character be in guess game?", false, false)
							.addChoice("True", "T")
							.addChoice("False", "F"); 
					OptionData collectOp = new OptionData(OptionType.STRING, "collect", "character be in collect game?", false, false)
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
					
					OptionData gift = new OptionData(OptionType.STRING, "gift", "your character to give" , true, true); 
					OptionData receiver = new OptionData(OptionType.USER, "receiver", "Enter a user to accept the gift", true, false);
					OptionData choice = new OptionData(OptionType.BOOLEAN, "permission" , "Enter true or false",true,false);
					
					OptionData amount = new OptionData(OptionType.INTEGER, "amount" , "Enter a valid integer",true,false);
					OptionData itemOptions = new OptionData(OptionType.STRING, "item", "Enter an item", true, true);  
					OptionData titleOptions = new OptionData(OptionType.STRING, "title", "image name to select",true,true); 
					//OptionData firstUserCollect= new OptionData(OptionType.USER, "gifter" , "Enter user to force gift from", true, false);
					OptionData characterForceGift = new OptionData(OptionType.STRING, "receiver-character", "Pick character to force gift" , true, true); 
					commandList.add(Commands.slash("set-default-image", "Get statistics on amount of characters collected").addOptions(characterOption,titleOptions) ); 
					commandList.add(Commands.slash("stats", "Get statistics on amount of characters collected")); 
					commandList.add(Commands.slash("search", "Search characters").addOptions(characterOption)); 
					commandList.add(Commands.slash("prices", "Prices of items and services you can buy!")); 
					commandList.add(Commands.slash("buy", "Buy following services").addOptions(itemOptions)); 
					commandList.add(Commands.slash("deposit", "Add cash to the balance").addOptions(amount)); 
					commandList.add(Commands.slash("withdraw", "Withdraw money from balance.").addOptions(amount)); 
					commandList.add(Commands.slash("balance", "Return your current balance.")); 
					commandList.add(Commands.slash("work", "Do a job and earn income!")); 
					commandList.add(Commands.slash("memes", "Returns a helluva boss meme!")); 
					commandList.add(Commands.slash("require-permission", "Require users to have the Helluva Permission role to insert OCs/Sonas into the bot!").addOptions(choice)); 
					commandList.add(Commands.slash("wiki-full", "Display full wiki of the entered character").addOptions(characterOption));
					commandList.add(Commands.slash("wiki", "Display general information on entered character").addOptions(characterOption)); 
					commandList.add(Commands.slash("simps", "Return random character the caller simps for"));
					commandList.add(Commands.slash("next-claim", "Return next claim time and number of claims left!"));
					commandList.add(Commands.slash("next-roll", "Return next roll time and number of rolls left!"));
					commandList.add(Commands.slash("update-oc", "Update fields of your oc of your choice!").addOptions(customCharacterOp2,nameOp,urlOp,kdmOp,smashpassOp,simpsOp,shipsOp,kinsOp,waifuOp, favOp,guessOp,collectOp));
					commandList.add(Commands.slash("update-sona", "Update fields of your sona you must pick at least one field for this command to work!").addOptions(nameOp,urlOp,kdmOp,smashpassOp,simpsOp,shipsOp,kinsOp,waifuOp, favOp,guessOp,collectOp)); 
					commandList.add(Commands.slash("smashpass", "Return character the user would smash or pass for").addOptions(characterOptionSMP)); 
					commandList.add(Commands.slash("kins", "Return random character the user kins for")); 
					commandList.add(Commands.slash("waifu", "Return your waifu or return another user's waifu").addOptions(UserOption));
					commandList.add(Commands.slash("waifu-trade", "Offer to trade your waifu to another user").addOptions(UserOptionTrade)); 
					commandList.add(Commands.slash("user-info", "Get general information on specified used").addOptions(UserOption)); 
					commandList.add(Commands.slash("kdm", "Game were you choose 3 characters to kill, date or marry").addOptions(characterOptionOne,characterOptionTwo, characterOptionThree));
					//commandList.add(Commands.slash("wikiall", "Display all full wikis of the character on the database only HBAdmins can do this procedure").addOptions(optionSelection.addChoice("all", "all characters").addChoice("major", "major").addChoice("minor", "minor"))); 
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
					commandList.add(Commands.slash("gift-collectable","Give a collectable to another user!").addOptions(gift,receiver)); 
					commandList.add(Commands.slash("sona-available","Check sona is available in following gamemodes!").addOptions(UserOption)); 
					commandList.add(Commands.slash("oc-available","Check oc is available in following gamemodes!").addOptions(customCharacterOp)); 
					commandList.add( Commands.slash("force-gift", "Force gift from one users collection to another").addOptions(requiredUserOp,characterForceGift,receiver)); 		
		
				
	}
	
	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event)
	{
		
		 // Check type of of command 
		 if( ( event.getName().equals("wiki-full") || event.getName().equals("wiki") ) ) 
		 { 
			 // Search the letter and get 25 possible options including that lettter 			 
			 CompletableFuture.supplyAsync( () -> 
			 {
				 CharacterSelection select = new CharacterSelection(); 
					ArrayList<String> names = null ; 
					names = select.getAllCharacterNames(GAMETYPE.WIKI,0);
					
				 return names; 
			 },this.executor).thenAccept((names) -> 
			 {
				List<Command.Choice> options = filteredCommands(event, names); 
				 event.replyChoices(options).queue();
			 }).exceptionally((ex) ->
			 {
				 ex.printStackTrace(); 
				 return null; 
			 }); 
		 }
		 
		 else if(event.getName().equals("help") || 
				 event.getFocusedOption().getName().equals("command") )  
		 {
			 CompletableFuture.runAsync(() -> 
			 {
				 ArrayList<String> subList = new ArrayList<String>(); 
				 for(int i =0; i < cmds.size(); ++i) 
				 {
					 if(cmds.get(i).toLowerCase().contains(event.getFocusedOption().getValue().toLowerCase()) || cmds.get(i).equalsIgnoreCase(event.getFocusedOption().getValue())) 
					 {
						subList.add(cmds.get(i)); 
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
			 },this.executor).exceptionally((ex) -> 
			 {
				 ex.printStackTrace(); 
				 return null; 
			 }); 
			 
		 } 
		 else if(event.getName().equals("kdm")  ||  event.getFocusedOption().getName().equals("first") || 
					 event.getFocusedOption().getName().equals("second") || event.getFocusedOption().getName().equals("third") )  
		 { 
			 CompletableFuture.supplyAsync(() ->
			 {
				 CharacterSelection select = new CharacterSelection(); 
				ArrayList<String> names = select.getAllCharacterNames(GAMETYPE.KDM,event.getGuild().getIdLong());
				return names; 
			 },this.executor).thenAccept( (names) -> 
			 {
				List<Command.Choice> options = filteredCommands(event, names); 
				 event.replyChoices(options).queue(); 
			 })
			 .exceptionally((ex)->
			 {
				 ex.printStackTrace(); 
				 return null; 
			 }); 
		 }
 		 else if (event.getName().equals("smashpass") 
			 && ( event.getFocusedOption().getName().equals("character"))) 
		 {
 			 
 			 CompletableFuture.supplyAsync( () -> 
 			 {
 				 CharacterSelection select = new CharacterSelection(); 
				 
					ArrayList<String> names = select.getAllCharacterNames(GAMETYPE.SMASHPASS,event.getGuild().getIdLong());
 				 return names; 
 			 },this.executor).thenAccept( (names) -> 
 			 {
 			 
 				List<Command.Choice> options = filteredCommands(event, names); 
				 event.replyChoices(options).queue(); 
				 event.replyChoices(options).queue();
 			 }).exceptionally( (ex) ->
 			 {
 				 ex.printStackTrace(); 
 				 return null;
 			 }); ; 
		 }
		 else if ( event.getName().equals("add-favorite") && event.getFocusedOption().getName().equals("character")  ) 
		 {
			 CompletableFuture.supplyAsync( () -> 
 			 {
 				 CharacterSelection select = new CharacterSelection(); 
				ArrayList<String> names = select.getAllCharacterNames(GAMETYPE.FAVORITES, event.getGuild().getIdLong());
 				 return names; 
 			 },this.executor).thenAccept( (names) -> 
 			 {
 				List<Command.Choice> options = filteredCommands(event, names); 
				 event.replyChoices(options).queue();
 			 }).exceptionally( (ex) ->
 			 {
 				 ex.printStackTrace(); 
 				 return null;
 			 }); ; 
		 }
		 else if ( ( event.getName().equals("remove-favorite")  || event.getName().equals("swap-favorite-rank") ) 
				 &&  ( event.getFocusedOption().getName().equals("character") || event.getFocusedOption().getName().equals("first-character") ||
						 event.getFocusedOption().getName().equals("second-character")) ) 
		 {
			 CompletableFuture.supplyAsync( () -> 
 			 {
 				 CharacterSelection select = new CharacterSelection(); 
				ArrayList<String> names = select.getFavListNames(event.getUser().getIdLong(), event.getGuild().getIdLong());
 				 return names; 
 			 },this.executor).thenAccept( (names) -> 
 			 {
 				List<Command.Choice> options = filteredCommands(event, names); 
				 event.replyChoices(options).queue();
 			 }).exceptionally( (ex) ->
 			 {
 				 ex.printStackTrace(); 
 				 return null;
 			 }); 
			 
		 }
		 else if (  ( event.getName().equals("update-oc") || event.getName().equals("my-oc") || event.getName().equals("remove-my-oc") || event.getName().equals("set-default-oc") || event.getName().equals("oc-available") ) &&   ( event.getFocusedOption().getName().equals("customcharacter")) ) 
		 {		 
			 CompletableFuture.supplyAsync( () -> 
			 {			 
				 Long id = event.getUser().getIdLong(); 
				 CharacterSelection select = new CharacterSelection(); 
				 ArrayList<String> names = select.getUsersOCName(id, event.getGuild().getIdLong()); 
				 return names; 
			 },this.executor).thenAccept( (names) -> 
			 {
				 List<Command.Choice> options = filteredCommands(event, names); 
				 event.replyChoices(options).queue();
			 }).exceptionally( (ex) ->
			 {
				 ex.printStackTrace(); 
				 return null;
			 });  
		 }
		 else if ( event.getName().equals("search-oc") || event.getName().equals("remove-user-oc")  &&
				 event.getFocusedOption().getName().equals("customcharacter") ) 
		 {
 					 
			 CompletableFuture.supplyAsync( () -> 
			 {			 
				 CharacterSelection select = new CharacterSelection(); 
				 ArrayList<String> names = select.getUsersOCName(Long.valueOf(event.getInteraction().getOptionsByName("user").get(0).getAsString()), event.getGuild().getIdLong()); 
				 return names; 
			 },this.executor).thenAccept( (names) -> 
			 {
				 List<Command.Choice> options = filteredCommands(event, names); 
				 event.replyChoices(options).queue();
			 }).exceptionally( (ex) ->
			 {
				 ex.printStackTrace(); 
				 return null;
			 });  

			 
		 }
		 else if ( event.getName().equals("release")  &&  event.getFocusedOption().getName().equals("character") ) 
		 {	 
			 CompletableFuture.supplyAsync( () -> 
			 {			 
				 CharacterSelection select = new CharacterSelection(); 
				 ArrayList<String> names = select.getCollectNamesOfUser(event.getUser().getIdLong(), event.getGuild().getIdLong()); 
				 return names; 
			 },this.executor).thenAccept( (names) -> 
			 {
				 List<Command.Choice> options = filteredCommands(event, names); 
				 event.replyChoices(options).queue();
			 }).exceptionally( (ex) ->
			 {
				 ex.printStackTrace(); 
				 return null;
			 });   
		 }
		 else if ( event.getName().equals("force-release") &&   event.getFocusedOption().getName().equals("character") ) 
		 {
			 CompletableFuture.supplyAsync( () -> 
			 {			 
				 CharacterSelection select = new CharacterSelection(); 
				 ArrayList<String> names = select.getCollectNamesOfUser(Long.valueOf(event.getInteraction().getOptionsByName("user").get(0).getAsString()), event.getGuild().getIdLong()); 
				 return names; 
			 },this.executor).thenAccept( (names) -> 
			 {
				 List<Command.Choice> options = filteredCommands(event, names); 
				 event.replyChoices(options).queue();
			 }).exceptionally( (ex) ->
			 {
				 ex.printStackTrace(); 
				 return null;
			 });   
		 }
		 else if (  event.getName().equals("force-gift") || ( event.getName().equals("gift-collectable") || event.getName().equals("collect-trade")  || event.getName().equals("set-default-collect")  )  &&   
				 ( event.getFocusedOption().getName().equals("trader-character") || event.getFocusedOption().getName().equals("tradee-character") 
				 || event.getFocusedOption().getName().equals("character") || event.getFocusedOption().getName().equals("gift")  || event.getFocusedOption().getName().equals("receiver-character") ) ) 
		 {
			 
				// Needs to be fixed 
			 CompletableFuture.supplyAsync( () -> 
			 {		
				 ArrayList<String> names = null; 
				 CharacterSelection select = new CharacterSelection(); 
				 if (event.getInteraction().getOption("user") != null)
				 {  
					 names = select.getCollectNamesOfUser(Long.valueOf(event.getInteraction().getOptionsByName("user").get(0).getAsString()), event.getGuild().getIdLong());
				 }
				 else 
				 {
					 names = select.getCollectNamesOfUser(event.getUser().getIdLong(), event.getGuild().getIdLong());
				 }
				 return names; 
			 },this.executor).thenAccept( (names) -> 
			 {
				 List<Command.Choice> options = filteredCommands(event, names); 
				 event.replyChoices(options).queue();
			 }).exceptionally( (ex) ->
			 {
				 ex.printStackTrace(); 
				 return null;
			 });

		 }
		 else if (  ( event.getName().equals("add-wish") || event.getName().equals("search") || event.getName().equals("set-default-image"))
				 &&   event.getFocusedOption().getName().equals("character")  ) 
		 {
			 CompletableFuture.supplyAsync( () -> 
			 {			 
				 CharacterSelection select = new CharacterSelection(); 
				 ArrayList<String> names = select.getAllCharacterNames(GAMETYPE.COLLECT, event.getGuild().getIdLong());  
				 return names; 
			 },this.executor).thenAccept( (names) -> 
			 {
				 List<Command.Choice> options = filteredCommands(event, names); 
				 event.replyChoices(options).queue();
			 }).exceptionally( (ex) ->
			 {
				 ex.printStackTrace(); 
				 return null;
			 });		 
		 }
		 else if ( event.getName().equals("remove-wish") &&   event.getFocusedOption().getName().equals("character")  ) 
		 {		  		
			 CompletableFuture.supplyAsync( () -> 
			 {			 
				 CharacterSelection select = new CharacterSelection(); 
				 ArrayList<String> names = select.getWishListNames(event.getUser().getIdLong(), event.getGuild().getIdLong());  
				 return names; 
			 },this.executor).thenAccept( (names) -> 
			 {
				 List<Command.Choice> options = filteredCommands(event, names); 
				 event.replyChoices(options).queue();
			 }).exceptionally( (ex) ->
			 {
				 ex.printStackTrace(); 
				 return null;
			 });
		 } 
		 else if (event.getName().equals("buy") ) 
		 {
			 CompletableFuture.supplyAsync( () -> 
			 {
				 CharacterSelection select = new CharacterSelection(); 
				 HashMap<String, Pair<String, Integer>> map = select.getItems(); 
				 ArrayList<String> items = new ArrayList<String>(); 
				 map.forEach( (key, pair) -> 
				 {
					 items.add(key); 
				 }); 
				 
				 return items; 
				 
			 }, this.executor).thenAccept((items) -> 
			 {
				 List<Command.Choice> options = filteredCommands(event,items);
				 event.replyChoices(options).queue(); 
			 }).exceptionally((ex) -> 
			 {
				 ex.printStackTrace(); 
				 return null; 
			 });
		 }
		 if ( event.getName().equals("set-default-image") && 
				  event.getFocusedOption().getName().equals("title") ) 
		 {
			 // if character is not empty and theres a title option
			 CompletableFuture.supplyAsync( () -> 
			 {
				 
				 // get fields of the chosen character 
				 CharacterSelection select = new CharacterSelection(); 
				 String name = event.getInteraction().getOption("character").getAsString(); 
				 JSONArray jsonArray = select.getCharacterJsonImages(name); 
				 ArrayList<String> imageList =  new ArrayList<String>(); 
				 for(int i =0 ; i <  jsonArray.length(); i++) 
				 {
					 JSONObject elem = jsonArray.getJSONObject(i);  
					 String res  = Integer.valueOf(i+1).toString(); 
					 res += "|"; 
					 res += ( !elem.get("art_name").equals("") ) ? elem.get("art_name") : "Default" ;  
					 res += "|"; 
					 res += ( !elem.get("author_name").equals("")) ? elem.get("author_name") : "N/A";
					 res+= "|"; 
					 res += ( !elem.get("url").equals("")) ? elem.get("url") : "N/A";

					 imageList.add(res); 
				 }
				 
				 return imageList; 
				 
			 }, this.executor).thenAccept((items) -> 
			 {
				 List<Command.Choice> options = filteredCommands(event,items);
				 event.replyChoices(options).queue(); 
			 }).exceptionally((ex) -> 
			 {
				 ex.printStackTrace(); 
				 return null; 
			 });
		 }
	}
		 
	public List<Command.Choice> filteredCommands(CommandAutoCompleteInteractionEvent event , ArrayList<String> names)
	{
		
		 ArrayList<String> subList = new ArrayList<String>(); 
		 for(int i =0; i < names.size(); ++i) 
		 {
			 if(names.get(i).toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()) ) 
			 {
				subList.add(names.get(i)); 
			 }
		 }
		 // Convert to arrays
		 String [] subset = new String[subList.size()]; 
		 
		 subList.toArray(subset); 
		 
		 List<Command.Choice> options = Stream.of(subset).limit(25)
				 .filter(name -> name.toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()))
				 .map(name -> new Command.Choice(name, name))
				 .collect(Collectors.toList()); 
		return options; 
	}; 
	
	// Up
	@Override
	public void onGuildJoin(GuildJoinEvent event) 
	{
	}
	
	// Update Global commands up to unlimited amount of servers 
	public void onReady(ReadyEvent event) 
	{		
	}
	
	public void onStatusChange(StatusChangeEvent event)
	{
		switch(event.getNewStatus())
		{
		case CONNECTING_TO_WEBSOCKET : 	
			System.out.println("Socket connecting"); 
			event.getJDA().updateCommands().addCommands(commandList).queue(); 
		break; 
			default: 
		break; 
		}
	}
	
	// For first 100 servers 
	@Override 
	public void onGuildReady(GuildReadyEvent event) 
	{ 
	}
}
