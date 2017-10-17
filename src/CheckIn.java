import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CheckIn extends Repository{

	public CheckIn(String src, String target) {
		super(src, target);
	}
	
	@Override
	public void execute() throws RepoException, IOException{
		if ((target != "")&&(src != "")){
			File repo = new File(target);
			File srcFile = new File(src);
			File[] repoFiles = repo.listFiles();
			
			File rootRepo = null;
			if (repoFiles.length != 0){
				for (File f : repoFiles ){
					if (f.getName().equals(srcFile.getName())){
						rootRepo = f;
						break;
					}
				}
				
				if (rootRepo != null){
					checkRepo(src, rootRepo.getAbsolutePath());
				}else{
					throw new RepoException("Repository does not exist!");
				}
				
			}else{
				throw new RepoException("Repository does not exist!");
			}
		}
	}

	@Override
	public void generateManifest(File manifest) {
		// TODO Auto-generated method stub
		
	}
	
	private void checkRepo(String srcCurrent, String targetCurrent) throws IOException{
		File dir = new File(srcCurrent);
		File tar = new File(targetCurrent);
		
		File[] listFile = dir.listFiles();
		File[] tarList  = tar.listFiles();
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
				
				String artifact = aid(fsrc);
				if (tarDir != null){
					
					File[] fileVersions = tarDir.listFiles();
					
					for (File ftar : fileVersions){
						if (ftar.getName().equals(artifact)){
							return;
						}
					}
				}else{
					String newTarget = targetCurrent + File.separator + fsrc.getName();
					tarDir = new File(newTarget);
					tarDir.mkdir();
				}
				
	
				File newFile = new File(tarDir.getAbsolutePath() + File.separator + artifact);
				InputStream in = new FileInputStream(fsrc);
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

}
