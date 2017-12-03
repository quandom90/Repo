import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class: Create
 * 
 * @author	Quan Nguyen: quanlynguyen90@gmail.com
 * @author	Marvin Mendez: reacxtion@gmail.com
 * @author	Mingtau Li: minijordon@gmail.com
 * 
 * @description: Merges two project trees through their checkin manifests
 */

public class Merge extends Repository
{
	private File manifestRepo;
	private File manifestTarget;

	public Merge(String src, String target, File manifestRepo) {
		super(src, target);
		this.manifestRepo = manifestRepo;
		this.manifestTarget = null;
	}
	
	public Merge(String src, String target, File manifestRepo, File manifestTarget){
		super(src, target);
		this.manifestRepo = manifestRepo;
		this.manifestTarget = manifestTarget;
	}
	

	@Override
	public void execute() throws RepoException, IOException {
		// TODO Auto-generated method stub
		if ((!src.equals(""))&&(!target.equals(""))&&(manifestRepo != null)){
			if (manifestTarget != null){
				mergeWithManifest();
			}else{
				mergeWithPath();
			}
		}else{
			throw new RepoException("Target and/or source path not specified");
		}
	}

	@Override
	public void generateManifest(File manifest) throws IOException {
		
	}
	
	/*
	 * 	Merge functions to handle conflicts depending on input.
	 *	Call if user inputs source and target with manifest files.
	 * 	@param
	 * 		void
	 * 	@return
	 * 		void
	 * 
	 */
	private void mergeWithManifest() throws RepoException, IOException{
		ArrayList<File> srcList = readManifest(manifestRepo);
		ArrayList<File> targetList = readManifest(manifestTarget);
		
		for (File fSrc  : srcList){
			boolean newFile = true;
		
			for (File fTar : targetList){
				String realSrc = fSrc.getParent();
				String realTarget = fTar.getParent();
				String rootSrc = findRootOrProjectName(new File(realSrc), true);
				String rootTarget = findRootOrProjectName(new File(realTarget), true);
				
				String relativeSrc = realSrc.replace(rootSrc, "");
				String relativeTarget = realTarget.replace(rootTarget, "");
				
				if (relativeSrc.equals(relativeTarget)){
	
					if (!fSrc.getName().equals(fTar.getName())){
						//	Find root of the repo project tree to read manifest data
						File repoRoot = new File(findRoot(Paths.get(src)));
						
						//	Merge conflicting files into the target tree
						//	and have the user handle the conflicts
						mergeConflicts(fSrc, fTar, repoRoot, null);
					}
					
					newFile = false;
					break;
				}
			}
			
			
			
			//If there is a new file in the repo R not yet in the project tree T 
			//based from the manifest files then this adds the file to T
			if (newFile){
				rootDirectory = findRoot(Paths.get(src));
				String pathFromRoot = fSrc.getPath().replace(rootDirectory, "").substring(1);
			
				ArrayList<String> foldersTar = new ArrayList<String>(Arrays.asList(target.split("\\\\")));
				ArrayList<String> foldersSrc = new ArrayList<String>(Arrays.asList(pathFromRoot.split("\\\\")));
				
				StringBuilder sb = new StringBuilder();
				
				//If the last folder of the target is the root folder of the repo then
				//remove to avoid conflict during string path concatenation 
				if (foldersTar.get(foldersTar.size() - 1).equals(foldersSrc.get(0))){
					foldersSrc.remove(0);
					
					for (String s : foldersTar){
						sb.append(s + File.separator);
					}
					
					for (String s : foldersSrc){
						sb.append(s + File.separator);
					}
				}else{
					sb.append(target + File.separator +  pathFromRoot + File.separator);
				}
				
				//Copy file from repository R to project tree T
				File targetDir = new File(sb.toString().substring(0, sb.length() - 1).trim());
				String targetName = targetDir.getParentFile().getName();
				File targetDest = targetDir.getParentFile().getParentFile();
				copyFile(fSrc, targetDest, targetName);
				
			}
			
			
		}
	}
	
	/*
	 * 
	 * 	Call if user inputs manifest for the source and a path for the target
	 * 	@param
	 * 		void
	 * 	@return
	 * 		void
	 * 
	 */
	private void mergeWithPath() throws RepoException, IOException{
		ArrayList<File> repoMani = readManifest(manifestRepo);
		
		for (File fSrc : repoMani){
			String projectFile = findRootOrProjectName(fSrc, false);
			File project = new File(projectFile);
			File real = fSrc.getParentFile();
			
			String tarProjName = target + File.separator + project.getName();
			if (!target.contains(project.getName())){
				tarProjName = target + File.separator + project.getName();
			}
			
			String targetPath = real.getAbsolutePath().replace(project.getAbsolutePath(), tarProjName);
			
			File fileToCheck = new File(targetPath);
			
			if (fileToCheck.exists()){
				if (fileToCheck.isFile()){
					String fileAID = aid(fileToCheck);
					
					if (!fSrc.getName().equals(fileAID)){
						//	Handle conflicts
						File repoRoot = new File(findRoot(Paths.get(src)));
						//	Assume latest check-in is the target project tree's check-in and retrieve it
						String nextCIManiNum = getMostCurrentManiName(Command.CHECKIN, repoRoot.getAbsolutePath()).replaceAll("\\D+", "");
						int currCIManiNum = Integer.parseInt(nextCIManiNum) - 1;
						String currCIMani = "checkin"+currCIManiNum+".mani";
						
						File[] ciManiFiles = repoRoot.listFiles(new FilenameFilter() {
							public boolean accept(File repoRoot, String name) {
								return name.matches("checkin\\d+.mani");
							}
						});
						
						File targetMani = null;
						for (File ciMani: ciManiFiles)
						{
							if (ciMani.getName().equals(currCIMani))
							{
								targetMani = ciMani;
							}
						}
						
						if (targetMani != null)
						{
							File fTar = findFile(repoRoot, fileAID);
							mergeConflicts(fSrc, fTar, repoRoot, targetMani);
						}
					}
				}
			
			}
			else{
				checkFolderExist(tarProjName, targetPath);
				copyFile(fSrc, fileToCheck.getParentFile(), fileToCheck.getName());
			}
			
		}	
	}
	
	public void checkFolderExist(String current, String file){
		if (file.contains(current)){
			String relative = file.replace(current, "");
			String folder = "";
			for (char c : relative.toCharArray()){
				if (c == '\\'){
					String folderPath = current + File.separator + folder;
					File check = new File(folderPath);
				
					if (!check.exists()){
						check.mkdir();
					}			
					checkFolderExist(folderPath, file);
					break;
				}else{
					folder += c;
				}
			}
		}
	}
	
	public String findRootOrProjectName(File child, boolean findRoot){
		File parent = child.getParentFile();
		String result = "";
		File[] maniFileList = parent.listFiles(new FileFilter() {
			@Override
			public boolean accept(File name) {
				return name.getName().endsWith(".mani");
			}
		});
		
		if (maniFileList.length == 0){
			result = findRootOrProjectName(parent, findRoot);
		}else{
			if (findRoot)
				return parent.getAbsolutePath();
			else
				return child.getAbsolutePath();
		}
		
		return result;
	}
	
	/*
	 * 
	 * 	This function finds the conflicting files
	 * 	and notifies the user which files they are
	 * 	@param fTar
	 * 		target file
	 * 	@param fSrc
	 * 		source file
	 * 	@param repoRoot
	 * 		repository root directory
	 * 	@param maniTar
	 * 		manifest target file
	 * 	@return
	 * 		void
	 * 
	 */
	public void mergeConflicts(File fTar, File fSrc, File repoRoot, File maniTar) throws IOException
	{
		//	fSrc and fTar are the conflicting files
		String tarConParent = fTar.getParentFile().getName();
		String[] tarConSplit = tarConParent.split("\\.");
		
		String srcConParent = fSrc.getParentFile().getName();
		String[] srcConSplit = srcConParent.split("\\.");
		
		//	Find conflicting file in the target project tree
		File tarFile = findFile(new File(target), tarConParent);
		
		//	Get manifest files to link conflicting files to its proper tree versions
		//	And then copy each version into the target project tree
		File[] ciManiFiles = repoRoot.listFiles(new FilenameFilter() {
			public boolean accept(File repoRoot, String name) {
				return name.matches("checkin\\d+.mani");
			}
		});
		
		for (File ciMani: ciManiFiles)
		{
			Scanner scMani = new Scanner(ciMani);
			while (scMani.hasNext())
			{
				String line = scMani.nextLine();

				if (line.contains("File Copied Info:"))
				{	
					String[] fileInfo = line.split("\\s+");
					if (fileInfo[3].equals(fSrc.getName()) && fileInfo[4].equals(srcConParent))
					{
						File sFile = new File(fileInfo[5]);
						String tName = srcConSplit[0]+"_MT."+srcConSplit[1];
						File tFile = new File(tarFile.getParent() + File.separator + tName);
						File tDest = new File(tarFile.getParent());
						copyFile(sFile, tDest, tName);
						System.out.println("Conflicting file " + tName + " at " + tDest.getAbsolutePath());
					}
					if (fileInfo[3].equals(fTar.getName()) && fileInfo[4].equals(tarConParent))
					{
						File sFile = new File(fileInfo[5]);
						String tName = tarConSplit[0]+"_MR."+tarConSplit[1];
						File tFile = new File(tarFile.getParent() + File.separator + tName);
						File tDest = new File(tarFile.getParent());
						copyFile(sFile, tDest, tName);
						System.out.println("Conflicting file " + tName + " at " + tDest.getAbsolutePath());
					}
				}
			}
			scMani.close();
		}
		
		
		if(manifestTarget == null)
		{
			manifestTarget = maniTar;
		}
		//	Get grandfather conflicting file from mani
		//	and copy it into the project tree, too
		File grandFatherMani = getGrandfather(repoRoot, manifestRepo.getName(), manifestTarget.getName());
		
		Scanner sc = new Scanner(grandFatherMani);
		while (sc.hasNext())
		{
			String line = sc.nextLine();

			if (line.contains("File Copied Info:"))
			{	
				String[] fileInfo = line.split("\\s+");
				if (fileInfo[4].equals(srcConParent))
				{
					File sFile = new File(fileInfo[5]);
					String tName = tarConSplit[0]+"_MG."+tarConSplit[1];
					File tFile = new File(tarFile.getParent() + File.separator + tName);
					File tDest = new File(tarFile.getParent());
					copyFile(sFile, tDest, tName);
					Files.deleteIfExists(tarFile.toPath());
					System.out.println("Conflicting file " + tName + " at " + tDest.getAbsolutePath());
				}
			}
		}
		sc.close();
	}

}
