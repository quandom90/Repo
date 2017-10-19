import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class CheckOut extends Repository{
	private File maniFile;
	private String rootDirectory;
	private ArrayList<String> filesCopied;
	
	public CheckOut(String src, String target, File mf) {
		super(src, target);
		maniFile = mf;
		filesCopied = new ArrayList<String>();
	}

	@Override
	public void execute() throws RepoException, IOException {
		File targetFile = new File(target);
		if(!targetFile.exists()) {
			targetFile.mkdirs();
		}
		if(targetFile.isDirectory() && targetFile.list().length == 0){ //if target entered is directory and is empty
			ArrayList<File> sourceDirectories = readManifest();
			rootDirectory = findRoot(Paths.get(src));
			for(File s: sourceDirectories) {
				//copy files here
				File targetDir = new File(s.toString().replace(rootDirectory,target).trim());
				String targetName = targetDir.getParentFile().getName();
				File targetDest = targetDir.getParentFile().getParentFile();
				copyFile(s,targetDest,targetName);
			}
			int i = 0;
			System.out.println("root directory: ----->"+rootDirectory);
			System.out.println("mani file:" +new File(rootDirectory).getParent() + File.separator +"Checkout"+i+".mani");
			
			while(new File(new File(rootDirectory).getParent() + File.separator +"Checkout"+i+".mani").exists()){
				i++;
			}
			generateManifest(new File(new File(rootDirectory).getParent() + File.separator + "Checkout"+i+".mani"));
		} else {
			throw new RepoException("Please select a valid entry point for checkout");
		}
	}
	
	//return root directory
	public String findRoot(Path directory) {
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(directory)) {
				for (Path child : ds) {
						if (Files.isDirectory(child)) { // make dir
							File fileList[] = directory.toFile().listFiles();
							for(File f : fileList) {
								if(f.toString().contains(".mani")) {
									return child.toString();
								}
							}
							findRoot(child);
						}
					}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("Error walking through directories");
			}
		return null;
		
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
		
		for(String s : filesCopied) {
			bw.write(s);
		}
			
		bw.close();
	}
	
	public ArrayList<File> readManifest(){
		ArrayList<File> FileList = new ArrayList<File>();
		try {
			Scanner read = new Scanner(maniFile);
			while(read.hasNext()){ 
				String line = read.nextLine(); //read labels line by line

				if(line.contains("File Copied Info:")){			
					FileList.add(new File(line.substring(nthIndexOf(line," ",5), line.length()).trim()));
				}
			}
			read.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return FileList;
	}
	

	public void copyFile(File source, File target, String fileName){

		PrintWriter writer = null;
		Scanner read = null;
		try {
			read = new Scanner(source);
			if(!target.exists()){
				target.mkdirs();
			}
			writer = new PrintWriter(target + File.separator + fileName);
			do {
				String line = read.nextLine();
				writer.println(line);
			} while (read.hasNext());

		} catch (FileNotFoundException e) {
			System.out.println("Write Error: File not found");
		} catch (NoSuchElementException e) {
			System.out.println("Warning: file at "+ target.getName() + " is empty");
		} catch (Exception e) {
			System.out.println("Error writing to file: " + e);
		}finally {
			read.close();
			writer.close();
			filesCopied.add("File Copied Info: " + fileName + " " + source.getName() + " " + source + "\r\n");
		}
	}
	
	public  int nthIndexOf(String s, String sub, int n) {
		int index = s.indexOf(sub);
		while (--n > 0 && index != -1)
			index = s.indexOf(sub, index + 1);
		return index;
	}
}

