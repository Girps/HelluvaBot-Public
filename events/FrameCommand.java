package events;

import java.awt.Color;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;


import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameGrabber.Exception;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

public class FrameCommand extends ListenerAdapter
{
	public FrameCommand() 
	{
		
	}
	

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		// Frame command
		if(event.getName().equals("frame")) 
		{
		
			Frame frame = null; 
			BufferedImage bi = null; 
			String files[] = {"HAZBIN HOTEL (PILOT) .mp4", "HELLUVA BOSS - C.H.E.R.U.B (S1)： Episode 4 .mp4", "HELLUVA BOSS - EXES AND OOHS (S2)： Episode 3.mp4"
					, "HELLUVA BOSS - Loo Loo Land (S1)： Episode 2 .mp4","HELLUVA BOSS - Murder Family (S1)： Episode 1.mp4", "HELLUVA BOSS - OZZIE'S (S1)： Episode 7 - FINALE.mp4", 
					"HELLUVA BOSS - QUEEN BEE (S1)： Episode 8.mp4", "HELLUVA BOSS - SEEING STARS (S2)： Episode 2.mp4", "HELLUVA BOSS - Spring Broken (S1)： Episode 3.mp4", 
					"HELLUVA BOSS - THE CIRCUS (S2)： Episode 1.mp4", "HELLUVA BOSS - The Harvest Moon Festival (S1)： Episode 5.mp4", "HELLUVA BOSS - Truth Seekers  (S1)： Episode 6.mp4", 
					"HELLUVA BOSS - WESTERN ENERGY  (S2)： Episode 4.mp4", "HELLUVA BOSS (PILOT).mp4", "HELLUVA BOSS - UNHAPPY CAMPERS (S2)： Episode 5.mp4"}; 
			
			Random gen = new Random(); 
			int index = gen.nextInt(files.length);
		
				
			try 
			{		
				 FFmpegFrameGrabber g = new FFmpegFrameGrabber(files[index]);
				
				 g.start();
				 int length = g.getLengthInFrames(); 
				int rand = gen.nextInt(length); 
				g.stop();
				
				g.start();
				
				g.setFrameNumber(rand);
				Java2DFrameConverter convert = new Java2DFrameConverter(); 
				
					frame = g.grabImage(); 
					bi =  convert.convert(frame); 
					
					
					while(bi == null) 
					{
						rand = gen.nextInt(); 
						frame = g.grabImage(); 
						
						bi =  convert.convert(frame); 
					}
					
				
				g.stop();
			
				ImageIO.write(bi, "png", new File("temp" + event.getId() + ".png")); 
				
			
				 File file = new File("temp" + event.getId() + ".png"); 
				// Have image now build it to embed 
				EmbedBuilder builder = new EmbedBuilder(); 
				builder.setTitle(files[index].replace(".mp4", "") );
				builder.setDescription(" frame " + rand + "/" + length); 
				builder.setImage(  "attachment://"+ file.getName() ); 
				builder.setColor(new Color(255,102,102)); 
				builder.setFooter(event.getMember().getEffectiveName(), event.getMember().getEffectiveAvatarUrl()); 
				FileUpload upload = FileUpload.fromData(file); 
				event.getHook().sendMessageEmbeds(builder.build()).addFiles(upload).queue( e -> {file.delete();});   
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				event.getHook().sendMessage("Something went wrong!").queue(); 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				event.getHook().sendMessage("Something went wrong!").queue(); 
			}
		}
	}
	
}
