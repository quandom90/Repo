
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class CheckIn extends Repository{
	
	private HashMap<String, String> upToDateFiles; 
	
	public CheckIn(String src, String target) {
		super(src, target);
		upToDateFiles = new HashMap<String, String>();
	}
	
	
	@Override
	public void execute() throws RepoException, IOException{
		if ((target != "")&&(src != "")){
			
			checkRepo(src, target);
			
			File mani = new File(target + File.separator + getMostCurrentManiName(Command.CHECKIN, target));
			generateManifest(mani);
		}
	}

	@Override
	public void generateManifest(File manifest) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(manifest, true));
		bw.newLine(); //First four line is for labels
		bw.newLine();
		bw.newLine();
		bw.newLine();
		bw.write("Command: check-in\r\n");
		bw.write("Time of check-in: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME) + "\r\n");
		bw.write("User command: check-in " + src + " " + target +"\r\n");
		bw.write("Source path: " + src + "\r\n");
		bw.write("Target path: " + target + "\r\n");
		
		File targetFile = new File(target);
		findFiles(targetFile, manifest.getAbsolutePath(), bw);
		
		bw.close();
		listManifest.add(manifest);
		
	}
	
//	Recursively travel through the directory
	//	to write info for each file copied into the manifest file.
	private void findFiles(File dir, String manifestDir, BufferedWriter bw) throws IOException{
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
					for (String uAID: upToDateFiles.values()) {
						if (f.getName().equals(uAID)){
							String aid = f.getName();
							String parent = f.getParentFile().getName();
							String path = f.getPath();
							String line = "File Copied Info: " + aid + " " + parent + " " + path + "\r\n";
							bw.write(line);
						}
						
					}
				}
				
			}
	}
	
	private void createNewFile(File srcFile, String tarDir) throws IOException{

		File newFile = new File(tarDir);
		InputStream in = new FileInputStream(srcFile);
		OutputStream out = new FileOutputStream(newFile);
		byte[] buffer = new byte[1024];
		int length = in.read(buffer);
		
		while (length > 0){
			out.write(buffer, 0, length);
			length = in.read(buffer);
		}
		
		in.close();
		out.close();
	}
	
	private void checkRepo(String srcCurrent, String targetCurrent) throws IOException{
		File dir = new File(srcCurrent);
		File tar = new File(targetCurrent);
		
		File[] listFile = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return !name.equals(".DS_Store");
			}
		});
		
		File[] tarList  = tar.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return !name.equals(".DS_Store");
			}
		});
		
		for (File fsrc : listFile){
			if (fsrc.isDirectory()){
				 String targetPath = "";
				 for (File ftar : tarList ){
					 if (ftar.getName().equals(fsrc.getName())){
						 targetPath = ftar.getAbsolutePath();
						 break;
					 }
				 }
				 
				 if (targetPath != ""){
					 //System.out.println("FSRC: "+fsrc.getAbsolutePath()+" , targetPath: "+targetPath);
					 checkRepo(fsrc.getAbsolutePath(), targetPath);
				 }else{
					 targetPath = targetCurrent + File.separator + fsrc.getName();
					 File newTarget = new File(targetPath);
					 newTarget.mkdir();
					 checkRepo(fsrc.getAbsolutePath(), targetPath);
				 }
			} else if (fsrc.isFile()){
				File tarDir = null;
				for (File ftar : tarList){
					if (ftar.getName().equals(fsrc.getName())){
						if (ftar.isDirectory()){
							tarDir = ftar;
						}
					}
				}
				
				boolean sameFile = false;
				String artifact = aid(fsrc);
				upToDateFiles.put(fsrc.getName(), artifact);
				
				if (tarDir != null){
					
					File[] fileVersions = tarDir.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return !name.equals(".DS_Store");
						}
					});
					
					for (File ftar : fileVersions){
						if (ftar.getName().equals(artifact)){
							sameFile = true;
						}
					}
					
				}else{
					String newTarget = targetCurrent + File.separator + fsrc.getName();
					tarDir = new File(newTarget);
					tarDir.mkdir();
				}
				
				if (!sameFile){
					
					createNewFile(fsrc, tarDir + File.separator + artifact);
				}
			}
		}
	}

}
