package MiscClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import net.dv8tion.jda.api.entities.Message;

import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public class KdmRound {
	private String userId; 
	private Long gameId; // sames as messageId   
	private String kill; 
	private	String date; 
	private String marry; 
	private ArrayList<Long> ids;
	
	public KdmRound(String argUser,Long id) 
	{
		userId = argUser; 
		gameId = id; 
		kill = null; 
		date = null; 
		marry = null; 
		ids = new ArrayList<Long>(); 
		
	}
	
	public void setArrayList (ArrayList<Long> arg)
	{
		ids = arg; 
	}
	
	public ArrayList<Long> getArrayList() 
	{
		return ids; 
	}
	
	public void setState(String btn, String characterName, long idTarget, Message e, ButtonInteraction eBtn) 
	{
		
		if(btn.equalsIgnoreCase("kill")) 
		{
			kill = characterName; 
			if(this.isOver()) {return;}
		}
		else if(btn.equalsIgnoreCase("date")) 
		{
			date = characterName; 
			if(this.isOver()) {return;}
		}
		else if(btn.equalsIgnoreCase("marry")) 
		{
			marry = characterName;
			if(this.isOver()) {return;}
		}
		
		Button disButton = eBtn.getButton().asDisabled(); 
		
		// Now modify the rest besides idTarget 
		for(int i =0; i < ids.size(); ++i) 
		{
			 
			// Modify other buttons to disable them
			if(ids.get(i) != idTarget) 
			{
				
		
				List<Button> list = null;
				
					list = eBtn.getChannel().retrieveMessageById(ids.get(i)).complete().getActionRows().get(0).getButtons(); 
				
				if(disButton.getLabel().equalsIgnoreCase("kill")) 
				{
					eBtn.getChannel().asTextChannel().editMessageById(ids.get(i)," ").setActionRow(list.get(0).asDisabled(), list.get(1), list.get(2)).queue();

				}
				else if(disButton.getLabel().equalsIgnoreCase("date") ) 
				{
					eBtn.getChannel().asTextChannel().editMessageById(ids.get(i)," ").setActionRow(list.get(0), list.get(1).asDisabled(), list.get(2)).queue();
				}
				else  if(disButton.isDisabled())// marry case 
				{
					eBtn.getChannel().asTextChannel().editMessageById(ids.get(i)," ").setActionRow(list.get(0), list.get(1), list.get(2).asDisabled()).queue();
				}
			}
		}
		
	}
	
	public long getGameId() 
	{
		return gameId; 	
	}
	
	
	// Check if the round is over
	public boolean isOver() 
	{
		if(kill != null && marry != null && date != null) 
		{
			return true; 
		}
		
		return false; 
	}
	
	@Override
	public String toString() 
	{
		String result=""; 
		
		result = "<@"+ userId + ">" + " would kill " + kill + " date " + date + " and marry " + marry ; 
		
		return result; 
	}
	
}
