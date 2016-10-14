package il.ac.shenkar.searchEngine;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class TestSearch {

	/**
	 * Test case searchQuery "computer"
	 */
	@Test
	public void  searchQuery() {
		MysqlConnector ms = MysqlConnector.getInstance();
		String searchQuery = "computer"; 
		//System.out.println(searchQuery);

		try {
			List<String> splitedQueryList = ms.analyzeQuery(searchQuery);
			List<FileDescriptor> docNumbers_of_results = ms.getDocNumResults(splitedQueryList);
			List<FileDescriptor> result = ms.create_fileDescriptors_list_by_docNumbers(docNumbers_of_results);
			assertEquals(2, result.size());
			for (FileDescriptor fileDescriptor : result) {
				System.out.println(fileDescriptor);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Test case searchQuery "live"
	 */
	@Test
	public void  searchQuery2() {
		MysqlConnector ms = MysqlConnector.getInstance();
		String searchQuery = "live";
		System.out.println(searchQuery);

		try {
			List<String> splitedQueryList = ms.analyzeQuery(searchQuery);
			List<FileDescriptor> docNumbers_of_results = ms.getDocNumResults(splitedQueryList);
			List<FileDescriptor> result = ms.create_fileDescriptors_list_by_docNumbers(docNumbers_of_results);
			assertEquals(2, result.size());
			for (FileDescriptor fileDescriptor : result) {
				System.out.println(fileDescriptor);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
