package MiscClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public class KdmRound {
	private String userId; 
	private Long gameId; // sames as messageId   
	private String kill; 
	private	String date; 
	private String marry; 
	private ArrayList<Long> ids;
	private ArrayList<String> characterNames; 
	private int count; 
	public KdmRound(String argUser,Long id, String nameOne, String nameTwo, String nameThree) 
	{
		userId = argUser; 
		gameId = id; 
		kill = null; 
		date = null; 
		marry = null; 
		ids = new ArrayList<Long>(); 
		characterNames = new ArrayList<String>(); 
		characterNames.add(nameOne);
		characterNames.add(nameTwo);
		characterNames.add(nameThree); 
		
		
	}
	
	public void setArrayList (ArrayList<Long> arg)
	{
		ids = arg; 
	}
	
	public ArrayList<Long> getArrayList() 
	{
		return ids; 
	}
	
	public void setState(String btn, String characterName, long idTarget, ButtonInteraction eBtn) throws InterruptedException, ExecutionException 
	{
		String name = ""; 
		
		// Remove character now remove ids 
			for(int i = 0 ;  i < characterNames.size();  i++) 
			{
				// Compare string remove and stop when over 
				if(characterName.equals(characterNames.get(i))) 
				{
					name = characterName; 
					characterNames.remove(i);
					++count; 
					System.out.println(count); 
					break; 
				}
			} 
		
			
			for(int i = 0 ; i < ids.size(); ++i) 
			{
				if(idTarget == ids.get(i)) 
				{
					ids.remove(i); 
					break; 
				}
			}
		
		// Got the name now initalize 
		switch (btn)
		{
			case "Kill" : 
			{
				
				kill = name; 
				break; 
			}
			case "Date": 
			{
				
				date = name; 
				break; 
			}
			case "Marry":
			{
				marry = name; 
				break; 
			}
		}
		
		

		if (characterNames.size() == 1) 
		{ 
			if(kill == null)
			{
				kill = characterNames.get(0);
			}
			else if(date == null) 
			{
				date = characterNames.get(0); 
			}
			else 
			{
				marry = characterNames.get(0); 
			}
			
			// Now disable last message 
			
			
					
			
					List<Button> list = null;
					
						list = eBtn.getChannel().retrieveMessageById(ids.get(0)).submit().get().getActionRows().get(0).getButtons(); 
					
				
						eBtn.getChannel().asTextChannel().editMessageById(ids.get(0)," ").setActionRow(list.get(0).asDisabled(), list.get(1).asDisabled(), list.get(2).asDisabled()).queue();

			return; 
		} 
		
		
		Button disButton = eBtn.getButton().asDisabled(); 
		
		// Now modify the rest besides idTarget 
		for(int i =0; i < ids.size(); ++i) 
		{
			 
			// Modify other buttons to disable them
			if(ids.get(i) != idTarget) 
			{
				
		
				List<Button> list = null;
				
					list = eBtn.getChannel().retrieveMessageById(ids.get(i)).submit().get().getActionRows().get(0).getButtons(); 
				
				if(disButton.getLabel().equalsIgnoreCase("kill")) 
				{
					eBtn.getChannel().asTextChannel().editMessageById(ids.get(i)," ").setActionRow(list.get(0).asDisabled(), list.get(1), list.get(2)).queue();

				}
				else if(disButton.getLabel().equalsIgnoreCase("date") ) 
				{
					eBtn.getChannel().asTextChannel().editMessageById(ids.get(i)," ").setActionRow(list.get(0), list.get(1).asDisabled(), list.get(2)).queue();
				}
				else  if(disButton.isDisabled())	// marry case 
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
		if(count >= 2) 
		{
			return true; 
		}
		
		return false; 
	}
	
	public String getUser() 
	{
		return userId; 
	}
	
	
	public String getKill() 
	{
		return kill;
	}
	
	public String getDate() 
	{
		return date; 
	}
	
	public String getMarry() 
	{
		return marry; 
	}
	
}
