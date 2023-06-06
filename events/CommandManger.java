package events;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import CharactersPack.CharacterSelection;
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
	private ArrayList<String> names; 
	public CommandManger (Connection conn_Arg)
	{
		conn = conn_Arg;
		CharacterSelection select = new CharacterSelection(conn); 
		ArrayList<String> names = null ; 
		try {
			 names = select.getAllCharacterNames();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		this.names = names; 
		
		System.out.println(names.size()); 
	}
	
	@Override 
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
	{
		String cmd = event.getName(); 
		if(cmd.equalsIgnoreCase("welcome"))
		{
			String userId = event.getUser().getId(); 
			event.reply("Welcome " + "<@" + userId +">" + "!").queue(); 
		}
		if(cmd.equalsIgnoreCase("say"))
		{
			OptionMapping messageOp = event.getOption("message"); 
			String message = messageOp.getAsString(); 
			event.reply("say" + message).queue();
		}
	}
	
	
	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event)
	{
		
		 // Check type of of command 
		 if( ( event.getName().equals("wikifull") || event.getName().equals("wiki") || event.getName().equals("smashpass") || event.getName().equals("kdm")) 
				 && ( event.getFocusedOption().getName().equals("character") ||  event.getFocusedOption().getName().equals("first") || 
						 event.getFocusedOption().getName().equals("second") || event.getFocusedOption().getName().equals("third") ) ) 
		 { 
			 // Search the letter and get 25 possible options including that lettter 
			 
			 ArrayList<String> subList = new ArrayList<String>(); 
			 
			 for(int i =0; i < names.size(); ++i) 
			 {
				 if(names.get(i).toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()) || names.get(i).equalsIgnoreCase(event.getFocusedOption().getValue())) 
				 {
					subList.add(names.get(i)); 
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
		 
		 
		 String[] shownames = {"helluva boss", "hazbin hotel"}; 
		 // Check type of of command 
		if( ( event.getName().equals("kdm")) 
						 && event.getFocusedOption().getName().equals("show")) 
				 {
				
					 List<Command.Choice> options = Stream.of(shownames)
							 .filter(showname -> showname.startsWith(event.getFocusedOption().getValue()))
							 .map(showname-> new Command.Choice(showname, showname))
							 .collect(Collectors.toList()); 
					 event.replyChoices(options).queue();   
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
				OptionData UserOption = new OptionData(OptionType.USER, "user", "Enter a user you want to know the waifu of", false, false);
				OptionData UserOptionTrade = new OptionData(OptionType.USER, "tradee", "Enter a user to trade with ", true, false);  
				OptionData characterOptionOne = new OptionData(OptionType.STRING, "first", "Enter a first character",false,true);
				OptionData characterOptionTwo = new OptionData(OptionType.STRING, "second", "Enter a second character",false,true);
				OptionData characterOptionThree = new OptionData(OptionType.STRING, "third", "Enter a third character",false,true);
				
				
				
				commandList.add(Commands.slash("wikifull", "Display full wiki of the entered character").addOptions(characterOption));
				commandList.add(Commands.slash("wiki", "Display general information on entered character").addOptions(characterOption)); 
				commandList.add(Commands.slash("simps", "Return random character the caller simps for"));
				commandList.add(Commands.slash("smashpass", "Return character the user would smash or pass for").addOptions(characterOptionSMP)); 
				commandList.add(Commands.slash("kins", "Return random character the user kins for")); 
				commandList.add(Commands.slash("waifu", "Return your waifu or return another user's waifu").addOptions(UserOption));
				commandList.add(Commands.slash("waifutrade", "Offer to trade your waifu to another user").addOptions(UserOptionTrade)); 
				commandList.add(Commands.slash("userinfo", "Get general information on specified used").addOptions(UserOption)); 
				commandList.add(Commands.slash("kdm", "Game were you choose 3 characters to kill, date or marry").addOptions(characterOptionOne,characterOptionTwo, characterOptionThree));
				event.getGuild().updateCommands().addCommands(commandList).queue();
				
	}
}
