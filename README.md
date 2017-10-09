Program Name: SCM

Team: 
	
	Quan Nguyen
	Marvin Mendez

Contact Info: 
	
	Quan: quanlynguyen90@gmail.com
	Marvin: reacxtion@gmail.com

Class Number: CECS 543

Project Part: #1: Create Repository

Intro:

	This project part consists of building the first portion of our SCM (Source Code Management) project in Java with a development team. Only the initial use-case: Create Repo is implemented.

External Requirements: (minimum JDK 8), Windows in order to follow Build, Installation, and Setup instructions

Build, Installation, and Setup:

	If you do not have at least JDK 8, go to http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html and install. Once installed set path to bin folder of the jdk, located usually in Program Files.
	
	To set the path type "set "path=%path%;[Directory of jdk bin here]"" in a Windows command prompt or edit Environment Variables add the directory of the jdk bin to PATH. 
	 
	If jdk is installed and path is set, then from the command prompt go to directory of the project .java files. Once there type "javac Main.java Repository.java" to create class files.
	
	To Run: Once class files are created, run program by typing "java -cp ./classes;. Main"
	To Create Executable: Once class files are created, create jar by typing "jar cvfm scm.jar manifest.txt *.class". Execute by typing "scm.jar" or "java -jar scm.jar".

	In order for the program to run two folders should exist on the user's computer. One folder, which must be populated with other folders and files, acts as the source folder. The second folder, which must be empty, acts as the target folder to place the created repository.


Usage:

	When the program is ran the user is prompted through the console to "Enter the source tree path:". Here the user must provide the full source path to the original project to be copied into the repository. The user must press enter/return when finished. A new prompt pops up on the console asking the user to "Enter target repo folder path:". Here the user must provide the full target path to the repository folder and press enter/return when finished. The program will then run and output to the console "repo created" if successful.

Extra Features: None

Bugs: Assumes target repository folder is empty. New files and folders will not be created if target repository folder is already populated with files. 