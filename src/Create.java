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

public class Create extends Repository{
	
	public Create(String src, String target) {
		super(src, target);
	}

	/*
	 * Initiates the ptree for the repository by copying the root folder from src and storing it in target
	 */
	@Override
	public void execute() throws RepoException, IOException{
		if ((target != "")&&(src != "")){
			File rootSrc = new File(src);
			System.out.println("source::"+src); //delete me
			String rootRep = target + File.separator + rootSrc.getName();
			System.out.println("Rootrep::"+rootRep); //delete me
			File rep = new File(rootRep);
			
			if (rep.mkdir()){
				duplicate(src, rootRep);
			}else{
				throw new RepoException("Repo already exists!");
			}
		}
	}
	
	/*
	 * Creates the folders and files for the rest of the ptree for the repository. Uses recursion to traverse the ptree
	 * @param directory - the string path of the current leaf of the ptree traversal from the source to be copied from
	 * @param rep - the string path of the current leaf of the ptree traversal from the target to be stored in 
	 */
	private void duplicate(String directory, String rep) throws IOException{
		File dir = new File(directory);
		File[] listFile = dir.listFiles();
		
		for(File f : listFile){
			if (f.isDirectory()){
				String newTarget = rep + File.separator + f.getName();
				File newDir = new File(newTarget);
				newDir.mkdir();
				duplicate(f.getAbsolutePath(), newTarget);
			}else if (f.isFile()){
				String newName = aid(f);
				String newTarget = rep + File.separator + f.getName();
				File newDir = new File(newTarget);
				newDir.mkdir();
				
				File newFile = new File(newTarget + File.separator + newName);
				InputStream in = new FileInputStream(f);
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
		}
	}

	@Override
	public void generateManifest(File manifest) throws IOException {
		// TODO Auto-generated method stub
		BufferedWriter bw = new BufferedWriter(new FileWriter(manifest, true));
		bw.newLine(); //First four line is for labels
		bw.newLine();
		bw.newLine();
		bw.newLine();
		bw.write("Command: create-repo\r\n");
		bw.write("Time repo created: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME) + "\r\n");
		bw.write("User command: create-repo " + src + " " + target +"\r\n");
		bw.write("Source path: " + src + "\r\n");
		bw.write("Target path: " + target + "\r\n");
		
		File srcFile = new File(src);
		File dir = new File(target + File.separator + srcFile.getName());	
		findFiles(dir, manifest.getAbsolutePath(), bw);
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
					String aid = f.getName();
					String parent = f.getParentFile().getName();
					String path = f.getPath();
					String s = "File Copied Info: " + aid + " " + parent + " " + path + "\r\n";
					bw.write(s);
				}
			}
	}

}

