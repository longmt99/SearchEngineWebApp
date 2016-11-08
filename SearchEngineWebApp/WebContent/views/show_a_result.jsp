<%@page import="java.io.FileReader"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.File"%>
 <%@ page language="java" contentType="text/html; charset=windows-1255"
    pageEncoding="windows-1255"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=windows-1255">
		<title>Search result</title>
	</head>
	<body>
		<article id="numberOfResults">
			<p>
				<%
					String filePath = (String)request.getParameter("filePath");
					String everything="";
					String jspPath = session.getServletContext().getRealPath("/");
					
				if (filePath.endsWith("txt")){	
					File file = new File(jspPath+filePath);
					BufferedReader br = new BufferedReader(new FileReader(file.getPath()));
					StringBuilder sb = new StringBuilder();
					String line = br.readLine();
			
					while (line != null) {
						sb.append(line);
						sb.append("<br/>");
						line = br.readLine();
					}
					br.close();
					everything = sb.toString();
				}else {
					everything = "images";
				}
				
					String print = "<br/><a href='javascript:window.print()'  target='_blank' class='resultTitleLink'>"
							+"<img src='../views/includes/images/printer.png'  height=22 width=28 alt='print this page' id='print-button' />Print this page</a><br/>";
					if (!filePath.endsWith("txt")){
						out.println(print+ "<img src='"+filePath+"'></img>"  + print);
					}else{
						out.println(print + everything + print);
						out.flush();
					}
				%>
			</p>
		</article>
	</body>
</html>
