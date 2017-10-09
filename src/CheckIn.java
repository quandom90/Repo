import java.io.File;
import java.io.IOException;

public class CheckIn extends Repository{

	public CheckIn(String src, String target) {
		super(src, target);
	}
	
	@Override
	public void execute() throws RepoException, IOException{
		if ((target != "")&&(src != "")){
			File repo = new File(target);
			File[] files = repo.listFiles();
			
			if (files.length != 0){
				
			}else{
				throw new RepoException("Repository does not exist!");
			}
		}
	}

	@Override
	public void generateManifest(File manifest) {
		// TODO Auto-generated method stub
		
	}

}
