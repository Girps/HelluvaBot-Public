package events;

import java.util.List;

import net.dv8tion.jda.api.entities.Role;

public class Helper {
	
	
// Check if user has Helluva Admin Role
public static boolean checkAdminRole(List<Role> roles) 
{
	for(int i =0 ; i < roles.size(); ++i) 
	{
		if (roles.get(i).getName().equalsIgnoreCase("Helluva Admin") ) {return true;}
	}
	
	return false; 
}

// Check if user had OC privillege role
public static boolean checkOcSonaPrivellegeRole(List<Role> roles) 
{
	for(int i =0 ; i < roles.size(); ++i) 
	{
		if (roles.get(i).getName().equalsIgnoreCase("Helluva Permission") ) {return true;}
	}
	
	return false; 
}


}


