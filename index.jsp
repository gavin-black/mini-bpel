<%@ page import="bpel.*"%>
<%
{
   String action = request.getParameter( "action" );
   String bpel_file = request.getParameter( "bpel_file" );
   String id = request.getParameter( "id" );
   String args = request.getParameter( "args" );
   String variables = request.getParameter( "action" );
   String parents = request.getParameter( "parents" );
   String current = request.getParameter( "current" );
   String params = "";
   boolean runBpel = true;

   // Build up the string
   if( id != null && action != null ) params = "id=" + id + "&action=" + action;
   else if ( id == null && action != null ) params = "action=" + action;
   else if ( bpel_file != null )
   {
      params = "bpel_file=" + bpel_file; 
      if( variables != null ) params += "&variables=" + variables + "&parents=" + parents + "&current=" + current;
      else if( args != null ) params += "&args=" + args;
   } else {
      out.print("Error: Invalid arguments given");
      runBpel = false;
   }

   if( runBpel )
   {
      BpelEngine bpelEngine = new BpelEngine();
      bpelEngine.begin(params);
      out.print(bpelEngine.getResponse());
   }
}
%> 

