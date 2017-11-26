import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Class: Main
 * 
 * @author Quan Nguyen: quanlynguyen90@gmail.com
 * @author Marvin Mendez: reacxtion@gmail.com
 * @author Mingtau Li: minijordon@gmail.com
 * 
 * @description: main program
 */
public class Main {

	// Runs main program
	// Accepts user input of a src and target path to execute repo cloning
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

	/**
	 * This function adds a label to select manifest
	 * 
	 * @param manifest
	 *            manifest file to label
	 * @param label
	 *            label name
	 * @return Nothing
	 */
	public static void addLabel(File manifest, String label)
			throws IOException, RepoException {

		BufferedReader br = new BufferedReader(new FileReader(manifest));
		StringBuilder sb = new StringBuilder();
		int count = 0;

		while ((br.ready()) && (count < 4)) {
			String labelLine = br.readLine().trim();

			if (labelLine.isEmpty()) {
				sb.append(label + "\r\n");
				break; // added label
			} else {
				sb.append(labelLine + "\r\n");
			}
			count++;
		}

		if (count >= 4) {
			br.close();
			throw new RepoException("Exceed label limit of 4!");
		}

		while (br.ready()) {
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
	 * 
	 * @return Nothing
	 */
	public static void repoMenu() throws RepoException, IOException {
		@SuppressWarnings("resource")
		Scanner kb = new Scanner(System.in);
		boolean finished = false;

		while (!finished) {
			
			String command = null;
			String[] tokens = null;
			while(true){
			System.out.println("\nRepository Commands\n");
			System.out.println("create-repo [source folder] [target folder]");
			System.out.println("check-in [source folder] [target folder]");
			System.out.println("check-out [source folder] [target folder]");
			System.out.println("label-manifest [manifest file] [label]");
			System.out.println("merge [source folder] [target folder]");
			System.out.println("exit-menu");
			System.out.println("\nEnter the command and arguments you would like to perform:\n");
			System.out.print(">");
				String cmd = kb.nextLine();
				//allows users to type in command with arguments in one single go
				//splits by spaces and quotes and then takes away quotes
				tokens = cmd.split("\"?( |$)(?=(([^\"]*\"){2})*[^\"]*$)\"?");		
				command = tokens[0].trim();
				if(tokens.length > 3){
					System.out.println("Invalid arguments. Consider enclosing directories in quotes");
				}else{
					break;
				}
			}

			if (command.equals("exit-menu")) {
				System.out.println("Exiting menu.\n\nProgram Exiting...\nGoodbye");
				finished = true;
			} else if (command.equals("label-manifest")) {
				String manifestDir = null;
				String label = null;
				if(tokens.length < 3){
					System.out.println("Enter manifest directory + filename:");
					System.out.print(">");
					manifestDir = kb.nextLine();
					System.out.println("Enter name for manifest");
					System.out.print(">");
					label = kb.nextLine();
				}else {
					try{
						manifestDir = tokens[1].trim();
						label = tokens[2].trim();
					}catch(Exception e){
						e.printStackTrace();
					}
				}

				File manifest = new File(manifestDir);
				addLabel(manifest, label);
			}
			else {
				String src = null;
				String target = null;
				if(tokens.length < 3){
					System.out.println("  Enter source:");
					System.out.print(">");
					src = kb.nextLine();
					System.out.println("  Enter target:");
					System.out.print(">");
					target = kb.nextLine();
				}else {
					src = tokens[1].trim();
					target = tokens[2].trim();
				}

				switch (command) {
				case "create-repo":
					Repository rep = new Create(src, target);
					rep.execute();
					System.out.println("repo created");

					// Generate Manifest File
					String manifestDir = target + File.separator + "create.mani";
					File manifest = new File(manifestDir);
					rep.generateManifest(manifest);

					System.out.println("Enter label: ");
					String label = kb.nextLine();
					addLabel(manifest, label);
					break;
				case "check-in":
					CheckIn checkin = new CheckIn(src, target);
					checkin.execute();
					break;
				case "check-out":
					File repo = new File(src);

					File[] maniFileList = repo.listFiles(new FileFilter() {
						@Override
						public boolean accept(File name) {
							return name.getName().endsWith(".mani");
						}
					});

					// Get manifest file/label from user
					System.out.println("Enter a manifest file (or label) to check-out from: ");
					String input = kb.nextLine();

					// Determine if input is mainfest file or label
					// and retrieve manifest file if label
					if (!input.isEmpty()) {
						String[] maniOrLabel = input.split("\\.");

						if (maniOrLabel.length > 1 && input.split("\\.")[1].equals("mani")) {
							boolean maniExists = false;
							for (File f : maniFileList) {
								if (f.getName().equals(input))
									maniExists = true;
							}

							if (maniExists) {
								File maniFile = new File(src + File.separator + input);
								CheckOut checkout = new CheckOut(src, target, maniFile);
								checkout.execute();
							} else {
								System.out.println("Manifest file does not exist.");
							}
						}
						// Find manifest file that corresponds with the label
						// given
						else {
							String maniName = getManifest(input, maniFileList);
							if (maniName == null)
								System.out.println("Label not found.");
							else {
								File mani = new File(src + File.separator + maniName);
								CheckOut checkout = new CheckOut(src, target, mani);
								checkout.execute();
							}
						}
					} else { // else if input is empty
						System.out
								.println("No manifest specified.\n"
										+ "You must provide a manifest file or label to check out from.");
					}
						break;
					case "merge":
						/*
						 * Two ways to call Merge: 
						 * 
						 * 1. By inputing a manifest file for
						 * both repo and project tree, assume the manifest file is the latest check-in from project tree. 
						 * 
						 * 2. By inputing a manifest file for the repo and the file path of the project tree.
						 */

						// Get manifest file/label from user
						System.out.println("Enter the manifest file (or label):");
						String mergeMani1 = kb.nextLine();
						System.out.println("Enter second manifest file (or label):");
						String mergeMani2 = kb.nextLine();
						
						
						// Determine if input is mainfest file or label
						// and retrieve manifest file if label	
						File rMani = getFileFromManiOrLabel(src,target, mergeMani1);
						File tMani = getFileFromManiOrLabel(src,target, mergeMani2);
						// Call merge with option 1
						if(rMani != null && tMani != null){
							try{
							Merge merge1 = new Merge(src,target, rMani, tMani);
							merge1.execute();
							}catch(Exception e) {
								e.printStackTrace();
							}
						}

// Call merge with option 2
//Option 2 commented out for now. Uncomment to use
//						Merge merge2 = new Merge(src,target, rMani);
//						merge2.execute();
						break;
				default:// default case
					System.out.println("Invalid command");
				}

			}
		}// end else if not exit
	}

	public static String getManifest(String label, File[] maniList) {
		for (File mani : maniList) {
			Scanner read;
			try {
				read = new Scanner(mani);
				int lineCount = 0;

				while (read.hasNextLine() && lineCount < 4) {
					if (read.nextLine().equals(label)) {
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

	public static File getFileFromManiOrLabel(String src, String target, String input){
		File mergeRepo = new File(src);
	
		File[] maniMergeList = mergeRepo.listFiles(new FileFilter() {
			@Override
			public boolean accept(File name) {
				return name.getName().endsWith(".mani");
			}
		});
		
		if (!input.isEmpty()) {
			String[] maniOrLabel1 = input.split("\\.");
	
			if (maniOrLabel1.length > 1 && input.split("\\.")[1].equals("mani")) {
				boolean maniExists = false;
				for (File f : maniMergeList) {
					if (f.getName().equals(input))
						maniExists = true;
				}
	
				if (maniExists) {
					File mani = new File(src + File.separator + input);
						return mani;
				} else {
					System.out.println("Manifest file does not exist.");
				}
			}
			// Find manifest file that corresponds with the label
			// given
			else {
				String maniName = getManifest(input, maniMergeList);
				if (maniName == null)
					System.out.println("Label not found.");
				else {
					File mani = new File(src + File.separator + maniName);
					return mani;
				}
			}
		} else { // else if input is empty
			System.out.println("No manifest specified.\n"
		+ "You must provide a manifest file or label to check out from.");
		}
		return null;
	}
}
