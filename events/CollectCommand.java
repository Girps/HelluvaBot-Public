package events;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.awt.Color;


import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.Character;
import CharactersPack.SETUPTYPE;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class CollectCommand extends ListenerAdapter{
	private static Connection conn; 
	private static EventWaiter waiter; 
	
	public CollectCommand(Connection argConn, EventWaiter argWaiter) 
	{
		conn = argConn; 
		waiter = argWaiter; 
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{	
		event.deferReply().queue();
		CharacterSelection select = new CharacterSelection(conn);
		
		// Insert player into the collect game 
		try 
		{
			select.insertUserIntoCollect(event.getUser().getIdLong(), event.getGuild().getIdLong());
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} 
		
		switch (event.getName())
		{
			case "roll": // roll a random character claim with a reaction 
				
			try 
			{
				
				// if player has no turn return 
				if ( select.getPlayerRollsLimit(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
				{
					// Get time till next turn reset and 
					String time = select.getRollRestTime(event.getUser().getIdLong(), event.getUser().getIdLong()); 
					event.getHook().sendMessage( event.getUser().getAsMention() + " you ran out of rolls! Please wait till " + MarkdownUtil.bold(time) + " to roll again!").queue(); 
					return; 
				}
				
				// Decrement players roll
				select.decPlayerRoll(event.getUser().getIdLong(), event.getGuild().getIdLong()); 
				Character temp = select.getRandomCharacters(GAMETYPE.COLLECT, SETUPTYPE.LIGHT, event.getGuild().getIdLong(), 1)[0];
				long charId = temp.getId(); 
				// Get list of users that have this character in their wish list 
				
				
				EmbedBuilder builder = new EmbedBuilder(); 
				builder.setTitle(temp.getName()); 
				builder.setDescription("React with an " + MarkdownUtil.bold("emoji") + " to claim! You have "  + MarkdownUtil.bold("15 seconds") + " to claim this character!");
				builder.setImage(temp.getDefaultImage());
				builder.setFooter(event.getMember().getEffectiveName() + " rolled ", event.getMember().getEffectiveAvatarUrl()); 
				builder.setColor(Color.YELLOW); 
				
				boolean alreadyClaimed = select.hasBeenClaimed(temp.getId(),event.getGuild().getIdLong()); 
				
				// Already claim post embed 
				if(alreadyClaimed) 
				{
					// Get the person who has the character 
					long userId = select.getCollectedCharPlayerId(temp.getId() ,event.getGuild().getIdLong()); 
					builder.setColor(event.getGuild().retrieveMemberById( userId ).submit().get().getColor()); 
					builder.setFooter("Already claimed!", event.getGuild().getIconUrl()); 
					builder.setDescription("Owned by " + "<@" + userId +">" + " !"); 
					 
					
					
					event.getHook().sendMessageEmbeds(builder.build()).queue(); 
					return; 
				}
				
				ArrayList<String> usersName = select.getUsersOfWish(charId,event.getGuild().getIdLong()); 
				String list = ""; 
				
				for(String names : usersName) 
				{
					list += names + " "; 
				}
				
				event.getHook().sendMessageEmbeds(builder.build() )
				.queue( (Emessage) -> 
					{
						this.waiter.waitForEvent(
								MessageReactionAddEvent.class, 
								(eReact) -> 
								
								{ 
									if(eReact.getUser().isBot()) 
									{
										return false; 
									}
							
									boolean collectLimit = false,claimLimit = false; 
									
									try 
									{
										if(eReact.getMessageIdLong() == Emessage.getIdLong()) 
										{
											// Insert the user who reacted into table 
											select.insertUserIntoCollect(eReact.getUser().getIdLong(), event.getGuild().getIdLong()); 
										
											// Check if max has already been collect or has to wait till claim refreshes 
											claimLimit = select.getClaimLimit(eReact.getUser().getIdLong(), event.getGuild().getIdLong()); 
											collectLimit = select.checkCollectLimit(eReact.getUser().getIdLong(), event.getGuild().getIdLong());
											
											
											// If valid claim 
											if (!claimLimit && !collectLimit && !alreadyClaimed) 
											{
												
												return true; 
											} 	// Already claimed pass hour interval 
											else if (claimLimit)	// check if they already claimed the pass hour interval 
											{
												String time = "";
												try 
												{
													time = select.getPlayerCollectTime(eReact.getUserIdLong(), eReact.getGuild().getIdLong());
													event.getHook().sendMessage(eReact.getUser().getAsMention() + " you already claimed! Wait after " + MarkdownUtil.bold(time) + " before you can claim again!" ).queue(); 
												} catch (SQLException e)
												{
													// TODO Auto-generated catch block
													e.printStackTrace();
												} 
												
												return false; 
											}
											else if (collectLimit) 
											{
												event.getHook().sendMessage(eReact.getUser().getAsMention() + " you reached the max number of characters to collect!" ).queue(); 
												return false; 
											}
											
											return false;
											
										}
									} catch (SQLException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									return false; 
									
									
								
								},	// success 
								(eSuccess) -> 	
								{
									// Function to give user the character 
									try 
									{
										EmbedBuilder tempBuilder = new EmbedBuilder(builder); 
										tempBuilder.setFooter("Claimed by " + eSuccess.getMember().getEffectiveName(), eSuccess.getMember().getEffectiveAvatarUrl());
										tempBuilder.setColor(eSuccess.getMember().getColor()); 
										select.claimCharacter(temp.getId(), eSuccess.getUser().getIdLong(), eSuccess.getGuild().getIdLong());
										event.getHook().editMessageEmbedsById(Emessage.getIdLong(), tempBuilder.build()).queue(); 
										event.getHook().sendMessage(eSuccess.getUser().getAsMention() + " has claimed " + MarkdownUtil.bold(temp.getName()) + "!").queue(); 
									}
									catch (SQLException e1)
									{
										// TODO Auto-generated catch block
										e1.printStackTrace();
										event.getHook().sendMessage("something went wrong!").queue(); 
									} 
								} 
								,
									// Waited 15 seconds call this function
									15L,TimeUnit.SECONDS, 
									() -> event.getHook().editMessageEmbedsById(Emessage.getIdLong(), builder.setFooter(
									"Claim has expired!",event.getGuild().getIconUrl()).build()).queue()
								);
					}
				); 
				// Send message notify
				if(!list.isEmpty())
				{ 
					event.getHook().sendMessage(list + " character " + MarkdownUtil.bold(temp.getName()) + " has been rolled!").queue(); 
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
				
			break; 
			case "collection" : // shows collect list of calling user or option of another user 
			try 
			{
				if(event.getOptions().isEmpty())
				{
					
					if(!select.hasCollectList(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " you have not collected a character from the collect game"+ "!").queue(); 
						return; 
					}
					ArrayList<Character> list = select.getCollectionList(event.getUser().getIdLong(), event.getGuild().getIdLong());
					String names = "";
					
					
					
					EmbedBuilder builder = new EmbedBuilder();
					
					for(Character temp : list) 
					{
						names += "- "  + temp.getName() + "\n"; 
					}
					builder.setAuthor(event.getMember().getEffectiveName() + "'s Collection!", event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveAvatarUrl());
					builder.setThumbnail(list.get(0).getDefaultImage()); 
					builder.setColor(Color.YELLOW); 
					builder.setDescription(names); 
					event.getHook().sendMessageEmbeds(builder.build()).queue(); 
				}
				else 
				{
					if(!select.hasCollectList(event.getOption("user").getAsUser().getIdLong(), event.getGuild().getIdLong())) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " user " + event.getOption("user").getAsMember().getEffectiveName() + " has not collected a character from the collect game"+ "!").queue(); 
						return; 
					}
					
					long idtarget = event.getOption("user").getAsUser().getIdLong();  
					ArrayList<Character> list = select.getCollectionList(idtarget, event.getGuild().getIdLong()); 
					String names = ""; 
					EmbedBuilder builder = new EmbedBuilder(); 
						
					for(Character temp : list) 
					{
						names += "- "  + temp.getName() + "\n"; 
					}
					builder.setAuthor(event.getOption("user").getAsMember().getEffectiveName() + "'s Collection!", event.getOption("user").getAsMember().getEffectiveAvatarUrl(),
							event.getOption("user").getAsMember().getEffectiveAvatarUrl());
					builder.setThumbnail(list.get(0).getDefaultImage()); 
					builder.setColor(Color.RED); 
					builder.setDescription(names); 
					event.getHook().sendMessageEmbeds(builder.build()).queue(); 
				}
					
			} 
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				event.getHook().sendMessage("Something went wrong!").queue();
			} 
				
				
				break; 
			case "collect-trade" : // trade character in collection
				
				String traderCharacter = event.getOption("trader-character").getAsString(); 
				long tradee = event.getOption("user").getAsUser().getIdLong(); 
				String tradeeCharacter = event.getOption("tradee-character").getAsString(); 
				
				// Check if tradee is bot 
				if(event.getUser().isBot() || event.getOption("user").getAsUser().isBot()) 
				{
					event.getHook().sendMessage("Can't trade with a bot!").queue(); 
					return; 
				}
				
				event.getHook().sendMessage(event.getUser().getAsMention() +  " wants to trade their collectible " +  MarkdownUtil.bold(traderCharacter) 
				+ " with " + event.getOption("user").getAsUser().getAsMention() + "'s" + " collectible " + MarkdownUtil.bold( tradeeCharacter)  + "! React to this message with an emoji to accept the trade!").queue
				( (eMessage) -> 
					{
						this.waiter.waitForEvent(
								MessageReactionAddEvent.class, 
								(eReact) ->  
									{ 
										// Make sure not a bot and same user and message and conduct the trade 
										if(!eReact.getUser().isBot() && eReact.getUser().getIdLong() == tradee 
												&& eReact.getMessageIdLong() == eMessage.getIdLong() ) 
										{
											// now trade 
											return true; 
										}
										else 
										{
											return false;
										}
									},	// success 
									(eSuccess) -> 	
									{
										long traderCharacterId = 0; 
										long tradeeCharacterId = 0; 
								
										try 
										{
											traderCharacterId = select.getCharacterId(traderCharacter, event.getGuild().getIdLong());
											tradeeCharacterId = select.getCharacterId (tradeeCharacter,event.getGuild().getIdLong()); 
											select.swapUserCollectible(event.getUser().getIdLong(), tradee, traderCharacterId, tradeeCharacterId, eSuccess.getGuild().getIdLong());
										} catch (SQLException e)
										{
											// TODO Auto-generated catch block
											e.printStackTrace();
											event.getHook().sendMessage("Something went wrong!").queue(); 
											return; 
										} 
										event.getHook().sendMessage("Trade successful!").queue(); 
									} 
									,
									// Waited 10 seconds call this function
									30L,TimeUnit.SECONDS, 
									() -> event.getHook().sendMessage("30 seconds passed trade expired!").queue()
								);
					}
				); 
				
				
				break; 
			case "reset-collect" : // Can only be decided by Helluva Admin
				
				if ( Helper.checkRoles(event.getMember().getRoles())) 
				{
					// Reset the game for the server 
					try 
					{
						select.resetCollectGame(event.getGuild().getIdLong());
						event.getHook().sendMessage( "Helluva Admin " + event.getUser().getAsMention() + " has reset the collect game!").queue();
					}
					catch (SQLException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						event.getHook().sendMessage("Something went wrong!").queue(); 
					} 
					
				}
				else 
				{
					event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only Helluva Admins can use that command!").queue();
					return; 
				}
				
				break; 
			case "set-default-collect" :  // sets default image of the collection list 
			{
				String targetCharacter = event.getOption("character").getAsString(); 
				long charId;
				try 
				{
					if(select.getSearchCharIdSelect(select.getCharacterId(targetCharacter, event.getGuild().getIdLong()), event.getUser().getIdLong(), event.getGuild().getIdLong())) 
					{ 
						charId = select.getCharacterId(targetCharacter, event.getGuild().getIdLong());
						select.setDefCollectCharacter(charId,event.getUser().getIdLong(), event.getGuild().getIdLong()); 
						event.getHook().sendMessage(event.getUser().getAsMention() + " collectible "+ MarkdownUtil.bold(targetCharacter) + " has been set as default image!").queue(); 
					}
					else 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " character "  + MarkdownUtil.bold(targetCharacter) + " is not on your collect list!" ).queue(); 
					}
				} catch (SQLException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					event.getHook().sendMessage("Something went wrong!").queue(); 
				} 
				
			}	
				break; 
			case "wish-list": // list of characters players want to claim, or option wish list of another user
				{
					
					
					 
					ArrayList<Character> list;
					try
					{	
						
						
						if(event.getOptions().isEmpty())
						{
							
							if(!select.hasWishList(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
							{
								event.getHook().sendMessage(event.getUser().getAsMention() + " you have not added a character to your wishlist"+ "!").queue(); 
								return; 
							}
							
							
							list = select.getWishList(event.getUser().getIdLong(), event.getGuild().getIdLong());
						
							String names = ""; 
							EmbedBuilder builder = new EmbedBuilder(); 
							
							for(Character temp : list) 
							{
								if(!select.hasBeenClaimed(temp.getId(), event.getGuild().getIdLong())) 
								{  
									names += "- "  + temp.getName() + "\n"; 
								}
								else 
								{
									names += "- " + temp.getName() + " :x:" +"\n"; 
								}
							}
							builder.setAuthor(event.getMember().getEffectiveName() + "'s wishlist!", event.getUser().getEffectiveAvatarUrl(),
									event.getUser().getEffectiveAvatarUrl());
							builder.setThumbnail(list.get(0).getDefaultImage()); 
							builder.setColor(Color.YELLOW); 
							builder.setDescription(names); 
							builder.setFooter("You will be @ when these characters appear on a roll!"); 
							event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						}
						else 
						{
							long idtarget = event.getOption("user").getAsUser().getIdLong(); 
							
							if(!select.hasWishList(idtarget, event.getGuild().getIdLong())) 
							{
								event.getHook().sendMessage( event.getUser().getAsMention() + "user " + event.getOption("user").getAsUser().getAsMention() + " has not added a character to their wishlist"+ "!").queue(); 
								return; 
							}
							list = select.getWishList(idtarget, event.getGuild().getIdLong());
							
							String names = ""; 
							EmbedBuilder builder = new EmbedBuilder(); 
						
							for(Character temp : list) 
							{
								names += "- "  + temp.getName() + "\n"; 
							}
							builder.setAuthor(event.getOption("user").getAsMember().getEffectiveName() + "'s wishlist!", event.getOption("user").getAsMember().getEffectiveAvatarUrl(),
									event.getOption("user").getAsMember().getEffectiveAvatarUrl());
							builder.setThumbnail(list.get(0).getDefaultImage()); 
							builder.setColor(Color.YELLOW); 
							builder.setDescription(names); 
							builder.setFooter("You will be @ when these characters appear on a roll!"); 
							event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						}
						
					} 
					catch (SQLException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						event.getHook().sendMessage("Something went wrong!").queue(); 
					} 
					
				}
				break; 
			case "add-wish": 
				{
						String characterName = event.getOption("character").getAsString(); 
						long charId = 0; 
						try
						{
							if(select.wishListLimit(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
							{ 
								charId = select.getCharacterId(characterName, event.getGuild().getIdLong());
								select.addToWishList(charId, event.getUser().getIdLong(), event.getGuild().getIdLong()); 
								event.getHook().sendMessage(event.getUser().getAsMention() + " character " + MarkdownUtil.bold(characterName) + " added to your wishlist you will be notified when this character appears on a roll!").queue(); 
							}
							else 
							{
								event.getHook().sendMessage(event.getUser().getAsMention() + " you reached the limit of 5 characters in your wishlist!").queue(); 
							}
						} 
						catch (SQLException e)
						{
							event.getHook().sendMessage("Something went wrong!").queue(); 					
							e.printStackTrace();
						} 
				}
				break; 
			case "clear-wishes" :
			{
				try 
				{
					if(select.hasWishList(event.getUser().getIdLong(), event.getGuild().getIdLong()))
					{
						select.clearWishList(event.getUser().getIdLong(), event.getGuild().getIdLong()); 
						event.getHook().sendMessage(event.getUser().getAsMention() + " wishlist cleared!").queue(); 
					}
					else 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " you don't have a wishlist!").queue(); 
					}
				} catch (SQLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					event.getHook().sendMessage("Something went wrong!").queue();
				}
				
			}
			break ; 
			case "remove-wish": 	// remove character from your wish list
			{
				String targetCharacter = event.getOption("character").getAsString(); 
				try 
				{
					if(select.searchWishList(targetCharacter,event.getUser().getIdLong(), event.getGuild().getIdLong()) )
					{
						select.removeWish(targetCharacter,event.getUser().getIdLong(), event.getGuild().getIdLong());
						event.getHook().sendMessage(event.getUser().getAsMention() + " has removed wish " + MarkdownUtil.bold(targetCharacter) + "!").queue();
					}
					else 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " you don't have " +  MarkdownUtil.bold(targetCharacter) + "!").queue();
					}
				
				} 
				catch (SQLException e)
				{
				// TODO Auto-generated catch block
					e.printStackTrace();
					event.getHook().sendMessage("Something went wrong!").queue(); 
				} 
			}
				break; 
			case "release": // remove character from collection own collection
			{ 
				String targetCharacter = event.getOption("character").getAsString(); 
				try 
				{
					if(select.searchCharacterCollectList(targetCharacter,event.getUser().getIdLong(), event.getGuild().getIdLong()) )
					{
						select.removeCollectCharacter(targetCharacter,event.getUser().getIdLong(), event.getGuild().getIdLong());
						event.getHook().sendMessage(event.getUser().getAsMention() + " has released " +  MarkdownUtil.bold(targetCharacter) + " back to the collect game!").queue();
					}
					else 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " you don't have " +  MarkdownUtil.bold(targetCharacter) + "!").queue();
					}
				
				} 
				catch (SQLException e)
				{
				// TODO Auto-generated catch block
					e.printStackTrace();
					event.getHook().sendMessage("Something went wrong!").queue(); 
				} 
			}
				break; 
			case "force-release" : 
			{
					long targetUser = event.getOption("user").getAsUser().getIdLong(); 
					
					String targetCharacter = event.getOption("character").getAsString(); 
				try 
				{
					
					if ( !Helper.checkRoles(event.getMember().getRoles())) 
					{
						event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only Helluva Admins can use that command!").queue();
						return; 
					}
					
					if(select.searchCharacterCollectList(targetCharacter,targetUser, event.getGuild().getIdLong()) )
					{
						select.removeCollectCharacter(targetCharacter,event.getOption("user").getAsUser().getIdLong(), event.getGuild().getIdLong());
						event.getHook().sendMessage(event.getUser().getAsMention() + " has released " +  event.getOption("user").getAsUser().getAsMention() + "'s "
						+ targetCharacter + " back to the collect game!").queue();
					}
					else 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " user " + event.getOption("user").getAsMentionable() + 
								" does not have character "+ targetCharacter + "!").queue();
					}
				
				}
				catch (SQLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					event.getHook().sendMessage("Something went wrong!").queue(); 
				} 
			}	
				break; 
		}
	}
	
}
