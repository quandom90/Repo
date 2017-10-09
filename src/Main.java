import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
 
public class Main {
	
	//	Runs main program
	//	Accepts user input of a src and target path to execute repo cloning
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		Scanner kb = new Scanner(System.in);
		System.out.println("Enter source tree path: ");
		String src = kb.nextLine();
		System.out.println("Enter target repo folder path: ");
		String target = kb.nextLine();
		
		Repository rep = new Create(src, target);
		
		try {
			rep.execute();
			System.out.println("repo created");
			
			//Generate Manifest File
			String manifestDir = target + File.separator + "manifest.txt";
			File manifest = new File(manifestDir);
			rep.generateManifest(manifest);
			
			System.out.println("Enter label: ");
			String label = kb.nextLine();
			addLabel(manifest, label);
		} catch (RepoException e) {
		
			e.printStackTrace();
		} catch (IOException e) {
		
			e.printStackTrace();
		}
			
	}
	
	public static void addLabel(File manifest, String label) throws IOException, RepoException{
		
		BufferedReader br = new BufferedReader(new FileReader(manifest));
		StringBuilder sb = new StringBuilder();
		int count = 0;
		
		while((br.ready())&&(count < 4)){
			String labelLine = br.readLine().trim();
			
			if (labelLine.isEmpty()){
				sb.append(label + "\r\n");
				break; //added label
			}else{
				sb.append(labelLine + "\r\n");
			}
			count++;
		}
		
		if (count >= 4){
			br.close();
			throw new RepoException("Exceed label limit of 4!");
		}
		
		while(br.ready()){
			String line = br.readLine().trim();
			sb.append(line + "\r\n");
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(manifest));
		bw.write(sb.toString());
		bw.close();
	}
	

}