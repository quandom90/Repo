import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
/**
 * Class: Main
 * 
 * @author	Quan Nguyen: quanlynguyen90@gmail.com
 * @author	Marvin Mendez: reacxtion@gmail.com
 * @author	Mingtau Li: minijordon@gmail.com
 * 
 * @description: main program
 */
public class Main {
	
	//	Runs main program
	//	Accepts user input of a src and target path to execute repo cloning
	public static void main(String[] args) {
		
		repoMenu();
		
	}
	
	/**
	 * This function adds a label to select manifest
	 * 
	 * @param manifest
	 *            manifest file to label
	 * @param label
	 *            label name
	 * @return Nothing
	 */
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
	
	/**
	 * This function asks user for input and executes commands accordingly
	 * @return Nothing
	 */
	public static void repoMenu()
	{
		@SuppressWarnings("resource")
		Scanner kb = new Scanner(System.in);
		boolean finished = false;
		
		while(!finished)
		{
			System.out.println("Repository Commands\n");
			System.out.println("1. create-repo [source folder] [target folder]");
			System.out.println("2. check-in [source folder] [target folder]");
			System.out.println("3. check-out [source folder] [target folder]");
			System.out.println("4. label-manifest [manifest file] [label]");
			System.out.println("5. exit-menu");
			System.out.println("Enter the command and arguments you would like to perform:");
			String command = kb.next();
			System.out.println("Command: " + command);
			
			if(command.equals("exit-menu"))
			{
				System.out.println("Exiting menu.");
				finished = true;
			}
			else if(command.equals("label-manifest"))
			{
				String manifestDir = kb.next();
				String label = kb.next();
				kb.nextLine();
				//	TODO	: create manifest label function
				File manifest = new File(manifestDir);
				try {
					addLabel(manifest, label);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RepoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				System.out.print(">");
				String src = kb.next();
				System.out.print("\n>");
				String target = kb.next();
				kb.nextLine();
				
				if(command.equals("create-repo"))
				{
					Repository rep = new Create(src, target);
					try {
						rep.execute();
						System.out.println("repo created");
						
						//Generate Manifest File
						String manifestDir = target + File.separator + "manifest.mani";
						File manifest = new File(manifestDir);
						rep.generateManifest(manifest);
						
						System.out.println("Enter label: ");
						String label = kb.nextLine();
						addLabel(manifest, label);
					} catch (RepoException e) {
					
						e.printStackTrace();
					} catch (IOException e) {
					
						e.printStackTrace();
					}
				}
				else if (command.equals("check-in"))
				{
					CheckIn checkin = new CheckIn(src, target);
				}
				else if (command.equals("check-out"))
				{
					//	TODO: get manifest file from cache
					//	label mapped to manifest file
					//	Dummy code
//					HashMap<String, String> cache = new HashMap<String, String>();
//					cache.put("label1", "manifest.mani");
//					File maniFile = new File(cache.get("label1"));
					
//					CheckOut checkout = new CheckOut(src, target, maniFile);
					CheckOut checkout = new CheckOut(src, target, new File("C:\\trashme2\\repo_folder\\manifest.mani"));
					
					try {
						checkout.execute();
					} catch (RepoException | IOException e) {
						e.printStackTrace();
					}
				}
				else {
					System.out.println("Invalid command");
				}
			}
	
		}
	}
}