Program Name: Repo

Team: 
	
	Quan Nguyen
	Marvin Mendez
	Mingtau Li

Contact Info: 
	
	Quan: quanlynguyen90@gmail.com
	Marvin: reacxtion@gmail.com
	Mingtau: mingtau.li@gmail.com

Class Number: CECS 543

Project Part: #3: Merging

Intro:

	This project is a continuation of our SCM (Source Code Management) project in Java. Use-case: Implement merging of two prject trees.

External Requirements: (minimum JDK 8), Windows in order to follow Build, Installation, and Setup instructions

Build, Installation, and Setup:

	If you do not have at least JDK 8, go to http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html and install. Once installed set path to bin folder of the jdk, located usually in Program Files.
	
	To set the path type "set "path=%path%;[Directory of jdk bin here]"" in a Windows command prompt or edit Environment Variables add the directory of the jdk bin to PATH. 
	 
	If jdk is installed and path is set, then from the command prompt go to directory of the project .java files. Once there type "javac Main.java Repository.java" to create class files.
	
	To Run: Once class files are created, run program by typing "java -cp ./classes;. Main"
	To Create Executable: Once class files are created, create jar by typing "jar cvfm scm.jar manifest.txt *.class". Execute by typing "scm.jar" or "java -jar scm.jar".

	In order for the program to run properly the target folders, which will be needed to peform the check-out and check-in commands, must be empty.


Usage:

	When the program is first ran the user will provided with the following menu:
		
		Repository Commands
		
		create-repo [source folder] [target folder]
		check-in [source folder] [target folder]
		check-out [source folder] [target folder]
		label-manifest [manifest file] [label]
		merge [source folder] [target folder]
		exit-menu
		Enter the command and arguments you would like to perform:

	The user can then enter the command and its arguments (absolute paths) to perform the respective actions displayed. If the check-out command is performed the user will be prompted to enter a label or manifest file to checkout from. Merge will also ask the user for two manifest files. A repo manifest file to merge from and a target project tree manifest file to merge to. The exit-menu command will exit the menu and close the program.

Extra Features: None

Bugs: 
	Assumes target repository folder is empty. New files and folders will not be created if target repository folder is already populated with files. Assumes no spaces in directories.
	Similarly named files will not create unique AID's after two of the same files are found in the repo. Merge from path is currently not implemented.