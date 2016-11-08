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
		
		// Creating 'postingFile' 'indexFile' tables
		try {
			ms.initTables();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		File folder = new File( System.getProperty("user.dir")+"\\WebContent\\db_test" );
		try {
			ms.clear_db_tables();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		while (true){
			File[] listOfFiles = folder.listFiles();
			boolean isNew = false;
			try {
				for (File file : listOfFiles) {
				    if (file.isFile()) {
				    	isNew = ms.insert_file_to_db_if_doesnt_exists_or_deleted_before("text", file.getPath(), file.lastModified());	
				    }
				    else{
				    	//Check the folder of pictures
				    	if (file.isDirectory() && file.getName().equals("images")){
				    		File[] listOfImages = file.listFiles();
			    			for (File image : listOfImages) {
			    				if (image.isFile() && !image.getName().split("\\.")[image.getName().split("\\.").length-1].equals("txt")) {
			    					isNew = ms.insert_file_to_db_if_doesnt_exists_or_deleted_before("image", image.getPath(), image.lastModified());	
							    }
			    			}
				    	}
					}
				}
			if(isNew){
				// Sorting the 'indexFile' by an alphabet order
				ms.sortByWord();

				// Removing duplicates from the 'indexFile'
				ms.removeDuplicate();
			
				ms.check_if_all_file_exists_by_posting_table_paths();
				
			}	
			Thread.sleep(2);
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
