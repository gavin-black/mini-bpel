package bpel.operations;
import bpel.database.*;
import bpel.Log;

/**
 * If node, NOTE: Differs from the normal BPEL spec, documented better in action
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class If extends InternalNode
{
		  /**
			* Top level if operation.  Can contain ifMain, elseIf, and else operations underneath.
			*
		   * Note: This deviates considerably from the Netbeans implementation.  
			*       The standard for BPEL ifs puts the if execution a level above elseifs and elses
			*       Example:
			*          if(condition) 
			*             doIffyStuff
			*             elseif(condition)
			*                doElseIffyStuff
			*             else
			*                doElseyStuff
			*          
			*          When they actually mean:
			*            if(condition) 
			*              doIffyStuff
			*            elseif(condition)
			*              doElseIffyStuff
			*            else
			*              doElseyStuff
			*       This causes so many headaches, and is so incredibly counter-intuitive that I am not following it
			*       Instead there is now a MainIf section that behaves exactly like an ElseIf
			*
			* @param id The id of the BPEL process 
			*/ 
		  public void action( int id )
		  {
					String logFunction = "If.action";
					Log.logEntry( id, logFunction);
					String state = "run";
				   try
					{
							  BpelResultSet rs = BpelDatabase.query("SELECT state FROM bpel_process WHERE id=" + id);
							  rs.next();
							  state = rs.getString("state");
		         } catch ( Exception e ) {
							  System.out.println(e);
					}

					for(int i = 0; i < children.size(); i++)
					{
							  ElseIf child = (ElseIf)children.elementAt(i);
							  if( "resume".equals(state) )
							  {
										 if( children.elementAt(i).execute(id) ) break;
							  }
							  else if( "run".equals(state) && child.evalCondition(id) ) 
							  {
										 children.elementAt(i).execute(id);
										 break;
							  }
					} 
					Log.logExit( id, logFunction);
		  }

		  /**
			* Constructor
			*
			* @param oname The name of the node
			* @param odepth The indentation level
			*/
		  public If( String oname, int odepth )
		  {
					 super(oname, "If", odepth);
		  }

}
