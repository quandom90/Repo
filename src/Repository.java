
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
	
	public File getGrandfather(File repo, String rMani, String tMani) throws IOException
	{
		String grandfather = "Grandfather not found.";
		
		ArrayList<Node> nodes = getManifestData(repo);
		Node rNode = null;
		Node tNode = null;
		for (Node node: nodes)
		{
			if (node.data.getName().equals(rMani))
			{
				rNode = node;
			}
			if (node.data.getName().equals(tMani))
			{
				tNode = node;
			}
		}
		
		ArrayList<String> rToRoot = findPathToRoot(rNode, nodes);
		ArrayList<String> tToRoot = findPathToRoot(tNode, nodes);
		
		ArrayList<String> intersection = new ArrayList<String>(rToRoot);
		intersection.retainAll(tToRoot);
		
		grandfather = intersection.get(0);
		
		File grandMani = null;
		for (Node node: nodes)
		{
			if(node.name.equals(grandfather))
			{
				grandMani = node.data;
			}
		}
		
		return grandMani;
	}
	
	public class Node
	{
		String name;
		File data;
		String parent;
		ArrayList<Node> children = new ArrayList<Node>();
		
		public Node(String name, File data, String parent)
		{
			this.name = name;
			this.data = data;
			this.parent = parent;
		}
		
		public void addChild(Node child)
		{
			children.add(child);
		}
	}
	
	public ArrayList<String> findPathToRoot(Node startNode, ArrayList<Node> nodes)
	{
		ArrayList<String> nodeToRoot = new ArrayList<String>();
		nodeToRoot.add(startNode.name);
		Node current = startNode;
		while(!current.name.equals("root"))
		{
			String parentName = current.parent;
			nodeToRoot.add(parentName);
			for (Node node: nodes)
			{
				if (node.name.equals(parentName))
				{
					current = node;
				}
			}
		}
		
		return nodeToRoot;
	}
	
	public ArrayList<Node> getManifestData(File repo) throws IOException
	{
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		File createMani = new File(repo.getAbsolutePath()+File.separator+"create.mani");
		Node root = new Node("root", createMani, null);
		nodes.add(root);
		
		File[] coManiFiles = repo.listFiles(new FilenameFilter() {
			public boolean accept(File repo, String name) {
				return name.matches("checkout\\d+.mani");
			}
		});
		
		File[] ciManiFiles = repo.listFiles(new FilenameFilter() {
			public boolean accept(File repo, String name) {
				return name.matches("checkin\\d+.mani");
			}
		});
		
		String parent = "";
		String child = "";
		
		for (File coManiFile: coManiFiles)
		{
			BufferedReader brco = new BufferedReader(new FileReader(coManiFile));
			
			//	Skip first eight lines to get to child
			for (int i=0; i<8; i++) 
			{ 
				brco.readLine(); 
			}
			child = brco.readLine().split("\\s+")[2];
			String nodeName = new File(child).getName();
			String maniName = brco.readLine().split("\\s+")[1];
			
			if (maniName.equals("create.mani"))
			{
				for (File file: ciManiFiles)
				{
					BufferedReader br = new BufferedReader(new FileReader(file));
					//	Skip first seven lines to get to parent
					for (int i=0; i<7; i++) 
					{ 
						br.readLine(); 
					}
					String filename = new File(br.readLine().split("\\s+")[2]).getName();
					if(filename.equals(nodeName))
					{
						Node node = new Node(nodeName, file, "root");
						nodes.add(node);
					}
					
					br.close();
				}
			}
			
			for (File ciManiFile: ciManiFiles)
			{	
				if(ciManiFile.getName().equals(maniName))
				{
					BufferedReader brci = new BufferedReader(new FileReader(ciManiFile));
					
					//	Skip first seven lines to get to parent
					for (int i=0; i<7; i++) 
					{ 
						brci.readLine(); 
					}
					parent = brci.readLine().split("\\s+")[2];
					String parentName = new File(parent).getName();
					for (File file: ciManiFiles)
					{
						BufferedReader br = new BufferedReader(new FileReader(file));
						//	Skip first seven lines to get to parent
						for (int i=0; i<7; i++) 
						{ 
							br.readLine(); 
						}
						String filename = new File(br.readLine().split("\\s+")[2]).getName();
						if(filename.equals(nodeName))
						{
							Node node = new Node(nodeName, file, parentName);
							nodes.add(node);
						}
						
						br.close();
					}
					
					brci.close();
				}
			}
			
			brco.close();
		}
		
		return nodes;
	}
}