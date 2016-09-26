package il.ac.shenkar.searchEngine;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


public class FolderScanner implements Runnable{
	private static final FolderScanner INSTANCE = new FolderScanner();
	private  MysqlConnector ms;
	
	private FolderScanner() {
		super();
		ms = MysqlConnector.getInstance();
		
		// creating 'postingFile' 'indexFile' tables
		try {
			ms.initTables();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		File folder = new File( System.getProperty("user.dir")+"\\WebContent\\db" );
		try {
			ms.clear_db_tables();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		while (true){
			File[] listOfFiles = folder.listFiles();
			try {
				for (File file : listOfFiles) {
				    if (file.isFile()) {
				    	ms.insert_file_to_db_if_doesnt_exists_or_deleted_before("text", file.getPath(), file.lastModified());	
				    }
				    else{
				    	//check the folder of pictures
				    	if (file.isDirectory() && file.getName().equals("images")){
				    		File[] listOfImages = file.listFiles();
			    			for (File image : listOfImages) {
			    				if (image.isFile() && !image.getName().split("\\.")[image.getName().split("\\.").length-1].equals("txt")) {
						    		//this is image file
			    					//System.out.println( image.getName() );
						    		ms.insert_file_to_db_if_doesnt_exists_or_deleted_before("image", image.getPath(), image.lastModified());	
							    }
			    			}
				    	}
					}
				}
			
				// Sorting the index file by an alfabetic order
				ms.sortByWord();

				// Removing duplicates from the index file
				ms.removeDuplicate();
			
				ms.check_if_all_file_exists_by_posting_table_paths();
				
				//if (listOfFiles.length==0) {
					// Clear DB tables
					//ms.clear_db_tables();
				//}
				
				Thread.sleep(10);
			} catch (InterruptedException | SQLException | IOException e) {
				e.printStackTrace();
			}
		}
	}



	//Getters and setters
	public static FolderScanner getInstance() {
        return INSTANCE;
    }
	public MysqlConnector getMs() {
		return ms;
	}

	public void setMs(MysqlConnector ms) {
		this.ms = ms;
	}
	
	
}
