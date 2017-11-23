import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.Paths;

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
				
			}
		}else{
			throw new RepoException("Target and/or source path not specified");
		}
	}

	@Override
	public void generateManifest(File manifest) throws IOException {
		
	}
	
	/*
	 * Call if user inputs source with manifest and target with manifest
	 */
	private void mergeWithManifest() throws RepoException{
		ArrayList<File> srcList = readManifest(manifestRepo);
		ArrayList<File> targetList = readManifest(manifestTarget);
		
	
		for (File fSrc  : srcList){
			boolean newFile = true;
		
			for (File fTar : targetList){
				String realSrc = fSrc.getParent();
				String realTarget = fTar.getParent();
				String rootSrc = findRootFromChild(new File(realSrc));
				String rootTarget = findRootFromChild(new File(realTarget));
				
				String relativeSrc = realSrc.replace(rootSrc, "");
				String relativeTarget = realTarget.replace(rootTarget, "");
				
				if (relativeSrc.equals(relativeTarget)){
	
					if (!fSrc.getName().equals(fTar.getName())){
						//Handle Conflict!!!!!!!
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
	 * Call if user input source with manifest and file path of target only
	 */
	private void mergeWithPath(){
		
	}
	
	public String findRootFromChild(File child){
		File parent = child.getParentFile();
		String result = "";
		File[] maniFileList = parent.listFiles(new FileFilter() {
			@Override
			public boolean accept(File name) {
				return name.getName().endsWith(".mani");
			}
		});
		
		if (maniFileList.length == 0){
			result = findRootFromChild(parent);
		}else{
			return parent.getAbsolutePath();
		}
		
		return result;
	}

}
