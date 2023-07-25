package events;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HelpCommand extends ListenerAdapter	
{
	public HelpCommand() 
	{
	} 
	
	
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		if(event.getName().equals("help")) 
		{
			String command = event.getOption("command").getAsString(); 
			event.deferReply().queue(); 
			EmbedBuilder builder = null; 
			switch (command) 
			{
				case "Collect command" :
				{
					builder = getEmbed(1); 
				}
					break; 
				case "Frame command" : 
				{
					builder = getEmbed(2);

				}				
				break; 
				case "Guess command" : 
				{
					builder = getEmbed(3);

				}
				break;
				case "Kdm command" : 
				{
					builder =  getEmbed(4); 
				}
				break;
				case "Kins command": 
				{
					builder = getEmbed(5); 

				}
				break; 
				case "Ships command":
				{
					builder =  getEmbed(6); 

				}
					break; 
				case "Simps command": 
				{
					builder = getEmbed(7); 

				}
				break; 
				case "Smashpass command":
				{
					builder = getEmbed(8); 


				}
				break;
				case "Sonas command":
				{
					builder = getEmbed(9); 

				}
					break; 
				case "UserInfo command":
				{
					builder = getEmbed(10); 


				}
					break;
				case "Favorite command":
				{
					builder = getEmbed(11); 

				}
				break;
				case "Waifu command": 
				{
					builder = getEmbed(12); 

				}
				break; 
				case "Wiki command":
				{
					builder = getEmbed(13); 

				}
					break; 
				case "Oc command" : 
				{
					builder = getEmbed(14); 
					break; 
				}
				case "Credits" : 
				{
					builder = getEmbed(15);
				}
					break; 
					default : 
						event.getHook().sendMessage("Something went wrong!").queue(); 
						break; 
			}
			builder.setColor(Color.RED); 
			builder.setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl()); 
			event.getHook().sendMessageEmbeds(builder.build()).queue(); 

		}
	}
	
	
	public EmbedBuilder getEmbed(int index) 
	{
		EmbedBuilder builder = new EmbedBuilder(); 

		switch (index)
		{ 
		case 1: 
				builder.setTitle("Collect commands list"); 
				builder.addField("/roll", "Returns a random character for a player to claim within a 15 second interval." 
						+ "Each player has 3 rolls every hour it resets!", false) ;
				builder.addField("/collection [user]", "Returns list of characters claimed in collect game " + 
							"optional arguement [user] to return another players list of character claimed in the game. Limit"
							+ " of 30 characters can be on a player's collection list.", false); 
				builder.addField("/reset-collect"," @Helluva Admin only command to reset collect game. Reset clears all collection list and "
						+ "releases characters back to game and resets all players claims and rolls." ,false); 
				builder.addField("","",false); 
				builder.addField("/set-default-collect", "Sets default image of users collect list.",false); 
				builder.addField("/wish-list [user]","Returns users wishlist. Any characters rolled will @ the user."
						+ " Optional [user] argument will return another players wish list.",false); 
				builder.addField("/add-wish <character>"," Adds a character to a player's wishlist. Player will be @ when "
						+ "that character is rolled.Limit of 5 characters can be on the wishlist.",false); 
				builder.addField("/clear-wishes","Removes all player's characters in wishlist",false); 
				builder.addField("/remove-wish <character>","Remove a specific character player's wish list "
						+ "<character> is required to complete this command",false); 
				builder.addField("","",false); 
				builder.addField("/release <character>", "Remove character from your collection list. This will release the character "
						+ "back to the collect game to be claimed by other players.",false); 
				builder.addField("/force-release <user> <character>","@Helluva Admin only command to force a user to release a character "
						+ "from their collection. Required arguments <user>, <character>",false); 
		break; 
		case 2:
			builder.addField("/frame", " Returns a random frame from a helluva boss or hazbin hotel episode.", false); 	
			break; 
		case 3 : 
			builder.addField("/guess", " Returns a random character embed for the player to guess the character name.", false); 	 
			break; 
		case 4 : 
			builder.addField("/kdm", "Returns 3 random characters for the player to decide to kill, date and marry.", false); 
			builder.addField("/kdm [characterOne] [characterTwo] [characterThree]", "Returns 3 random characters for the player to decide to kill, date and marry."
					+ " Player has the option to choose those 3 characters with arguments [characterOne] [characterTwo] [characterThree]", false); 	
			break; 
		case 5 : 
			builder.addField("/kins","Returns a random character the player kins for.",false); 
			break; 
		case 6: 
			builder.addField("/ships", "Returns a random ships between 2 characters.",false); 
			break; 
		case 7 : 
			builder.addField("/simps", "Retruns a random character the player simps for.",false); 

			break;
		case 8: 
			builder.addField("/smashpass","Returns a random character the player can decided to smash or pass for.",false); 
			builder.addField("/smashpass [character]","Returns a random character the player can decided to smash or pass for. Optional "
					+ "character can be selected with argument [character]",false); 
			break; 
		case 9 : 
			builder.addField("/sona", "Returns your sona",false); 
			builder.addField("/sona [user]", "Returns a sona of another player. Optional arguemnt [user]",false); 
			builder.addField("/insert-sona <name> <url> <kdm> <smashpass> <simps> <ships> <kins> <waifu> <favorite> <guess> <collect>",
					"Inserts a sona into each game. Use direct url link containing image of your sona must end with .jpg, .png or .gif. Gifs aren't recommended as they"
					+ " may not load inside small embeds.Recommended sites for url link https://imgur.com and https://postimages.org.  Limit of 10 Ocs!" + "\nTutorial to insert ocs and sona https://www.youtube.com/watch?v=pC9GgoP9ycE",false); 
			builder.addField("/remove-sona", "Remove your sona from the server.", false); 
			builder.addField("/remove-sona [user]", "@Helluva Admin only command to remove another user's sona.",false); 
			break; 
		case 10: 
			builder.addField("/user-info","Return general information",false); 
			builder.addField("/user-info [user]","Return general information of another user",false); 
			break; 
		case 11: 
			builder.addField("/add-favorite <character>","Add a character to your favorites list. Require argument <character>. Limit of 10 favorites. ",false);
			builder.addField("/clear-favorites","Remove all characters from your favorites list.",false); 
			builder.addField("/remove-favorite <character>", "Remove a character from your favorites list. Required "
					+ "arguement <character> must be from your favorites.",false);
			builder.addField("/favorites","Returns list of your favorite characters.",false);
			builder.addField("/favorites [user]", "Returns list of favorite characters from specified optional user. [user] is an optional argument.", false);
			builder.addField("/change-favorites-title <title>", "Change your favorites list title.Required argument <title>", false); 
			builder.addField("/swap-favorite-rank <first-character> <second-character>","Swap order between characters in your favorites list. Required arguments "
					+ "<first-character> and <second-character>",false); 
			break; 
		case 12 : 
			builder.addField("/waifu", "Returns a players waifu for the next 24 hours.", false); 
			builder.addField("/waifu [user]", "Returns another players waifu for the next 24 hours. Optional argument [user]",false); 
			builder.addField("/waifu-trade <user>", "Offer to trade with another user. The user has 30 seconds to react with your message to accept the trade. "
					+ "Required argument <user>. ", false); 
			break; 
		case 13 : 
			builder.addField("/wiki <character>", "Returns general information about a specified character. Requried argument "
					+ "<character>.",false);
			builder.addField("/wikifull <character>", "Returns more information and images about a specified character. Requried argument "
					+ "<character>", false); 
			break ; 
		case 14 : 
			builder = new EmbedBuilder(); 
			builder.addField("/insert-oc <name> <url> <kdm> <smashpass> <simps> <ships> <kins> <waifu> <favorite> <guess> <collect>", "Insert your oc into each game. Use direct url link containing image of your sona must end with .jpg, .png or .gif. Gifs aren't recommended as they " + 
					"may not load inside small embeds. Recommended sites for url link https://imgur.com and https://postimages.org.  Limit of 10 Ocs!" + "\nTutorial to insert ocs and sona https://www.youtube.com/watch?v=pC9GgoP9ycE",false); 
			
			builder.addField("/remove-my-oc <customcharacter>", "Remove your specified oc. Required argument <customcharacter>",false);
			builder.addField("/remove-user-oc <user> <customcharacter>", "@Helluva Admin only command to remove another users oc. Required arguments <user> and <customcharacter>", false); 
			builder.addField("/remove-all-ocs","Remove all your ocs in this server.",false); 
			builder.addField("/remove-all-ocs [user]","@Helluva Admin only command to remove all ocs of another user. Optional argument [user].",false); 
			builder.addField("/my-oc" , "Return a list of all your ocs", false); 
			builder.addField("/my-oc [customcharacter]" , "Return a full image of a specified oc. Optional argument [customcharacter]", false); 
			builder.addField("/search-oc <user> <customcharacter>", "Search a specifed oc of a user. Required arguments <user> <customcharacter>",false);
			builder.addField("/set-default-oc <customcharacter>","Set default image of your oc list. Required argument <customcharacter>",false); 
			break; 
		case 15 : 
			builder = new EmbedBuilder(); 
			builder.addField("Credits","https://twitter.com/Girpsy",false); 
			break; 
		}
		return builder;
	}
}
