package il.ac.shenkar.searchEngine;

public class SearchEngine {
	
	static FolderScanner fs;
	public static void main(String[] args) {
	    System.out.println("folderScanner is starting...");
	    fs = FolderScanner.getInstance();
		fs.run();
	}
// It is your code, I just change some data structure for implement indexing files 
}
