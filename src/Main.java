import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
 
public class Main {
	
	public enum Command {
		CHECKIN, CREATE, CHECKOUT, EXIT, LABEL,  NO_COMMAND
	}
	
	//	Runs main program
	//	Accepts user input of a src and target path to execute repo cloning
	public static void main(String[] args) {
		
		try {
			repoMenu();
		} catch (RepoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void addLabel(File manifest, String label) throws IOException, RepoException{
		
		BufferedReader br = new BufferedReader(new FileReader(manifest));
		StringBuilder sb = new StringBuilder();
		int count = 0;
		
		while((br.ready())&&(count < 4)){
			String labelLine = br.readLine().trim();
			
			if (labelLine.isEmpty()){
				sb.append(label + "\r\n");
				break; //added label
			}else{
				sb.append(labelLine + "\r\n");
			}
			count++;
		}
		
		if (count >= 4){
			br.close();
			throw new RepoException("Exceed label limit of 4!");
		}
		
		while(br.ready()){
			String line = br.readLine().trim();
			sb.append(line + "\r\n");
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(manifest));
		bw.write(sb.toString());
		bw.close();
	}
	
	public static void repoMenu() throws RepoException, IOException
	{
		@SuppressWarnings("resource")
		Scanner kb = new Scanner(System.in);
		boolean finished = false;
		
		while(!finished)
		{
			System.out.println("\nRepository Commands\n");
			System.out.println("1. create-repo [source folder] [target folder]");
			System.out.println("2. check-in [source folder] [target folder]");
			System.out.println("3. check-out [source folder] [target folder]");
			System.out.println("4. label-manifest [manifest file] [label]");
			System.out.println("5. exit-menu");
			System.out.println("Enter the command and arguments you would like to perform:");
			String command = kb.next();
			System.out.println("Command: " + command);
			
			Command cmd = getCommand(command);
			
			
			if(cmd == Command.EXIT)
			{
				System.out.println("Exiting menu.");
				finished = true;
			}
			else if(cmd == Command.LABEL)
			{
				String manifestDir = kb.next();
				String label = kb.next();
				kb.nextLine();
				//	TODO	: create manifest label function
				File manifest = new File(manifestDir);
				addLabel(manifest, label);
			}
			else 
			{
				System.out.print(">");
				String src = kb.next();
				System.out.print("\n>");
				String target = kb.next();
				kb.nextLine();
				
				if(cmd == Command.CREATE)
				{
					Repository rep = new Create(src, target);
					rep.execute();
					System.out.println("repo created");
					
					//Generate Manifest File
					String manifestDir = target + File.separator + "create.mani";
					File manifest = new File(manifestDir);
					rep.generateManifest(manifest);
					
					System.out.println("Enter label: ");
					String label = kb.nextLine();
					addLabel(manifest, label);
				}
				else if (cmd == Command.CHECKIN)
				{
					CheckIn checkin = new CheckIn(src, target);
					checkin.execute();
					
					File dir = new File(target);
					
					if (dir.isDirectory()){
						String manifestDir = target + File.separator + createManifestName(cmd, dir);
						File manifest = new File(manifestDir);
						checkin.generateManifest(manifest);
					}
				}
				else if (cmd == Command.CHECKOUT)
				{
					File repo = new File(src);
					
					File[] maniFileList = repo.listFiles(new FileFilter() {
						@Override
						public boolean accept(File name) {
							return name.getName().endsWith(".mani");
						}
					});
					
					System.out.println("List of manifest files");
					for(File f: maniFileList)
					{
						System.out.println(f.getName());
					}
					
					//	Get manifest file/label from user
					System.out.println("Enter a manifest file (or label) to check-out from: ");
					String input = kb.nextLine();
					
					//	Determine if input is mainfest file or label
					//	and retrieve manifest file if label
					if(!input.isEmpty())
					{
						String[] maniOrLabel = input.split("\\.");
						for(String s: maniOrLabel)
							System.out.println("Token: " + s);
						
						if(maniOrLabel.length > 1 && input.split("\\.")[1].equals("mani"))
						{
							boolean maniExists = false;
							for(File f: maniFileList)
							{
								if(f.getName().equals(input))
									maniExists = true;
							}
							
							if(maniExists)
							{
								File maniFile = new File(src + File.separator + input);
								CheckOut checkout = new CheckOut(src, target, maniFile);
								checkout.execute();
							}
							else
							{
								System.out.println("Manifest file does not exist.");
							}
						}
						//	Find manifest file that corresponds with the label given
						else
						{
							String maniName = getManifest(input, maniFileList);
							if(maniName == null)
								System.out.println("Label not found.");
							else
							{
								System.out.println(src + File.separator + maniName);
								File manifest = new File(src + File.separator + maniName);
								CheckOut checkout = new CheckOut(src, target, manifest);
								checkout.execute();
							}
						}
					}
					else
					{
						System.out.println("No manifest specified.\n"
								+ "You must provide a manifest file or label to check out from.");
					}
				}
				else if (cmd == Command.NO_COMMAND) {
					System.out.println("Invalid command");
				}
			}
	
		}
	}
	
	public static Command getCommand(String command){
		if (command.equals("exit-menu"))
			return Command.EXIT;
		else if (command.equals("label-manifest"))
			return Command.LABEL;
		else if (command.equals("create-repo"))
			return Command.CREATE;
		else if (command.equals("check-in"))
			return Command.CHECKIN;
		else if (command.equals("check-out"))
			return Command.CHECKOUT;
		else
			return Command.NO_COMMAND;
	}
	
	public static String createManifestName(Command cmd, File dir){
		String result;
		File[] fileList = null;
		
		switch (cmd){
			case CHECKIN:
				fileList = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return (name.contains(".mani")&&equals(name.contains("create")));
					}
				}); 
				result = "checkin" + (fileList.length + 1) + ".mani";
				break;
			case CHECKOUT:
				fileList = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return (name.contains(".mani")&&equals(name.contains("create")));
					}
				}); 
				result = "checkout" + (fileList.length + 1) + ".mani";
				break;
		default:
			result = "";
			break;
		}
		
		return result;
	}

	
	public static String getManifest(String label, File[] maniList)
	{
		for(File mani: maniList)
		{
			Scanner read;
			try {
				read = new Scanner(mani);
				int lineCount = 0;
				while(read.hasNextLine() && lineCount < 4)
				{
					if(read.nextLine().equals(label))
					{
						return mani.getName();
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}
}