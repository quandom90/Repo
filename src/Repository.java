import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

public abstract class Repository {
	
	public enum Command {
		CHECKIN, CREATE, CHECKOUT, EXIT, LABEL,  NO_COMMAND
	}
	
	protected String src;
	protected String target;
	protected ArrayList<File> listManifest;
	/*
	 * Constructor
	 * @param src - string path of project to be copied
	 * @param target - string path of target directory to store copied files from src 
	 */
	public Repository(String src, String target){
		this.src = src;
		this.target = target;
		File root = new File(src);
		File[] possibleManifest = root.listFiles();
		
		listManifest = new ArrayList<File>();
		for(File m : possibleManifest){
			if (m.getName().contains("manifest")){
				listManifest.add(m);
			}
		}
	}
	
	//Getter
	public String getSrc(){
		return src;
	}
	
	public String getTarget(){
		return target;
	}
	
	public abstract void execute() throws RepoException, IOException;
	public abstract void generateManifest(File manifest) throws IOException;
	
	/*
	 * Generates name for most current numbered manifest file
	 * @param cmd - the command for the repo either check-in or check-out
	 * @param dirName - the directory string of the repo
	 * @return - the name of the most current numbered manifest file 
	 */
	public String getMostCurrentManiName(Command cmd, String dirName){
		String result;
		File[] fileList = null;
		File dir = new File(dirName);
		
		switch (cmd){
			case CHECKIN:
				fileList = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return ((name.contains(".mani"))&&(name.contains("checkin")));
						
					}
				}); 
				result = "checkin" + (fileList.length + 1) + ".mani";
				break;
			case CHECKOUT:
				fileList = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return ((name.endsWith(".mani"))&&(name.contains("checkout")));
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
	
	/*
	 * Generates Artifact Code names for files with checksum
	 * @param file - the file which a code name will be generated from
	 * @return - the code string created by taking the checksum of the file
	 */
	public String aid(File file){
		try {
			FileInputStream fileRead = new FileInputStream(file);
			
			int c = fileRead.read();
			int[] weight = {1, 3, 7, 11, 17};
			int weightIndex = 0;
			int checkSum = 0;
			
			while(c != -1){
				 if ((char) c != '\n'){
					 checkSum += (weight[weightIndex] * c);
					 
					 if (weightIndex >= 4)
							weightIndex = 0;
						else
							weightIndex++;
				 }
				 
				 c = fileRead.read();
			}
			
			fileRead.close();
			String[] fileName = file.getName().split("\\.");
			String ext = fileName[fileName.length - 1];
			return (checkSum + "." + file.length() + "." + ext);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	
		
	}
	
	
}
