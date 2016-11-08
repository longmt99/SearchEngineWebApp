package il.ac.shenkar.searchEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/* This class is responsible for:
 * 1.Initializing a connection to the MySQL server
 * 2.Manages functionality on the 'index file' table 
 */

public class MysqlConnector {
	private static final MysqlConnector INSTANCE = new MysqlConnector();
	private static final int GMISHUT = 1;
	Connection connection = null;
	Statement statement = null;

	private MysqlConnector() {
		try {
			// initializing connection to the database
			Class.forName("com.mysql.jdbc.Driver");

			connection = DriverManager.getConnection("jdbc:mysql://localhost/searchengine", "root", "root");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// getter for MysqlConnecor instance
	public static MysqlConnector getInstance() {
		return INSTANCE;
	}

	public void initTables() throws SQLException {
		createTable_postingFile();
		createTable_indexFile();
	}

	// This function creates,if does not exists, a new 'indexFile' table
	public void createTable_indexFile() throws SQLException {
		String createIndexTable = "CREATE TABLE indexFile ("
				+ "        word VARCHAR(30) NOT NULL, "
				+ "        docNumber  VARCHAR(500) NOT NULL DEFAULT '', "
				+ "        freq VARCHAR(500) DEFAULT '', "
				+ "		   hits INT(4) DEFAULT 0 )";

		try {
			statement = connection.createStatement();
			statement.executeUpdate(createIndexTable);
			statement.close();
		} catch (SQLException e) {
			// Check table if exist
			statement = connection.createStatement();
			String query = "SELECT 1 FROM postingFile LIMIT 1";
			ResultSet rs = statement.executeQuery(query);

			rs.last();
			int equalNumRows = rs.getRow();
			statement.close();

			if (equalNumRows == 1) {
				System.out.println("\nindexFile table already exist!");
			}
		}
	}

	// This function creates,if doesn't exists, a new 'postingFile' table
	public void createTable_postingFile() throws SQLException {
		String createPostingFileTable = "CREATE TABLE postingFile ("
				+ "        docPath VARCHAR(300) NOT NULL, "
				+ "        docNumber INT(4) KEY AUTO_INCREMENT,"
				+ "		   deleted INT(1) DEFAULT 0, "
				+ "		   lastIndex BIGINT DEFAULT 0,"
				+ "		   isPicture BOOLEAN NOT NULL DEFAULT 0,"
				+ "		   tagNames VARCHAR(300) NOT NULL DEFAULT '' )";   

		try {
			statement = connection.createStatement();
			statement.executeUpdate(createPostingFileTable);
			statement.close();
		} catch (SQLException e) {
			statement = connection.createStatement();
			String query = "SELECT 1 FROM postingFile LIMIT 1";
			ResultSet rs = statement.executeQuery(query);

			rs.last();
			int equalNumRows = rs.getRow();
			statement.close();

			if (equalNumRows == 1) {
				System.out.println("\npostingFile table already exist!");
			}
		}
	}

	public void clear_tables() throws SQLException {
		clear_table_indexFile();
		clear_table_postingFile();
	}

	public void clear_table_indexFile() throws SQLException {
		// Clear the 'indexFile' table that already exists
		statement.executeUpdate("TRUNCATE TABLE indexFile;");
		System.out
				.println("This table already exist! Clearing the existing one...");
		statement.close();
	}

	public void clear_table_postingFile() throws SQLException {
		// Clear the 'postingFile' table that already exists
		statement.executeUpdate("TRUNCATE TABLE postingFile;");
		System.out
				.println("This table already exist! Clearing the existing one...");
		statement.close();
	}

	// This function inserts a new row into the 'posting File' table
	public void insert_file_postingFile(String type, String path, long lastModified, String tagNames) throws SQLException, IOException {
		if (type.equals("text")){
			PreparedStatement prepstate = connection
					.prepareStatement("INSERT INTO `postingFile` (`docPath`, lastIndex) "
							+ "VALUES (?, ?)");
			prepstate.setString(1, path);
			prepstate.setLong(2, lastModified);
			prepstate.execute();
			prepstate.close();
		}else{
			//Open tag-name file and create a string of all text in it.
			//get the (tag-name)txt file name from image path

			//Updates SQL
			PreparedStatement prepstate = connection
					.prepareStatement("INSERT INTO `postingFile` (`docPath`, lastIndex, isPicture, tagNames) "
							+ "VALUES (?, ?, ?, ?)");
			prepstate.setString(1, path);
			prepstate.setLong(2, lastModified);
			prepstate.setInt(3, 1);
			prepstate.setString(4, tagNames);
			prepstate.execute();
			prepstate.close();
		}
	}
	
	// This function inserts a pic into the 'postingFile' table
		public void insert_pic_postingFile(String path, String fileExtension, long lastModified, String tagNames) throws SQLException {
			PreparedStatement prepstate = connection
					.prepareStatement("INSERT INTO `postingFile` (`docPath`, lastIndex, isPicture, tagNames) "
							+ "VALUES (?, ?, ?, ?)");
			prepstate.setString(1, path);
			prepstate.setLong(2, lastModified);
			prepstate.setInt(3, 1);
			prepstate.setString(4, tagNames);
			prepstate.execute();
			prepstate.close();
		}

	// This function inserts a new row into the 'indexFile' table
	public void insert_indexFile(String word, int docNum, int freq)
			throws SQLException {
		PreparedStatement prepstate = connection
				.prepareStatement("INSERT INTO `indexFile` (`word`, `docNumber`, `freq`) "
						+ "VALUES (?, ?, ?)");
		prepstate.setString(1, word.toLowerCase());
		prepstate.setInt(2, docNum);
		prepstate.setInt(3, freq);

		prepstate.execute();
		prepstate.close();
	}

	// This function is executed when we want to sort the 'indexFile' database table by 'word' column
	public void sortByWord() throws SQLException {
		statement = connection.createStatement();
		statement.execute("ALTER TABLE `indexFile` ORDER BY word ASC;");
		statement.close();
	}

	/* Start Tomcat and show data indexed.
	 * This function removes duplicate (word + document number) sets Before
	 * deleting it sums the 'freq' column
	 */
	public void removeDuplicate() throws SQLException {
		statement = connection.createStatement();
		try {
			// Creating a temporary table that will store a table without duplicate rows
			statement.executeUpdate("CREATE temporary TABLE tsum AS"
					+ "		SELECT word,GROUP_CONCAT(freq) as freq, GROUP_CONCAT(docNumber) docNumber,  hits "
					+ "		FROM indexfile group by word;");
			// Clearing completely the 'indexFile' table
			statement.executeUpdate("TRUNCATE TABLE indexFile;");
	
			// Inserting into the empty 'indexFile' table all rows from the temporary table
			statement.executeUpdate("INSERT INTO indexFile (`word`,`docNumber`,`freq`, `hits`)"
							+ "		SELECT word,docNumber,freq,hits " + "		FROM tsum;");
			
			String query = "SELECT word,freq  FROM indexFile " ;
			ResultSet rs = statement.executeQuery(query);
			List<FileDescriptor> list = new ArrayList<FileDescriptor>();
			while (rs.next()) {
				String word = rs.getString("word");
				String freq = rs.getString("freq");
				FileDescriptor file = new FileDescriptor();
				file.setTitle(word);
				file.setPreview(freq);
				list.add(file);
			}
			
			for (FileDescriptor fileDescriptor : list) {
				updateHitsInIndexFile(fileDescriptor.getTitle(),fileDescriptor.getPreview());
			}
		} finally {
			// Deleting the temporary table we used
			statement.executeUpdate("DROP TEMPORARY TABLE IF EXISTS tsum;");
			statement.close();
		}
		
	}

	// Remove all the words from DB that corresponding to a file
	public void removeFileWords(int docNum) throws SQLException {
		statement = connection.createStatement();
		statement.executeUpdate("DELETE FROM indexFile "
				+ "		WHERE docNumber =" + docNum + ";");
		System.out
				.println("Step 1/2 - removed file from the posting file ,in index: "
						+ docNum);
		statement.close();
	}


	public int checkNumRows_postingFile() throws SQLException {
		int counter = 0;
		statement = connection.createStatement();

		String query = "SELECT * FROM  `postingfile`;";
		try {
			ResultSet rs = statement.executeQuery(query);
			rs.last();
			int numRows = rs.getRow();
			return numRows;
		} catch (SQLException e) {
			e.printStackTrace();
			/* Currently table currently doesn't have any lines */
			System.out
					.println("Posting file table currently doesnt have any files");
		}
		return 0;
	}

	// Remove the row from DB that have this docNumber
	public void deleteDocRow_by_number_postingFile(int docNum)
			throws SQLException {
		statement = connection.createStatement();
		int check = statement.executeUpdate("UPDATE postingFile "
				+ "		SET deleted=1 " + "		WHERE docNumber =" + docNum + ";");
		System.out
				.println("step 2/2 - logical delete the row from DB that have this docNumber ");
		statement.close();
		System.out.println("Removing completed");
	}

	/** have new one or not
	 * 
	 * @param type
	 * @param path
	 * @param lastModified
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public boolean insert_file_to_db_if_doesnt_exists_or_deleted_before(String type, String path, long lastModified)
			throws SQLException, IOException {
		
		// Fix path (ex. c:\\abc\\file...)
		String pathFix = path.replace("\\", "\\\\");
		// Check if a path already exists in the table
		statement = connection.createStatement();
		String query = "SELECT docPath FROM postingFile "
				+ "		WHERE docPath ='" + pathFix + "'";
		ResultSet rs = statement.executeQuery(query);

		rs.last();
		int equalNumRows = rs.getRow();
		statement.close();
		// If the path does not exist in the 'PostingFile' table, we will add it
		if (equalNumRows == 0) {
			
			String pathOfTagNames;
			String tagNames = null; 
			// The complete text from the file
			if (type.equals("image")){
				pathOfTagNames = path.substring(0, path.length()-3)+"txt";
				
				//Read file
				File file = new File(pathOfTagNames);
				BufferedReader br = new BufferedReader(new FileReader(file.getPath()));
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
		
				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				br.close();
				tagNames = sb.toString().toLowerCase();
				tagNames = tagNames.replaceAll("(?<!\\d)\\.|\\.+$|[^a-zA-Z0-9. ]"," ");
				tagNames = tagNames.replaceAll("\r", "");
				tagNames = tagNames.replaceAll("\n", " ");
			}
			
			insert_file_postingFile(type, path, lastModified, tagNames);

			// Get file docNum by path
			int docNum = get_docNum_by_path_from_postingFile_table(path);
			
			// Insert to 'indexFile' table
			parseFile_and_add_to_index_file_table(type, path, docNum, tagNames);

			System.out.println("\nAdd completely - " + path);
		return true;
		// The path exist in 'postingFile' table
		//Check if this file(path) has removed from db folder (deleted==1)
		} else {
			
			statement = connection.createStatement();
			query = "SELECT docPath,docNumber	FROM postingFile "
					+ "		WHERE docPath ='" + pathFix + "' AND deleted=1";
			rs = statement.executeQuery(query);
			rs.last();
			equalNumRows = rs.getRow();

			// If there are rows that equals to a path that was deleted in the past
			if (equalNumRows != 0) {
				
//				// Insert to 'indexFile' table
			//The path is exist and deleted==0
			// Check for changes on this file
			}else{
				
			}
		}
		return false;
	}

	private void setNew_lastModified_in_postingFile(int docNumber,long lastModified) throws SQLException {
		statement = connection.createStatement();

		// Change deleted column to 0
		int check = statement.executeUpdate("UPDATE postingFile "
					+ "		SET lastIndex="+ lastModified
					+ "		WHERE docNumber =" + docNumber);

		statement.close();
	}

	public int get_docNum_by_path_from_postingFile_table(String path)
			throws SQLException {
		path = path.replace("\\", "\\\\");
		statement = connection.createStatement();
		String query = "SELECT * FROM `postingFile` WHERE `docPath` ='" + path
				+ "';";
		ResultSet rs = statement.executeQuery(query);

		int docNumber = -1;
		rs.next();
		docNumber = rs.getInt("docNumber");

		return docNumber;
	}

	/*
	 * This function does several things: 
	 * 1. Parses a text file 
	 * 2. Adds every word from the file to the 'IndexFile' table 
	 * 3. Sorts the 'indexFile' structure
	 */
	public void parseFile_and_add_to_index_file_table(String type, String path, int docNum, String tagNames)
			throws IOException, SQLException {
		
		String everything = null; 
	// The complete text from the file
		String words[];
		
		if (type.equals("text")){	
			File file = new File(path);
			BufferedReader br = new BufferedReader(new FileReader(file.getPath()));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
	
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			br.close();
			everything = sb.toString();
		}else if (type.equals("image")){
			everything = tagNames;
		}
		// Removing special characters from the text
		everything = everything.replaceAll("(?<!\\d)\\.|\\.+$|[^a-zA-Z0-9. ] [^?(a-zA-Z0-9?)]"," ");
		everything = everything.replaceAll("\r", "");
		everything = everything.replaceAll("\n", " ");
		words = everything.split(" ");
		for (String tmpWord : words) {
			tmpWord.trim();
			if (!tmpWord.equals("")) { 
		// Makes sure we don't add an empty words to 'indexFile' table

				// Add this word to the database
				if(checkWordExistedInIndexFile(tmpWord,docNum)){
					updateFreqInIndexFile(tmpWord,docNum);
				}else{
					insert_indexFile(tmpWord, docNum, 1);
				}	
			}
		}
	}

	/*
	 * This function checks if the paths from the 'postingFile' table are valid
	 * If not, clear it from the 'postingFile' and its words from the 'indexFile'
	 */
	public void check_if_all_file_exists_by_posting_table_paths()
			throws SQLException {

		/* check if all path's are still valid */
		// Get all rows from postingFile table
		statement = connection.createStatement();
		String query = "SELECT * FROM `postingFile` WHERE deleted=0";
		ResultSet rs = statement.executeQuery(query);

		List<Integer> docNumbersToDelete = new ArrayList<Integer>();
		while (rs.next()) {

			File f = new File(rs.getString("docPath"));
			// if not valid -> Remove file from list
			if (!f.exists()) {

				// Save all document numbers of paths that are not valid
				docNumbersToDelete.add(rs.getInt("docNumber"));
			}
		}
		statement.close();
		for (int i = 0; i < docNumbersToDelete.size(); i++) {
			System.out.println("\nDetect invalid path in postingFile");
			// Remove all words from DB that Attributed to this path file (by filenumber)
			removeFileWords(docNumbersToDelete.get(i));

			// Remove the file path from the 'postingFile'
			deleteDocRow_by_number_postingFile(docNumbersToDelete.get(i));
		}
	}

	public void clear_db_tables() throws SQLException {
		clear_indexFile_table();
		clear_postingFile_table();
	}

	public void clear_indexFile_table() throws SQLException {
		Statement statement = connection.createStatement();
		statement.executeUpdate("TRUNCATE TABLE indexFile;");
		statement.close();
	}

	public void clear_postingFile_table() throws SQLException {
		Statement statement = connection.createStatement();
		statement.executeUpdate("TRUNCATE TABLE postingFile;");
		statement.close();
	}

	public List<String> analyzeQuery(String searchQuery) throws SQLException,IOException {
	    String str = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
		String stopList[] = str.split(",");
				
		for (String s : stopList){
			String regex = "\\s*\\b"+s+"\\b\\s*";
			searchQuery = searchQuery.replaceAll(regex, " ");
		}
		
		System.out.println(searchQuery);
		
		// Split by OR operator
		List<String> splitByOR = new ArrayList<String>(Arrays.asList(searchQuery.split(" OR ")));

		// Run on every element of splitByOR list and remove AND word and get array of words.
		for (int i=0; i<splitByOR.size(); i++) {
			String query = splitByOR.get(i).replace(" AND ", " ");
			query = query.trim();
			splitByOR.set(i, query);
		}

		return splitByOR;
	}

	public List<FileDescriptor> getDocNumResults(List<String> splitedQueryList)
			throws SQLException {
		List<FileDescriptor> resultDocNumbers = new ArrayList<FileDescriptor>();
		List<FileDescriptor> docNumbers_ToRemove = new ArrayList<FileDescriptor>();
		List<FileDescriptor> docNumbers_ToRemoveFRom = new ArrayList<FileDescriptor>();
		for (String words : splitedQueryList) {

			/*
			 * str can be:
			 * 
			 * [victory , dog NOT big duck] 2,3 cat 4 hot , blue dog 5 pig NOT
			 * loop gorj
			 */
			
			// If words contains a substring "NOT"
			if (words.contains("NOT")) {
				// list - dog, big duck
				// remove 'NOT' and split
				// string(0) , string(1)
				List<String> list_of_not_parts = new ArrayList<String>(Arrays.asList(words.split(" NOT ")));

				// take string(0) split by space " "
				List<String> tmp = new ArrayList<String>(Arrays.asList(list_of_not_parts.get(0).split(" ")));
				// Get docNum by words
				docNumbers_ToRemoveFRom = getDocNumList(tmp);

				// take string(1) split by space " "
				tmp = new ArrayList<String>(Arrays.asList(list_of_not_parts.get(1).split(" ")));
				// Get docNum by words
				docNumbers_ToRemove = getDocNumList(tmp);

				for (FileDescriptor doc : docNumbers_ToRemove) {
					int num = doc.getDocNum();
					int index_to_remove = docNumbers_ToRemoveFRom.indexOf(num);
					if (index_to_remove != -1) {
						docNumbers_ToRemoveFRom.remove(index_to_remove);
					}
				}
				
				// Add numbers to main list of document numbers
				for (FileDescriptor doc : docNumbers_ToRemoveFRom) {
					int num = doc.getDocNum();
					if (resultDocNumbers.indexOf(num) == -1) {
						resultDocNumbers.add(doc);
					}
				}

			// Search query without NOT
			} else {
				
				List<String> tmp = new ArrayList<String>(Arrays.asList(words.split(" ")));
				
				// Get docNum by words
				List<FileDescriptor> docNumbers_to_add_if_need = getDocNumList(tmp);
				// Add numbers to main list of document numbers
				for (FileDescriptor doc : docNumbers_to_add_if_need) {
					int num = doc.getDocNum();
					if (resultDocNumbers.indexOf(num) == -1) {
						resultDocNumbers.add(doc);
					}
				}
			}
		}
		return resultDocNumbers;
	}
	/**
	 * Get and check 1 Word of one file is existed or not in indexFile
	 * @param doc
	 * @return
	 * @throws SQLException 
	 */
	private boolean checkWordExistedInIndexFile(String  word , int docNum) throws SQLException{
		statement = connection.createStatement();
		String query = "SELECT docNumber,freq  FROM indexFile "
				+ "		WHERE word = '" + word + "'"
						+ " AND  docNumber = " + docNum + "" ;
		ResultSet rs = statement.executeQuery(query);
		while (rs.next()) {
			return true;
		}
		return false;
	}
	/**
	 * update Freq In IndexFile if existed 
	 * @param word
	 * @param docNum
	 * @throws SQLException
	 */
	private void updateFreqInIndexFile(String  word , int docNum) throws SQLException {
		statement = connection.createStatement();

		// Change freq=freq +1
		String updateSQL = "UPDATE indexFile "
					+ "		SET freq=freq +1"
					+ "		WHERE docNumber ='" + docNum+"'"
					+ "	    	AND word ='" + word+"'";
		
		System.out.println("updateSQL:" + updateSQL);
		statement.executeUpdate(updateSQL);

		statement.close();
	}

	private void updateHitsInIndexFile(String  word,String freq ) throws SQLException {
		String[] splitsFreq = StringUtils.split(freq,",");
		int hits =0;
		for (int i = 0; i < splitsFreq.length; i++) {
			int fr= Integer.parseInt(splitsFreq[i]);
			hits +=fr;
		}
		// Change freq=freq +1
		String updateSQL = "UPDATE indexFile "
					+ "		SET hits=" + hits
					+ "		WHERE  word ='" + word+"'";
		
		statement.executeUpdate(updateSQL);

	}
	public List<FileDescriptor> getDocNumList(List<String> stringList)
			throws SQLException {
		List<FileDescriptor> documentNumbers = new ArrayList<FileDescriptor>();
		StringBuilder words = new StringBuilder();
		// example of the string we are trying to create:
		// 'blue','pen','green','key'
		for (String w : stringList) {
			words.append("'" + w + "',");
		}
		// Remove the last ","
		words = words.deleteCharAt(words.length() - 1);

		int numberOfWords = stringList.size();
		
		//if (numberOfWords-1 !=0) numberOfWords--;

		statement = connection.createStatement();
		String query = "SELECT docNumber,freq  FROM indexFile "
				+ "		WHERE word IN (" + words + ")" ;
		ResultSet rs = statement.executeQuery(query);
		while (rs.next()) {
			String docNumberResult = rs.getString("docNumber");
			String freqResult = rs.getString("freq");
			String[] splitsNum = StringUtils.split(docNumberResult,",");
			String[] splitsFreq = StringUtils.split(freqResult,",");
			for (int i = 0; i < splitsNum.length; i++) {
				int num = Integer.parseInt(splitsNum[i]);
				int freq= Integer.parseInt(splitsFreq[i]);
				FileDescriptor doc = new FileDescriptor(num, freq);
				documentNumbers.add(doc);
			}
		}
		return documentNumbers;
	}

	public List<FileDescriptor> create_fileDescriptors_list_by_docNumbers(List<FileDescriptor> docNumbers_of_results) throws SQLException, IOException {
		List <FileDescriptor> fd = new ArrayList<FileDescriptor>();
		for( FileDescriptor doc : docNumbers_of_results ){
			int docNum = doc.getDocNum();
			String selectSQL = "SELECT * FROM postingFile WHERE docNumber=? AND deleted=?";
			PreparedStatement prepstate = connection.prepareStatement (selectSQL);
			prepstate.setInt(1, docNum);
			prepstate.setInt(2, 0);
			ResultSet rs = prepstate.executeQuery();
			
			 while (rs.next()) {
				 // Read file
				 File f = new File(rs.getString("docPath"));
				 BufferedReader br = new BufferedReader(new FileReader(f.getPath()));
				 
				 String line = br.readLine();
				 StringBuilder lineBuffer = new StringBuilder();
				 
				 int counter =0;
				 FileDescriptor fileDes = new FileDescriptor();
				 fileDes.setFreq(doc.getFreq());
				 fileDes.setPath(rs.getString("docPath"));
				 
				 /* init values */
				 fileDes.setTitle(f.getName()); // name
				 Calendar calendar = Calendar.getInstance();
				    calendar.setTimeInMillis(f.lastModified());
				    int mYear = calendar.get(Calendar.YEAR);
				    int mMonth = calendar.get(Calendar.MONTH);
				    int mDay = calendar.get(Calendar.DAY_OF_MONTH);
				    fileDes.setCreationDate(mDay+"."+mMonth+"."+mYear); //creation date
				 fileDes.setAuthor("Author: -"); //Author
				 fileDes.setPreview("no preview"); // name 
				 
				 if (rs.getInt("isPicture")!=1){
					 while (line != null) {
						 // if line number is #0 | #1 | #2 - remove '#' from line variable
						 /* (0) - Title
						 /* (1) - Creation Date
						 /* (2) - Author
						 /* (3) - Preview 
						 */
						
						 switch (counter) {
							 case 0: 
								if (line.contains("Title: ")){
									line = line.substring(1);	//remove the # and take the sentence
									String title= line.replaceAll("Title: ", "");
									fileDes.setTitle(title);
								}
							 break;
							 case 1: 
								if (line.contains("Creation date: ")){
									line = line.substring(1);
							 		String creationDate= line.replaceAll("Creation date: ", "");
							 		fileDes.setCreationDate(creationDate);
								}
							 break;
							 case 2: 
								 if (line.contains("Author: ")){
									line = line.substring(1);
								 	String author= line.replaceAll("Author: ", "");
								 	fileDes.setAuthor(author);
								 }
							 break;
							 case 3: 
								 if (line.contains("Preview: ")){
								 	line = line.substring(1);
								 	String preview= line.replaceAll("Preview: ", "");
								 	fileDes.setPreview(preview);
								 }
							 break;
						 }
						 counter++;
						 lineBuffer.append(line);
						 line = br.readLine();
						 
						 
						 // preview should be with relevant content
						 if (counter==4){
							 if (fileDes.getPreview() == null || fileDes.getPreview().length()==0 || fileDes.getPreview().equals("no preview")){
								 for (int j=0; j<3; j++){
									 if (line!= null) {
										 lineBuffer.append(line);
									 }
									 line = br.readLine();
								 }
								 fileDes.setPreview(lineBuffer.toString());
							 }
							 break;
						 }else if (line== null){
							 if (fileDes.getPreview() == null || fileDes.getPreview().length()==0 || fileDes.getPreview().equals("no preview")){
								 for (int j=0; j<3; j++){
									 if (line!= null) {
										 lineBuffer.append(line);
									 }
									 line = br.readLine();
								 }
								 fileDes.setPreview(lineBuffer.toString());
							 }
						 }
					 }
					 br.close();
				 }else{
					 //This is a picture
					 fileDes.setTitle("Title: (picture) "+rs.getString("tagNames") );
					 fileDes.setCreationDate("Creation date: -");
					 fileDes.setAuthor("Author: -");
					 fileDes.setPreview("Preview: -");
					 fileDes.setPic("true");
				 }
				 
				 fd.add(fileDes);
			 }
		}
		return fd;
	}

	public String readFileContent(String filePath) throws IOException {
		// Read file
		 File f = new File(filePath);
		 BufferedReader br = new BufferedReader(new FileReader(f.getPath()));
		 
		 StringBuilder allText = new StringBuilder();
		 String line = br.readLine();
		
		 while (line != null) {
			 allText.append(line+"<br>");
			 line = br.readLine();
		 }
		 br.close();
		 return allText.toString();
	}

	public void insert_image_to_db_if_doesnt_exists_or_deleted_before(
			String path, long lastModified) {		
	}
}
