<%@ page import="java.sql.*, java.net.URLConnection, java.net.*, java.util.*, java.io.*" %>
<html>
<body>

<%
    String sqlFileUrl = "http://localhost:8080/mini_bpel/setup/mysqlCommands.sql";
    String connectionURL = "jdbc:mysql://localhost:3306/";
  
    String user = request.getParameter("user");
    String pass = request.getParameter("pass");

    if ( null == user || "".equals(user) )
    {
        out.println("<H3>Database Login</H3>");
        out.println("<FORM action=\"index.jsp\" method=\"post\">");
        out.println("Username: <input type=\"text\" name=\"user\"><BR>");
	out.println("Password: <input type=\"password\" name=\"pass\"><BR>");
        out.println("<input type=\"submit\" value=\"Submit\">");
	out.println("</FORM>");
    } else { 
        boolean foundError = false;
        String sqlCommand = "";
        Connection connection = null;
        Statement statement = null;
	ResultSet rs = null;
        if(pass.indexOf("mitreis") >= 0 ) pass = "mitreis#1";
	Class.forName("com.mysql.jdbc.Driver").newInstance();
	try
	{
	  connection = DriverManager.getConnection(connectionURL, user, pass);
	  statement = connection.createStatement();
	  URL theURL = new URL(sqlFileUrl);
	  URLConnection connectionU = theURL.openConnection();
          HttpURLConnection conn = (HttpURLConnection) connectionU;
	  InputStream in = conn.getInputStream();
          BufferedReader rd  = new BufferedReader(
              new InputStreamReader(conn.getInputStream()));

	  // Step through each line of the file    
	  String line = "";
	  while ((line = rd.readLine()) != null) 
	  {
	      // The execute command fails if it's not a complete statement
	      // Therefore avoid running until we see a semi-colon
	      sqlCommand += line;
	      if( sqlCommand.indexOf(";") >= 0 )
	      {
	         statement.execute(sqlCommand);
		 sqlCommand = "";
	      }
	  }
	  connection.close();
	} catch (Exception e) {
	   out.println("Error Adding To the Database<BR>" + e);
	   out.println("<BR><BR><a href=\"index.jsp\">Go back</a>");
	   foundError = true;
	}
	if( !foundError) out.println("Successfully Setup Service");
    }
%>

</body>
</html> 

