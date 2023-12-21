package events;

import java.awt.Color;
import java.time.OffsetDateTime;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Date;
import java.util.List;
public class UserInfoCommand extends ListenerAdapter
{
	
	
	
	public UserInfoCommand() 
	{
		
	}
	
	// Override method on GuildMessageRecieved 
		@Override
		public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
		{
			
				// Just single string 
			if(event.getName().equals("user-info") && event.getOption("user") == null) 
			{
				Member user = event.getMember(); 
				 user.getUser().retrieveProfile().queue( (e) -> 
				{
					
					String bannerId = e.getBannerUrl();
					String nameId = user.getId(); 
					String nameRaw = user.getEffectiveName(); 
					Color col = user.getColor(); 
					OffsetDateTime time = event.getTimeCreated(); 
					Date date = Date.from(time.toInstant());
					String avatarURL = user.getEffectiveAvatarUrl(); 
					
					List<Activity> acts = user.getActivities();
					String actStr = ""; 
					
					// Get activities string
					if(  !( acts.isEmpty() ) )
					{
					// iterate string 
						for(int i = 0; i < acts.size(); ++i) 
						{
							actStr += acts.get(i).getName() + "\n"; 
						}
					}
					else 
					{
						actStr = "None"; 
					}
					
					// End of activities list
					
					// Roles strings of a user 
					List <Role> rols = user.getRoles(); 
					
					String strRoles= ""; 
					
					if(  !( rols.isEmpty() ) )
					{
					// iterate string 
						for(int i = 0; i < rols.size(); ++i) 
						{
							strRoles +=  rols.get(i).getAsMention() + "\n"; 
						}
					}
					else 
					{
						strRoles = "None"; 
					}
					
					// End of roles section
					
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setTitle(nameRaw + " Info"); 
					builder.setImage(bannerId); 
					builder.setDescription("<" + "@" + nameId +">");
					builder.addField("Roles", strRoles, false);  
					builder.addField("Status", user.getOnlineStatus().toString(), false); 
					builder.addField("Activities", actStr, false); 
					builder.setColor(col);	// set color as the users row color 
					builder.setThumbnail(avatarURL); 
					builder.setFooter("Time : " + date.toString(), event.getGuild().getIconUrl()); 	// will get guild icon
					MessageEmbed msgEmbed = builder.build();
					event.deferReply().queue();
					event.getHook().sendMessageEmbeds(msgEmbed).queue();
				} 
				
				); 
				
				
			}
			
			// Mulitple string 
			if( event.getName().equals("user-info") && event.getOption("user") != null)
			{
				// Now check if that the user is part of the discord guild
				Member user = event.getOption("user").getAsMember();   

				user.getUser().retrieveProfile().queue((e) ->
					{
						String bannerId = e.getBannerUrl();
						String nameId = user.getId().toString(); 
						String nameRaw = user.getEffectiveName(); 
						Color col = user.getColor();
						OffsetDateTime time = event.getTimeCreated(); 
						Date date = Date.from(time.toInstant());
						String avatarURL = event.getOption("user").getAsUser().getAvatarUrl(); 
						
						
						
						// Roles strings of a user 
						List <Role> rols = user.getRoles(); 
						
						String strRoles= ""; 
						
						if(  !( rols.isEmpty() ) )
						{
						// iterate string 
							for(int i = 0; i < rols.size(); ++i) 
							{
								strRoles += rols.get(i).getAsMention() + "\n"; 
							}
						}
						else 
						{
							strRoles = "None"; 
						}
						
						// End of roles list 
						
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setTitle(nameRaw + " Info"); 
						builder.setImage(bannerId); 
						builder.setDescription("<" + "@" + nameId +">");
						builder.addField("Roles", strRoles, false);  
						builder.addField("Status", user.getOnlineStatus().toString(), false); 
						builder.addField("Activities", "None", false); 
						builder.setColor(col);	// set color as the users row color 
						builder.setThumbnail(avatarURL); 
						builder.setFooter("Time : " + date.toString(), event.getGuild().getIconUrl()); 	// will get guild icon
						
						MessageEmbed msgEmbed = builder.build();
						event.deferReply().queue(); 
						event.getHook().sendMessageEmbeds(msgEmbed).queue();
					}
				);  
				
			}
			
		}
}
