import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


/**
 * Class: CheckOut
 * 
 * @author	Quan Nguyen: quanlynguyen90@gmail.com
 * @author	Marvin Mendez: reacxtion@gmail.com
 * @author	Mingtau Li: minijordon@gmail.com
 * 
 * @description: check in version of file from repository to target folder
 */

public class CheckOut extends Repository{
	private File maniFile;	// manifest file
	
	/** public constructor */
	public CheckOut(String src, String target, File mf) {
		super(src, target);
		maniFile = mf;
	}

	/**
	 * This function executes main checkout functionality
	 * @return Nothing
	 */
	@Override
	public void execute() throws RepoException, IOException {
		File targetFile = new File(target);	// if target doesn't exist, make one
		if(!targetFile.exists()) {
			targetFile.mkdirs();
		}
		if(targetFile.isDirectory() && targetFile.list().length == 0){ // if target entered is directory and is empty
			ArrayList<File> sourceDirectories = readManifest(maniFile);	// grab source directories from manifest
			rootDirectory = findRoot(Paths.get(src));	// get root directory from source
			System.out.println("Root directory: " + rootDirectory);
			
			for(File s: sourceDirectories) {
				//copy files here
				File targetDir = new File(s.toString().replace(rootDirectory,target).trim());
				String targetName = targetDir.getParentFile().getName();
				File targetDest = targetDir.getParentFile().getParentFile();
				copyFile(s,targetDest,targetName);
			}
			
			// generate numbered manifest
			File mani = new File(src + File.separator + getMostCurrentManiName(Command.CHECKOUT, src));
			generateManifest(mani);
		} else {
			throw new RepoException("Please select a valid entry point for checkout (Checkout Folder must be empty)");
		}
		System.out.println("Checkout Successful\n");
	}
	
	
	/**
	 * This function generates a manifest file 
	 * 
	 * @param manifest
	 *            Manifest file to use
	 * @return Nothing
	 */
	@Override
	public void generateManifest(File manifest) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(manifest, true));
		bw.newLine(); //First four line is for labels
		bw.newLine();
		bw.newLine();
		bw.newLine();
		bw.write("Command: check-out\r\n");
		bw.write("Time repo checked out: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME) + "\r\n");
		bw.write("User command: check-out " + src + " " + target +"\r\n");
		bw.write("Source path: " + src + "\r\n");
		bw.write("Target path: " + target + "\r\n");
		
		for(String s : filesCopied) {
			bw.write(s);
		}
			
		bw.close();
	}
	
}

