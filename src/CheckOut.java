import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class CheckOut extends Repository{
	private String fileVersion;
	
	public CheckOut(String src, String target, String fv) {
		super(src, target);
		fileVersion = fv;
	}

	@Override
	public void execute() throws RepoException, IOException {
		File targetDir = new File(target);
		if(targetDir.isDirectory() && targetDir.list().length == 0){ //if target entered is directory and is empty
			if(fileVersion != ""){
				
			} else {
				throw new RepoException("Unable to read manifest");
			}
		} else {
			throw new RepoException("Please select a valid entry point for checkout");
		}
	}

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
		
/*		File srcFile = new File(src);
		File dir = new File(target + File.separator + srcFile.getName());	
		findFiles(dir, manifest.getAbsolutePath(), bw);
		bw.close();
		listManifest.add(manifest);*/
		
	}
	
	//	Recursively travel through the directory
	//	to write info for each file copied into the manifest file.
/*	private void findFiles(File dir, String manifestDir, BufferedWriter bw) throws IOException{
			//Get list of files in directory ignoring the .DS_Store file
			File[] fileList = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return !name.equals(".DS_Store");
				}
			});
			
			for(File f: fileList)
			{
				if(f.isDirectory())
					findFiles(f, manifestDir, bw);
				else if(f.isFile())
				{
					String aid = f.getName();
					String parent = f.getParentFile().getName();
					String path = f.getPath();
					String s = "File Copied Info: " + aid + " " + parent + " " + path + "\r\n";
					bw.write(s);
				}
			}
	}*/
	
	public void readManifest(){
		try {
			Scanner read = new Scanner(new File(fileVersion));
			while(read.hasNext()){ 
				String line = read.nextLine(); //read labels line by line
				if(line.contains("File Copied Info:")){
					//TODO make files here, copy from repo containing the specified files
				}
			}
			read.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
