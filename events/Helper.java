package events;

import java.util.List;

import net.dv8tion.jda.api.entities.Role;

public class Helper {
	
	
public static boolean checkRoles(List<Role> roles) 
{
	for(int i =0 ; i < roles.size(); ++i) 
	{
		if (roles.get(i).getName().equalsIgnoreCase("Helluva Admin") ) {return true;}
	}
	
	return false; 
}


}


