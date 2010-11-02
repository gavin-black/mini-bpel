package bpel.operations;
import bpel.database.Variable;
import bpel.Log;

/**
 * The assign bpel operation
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Assign extends InternalNode 
{
		  /**
			* Assign is really just a renamed sequence with Copy nodes inside
			*
			* @param id The current thread
			*/
		  public void action( int id )
		  {
					 String logFunction = "Assign.action";
					 Log.logEntry( id, logFunction);
					 for(int i = 0; i < children.size(); i++)
					 {	
								children.elementAt(i).execute(id);
					 }
					 Log.logExit( id, logFunction, "void");
		  }

		  /**
			* Constructor for making an Assign operation
			*
			* @param oname The name of the operation
			* @param odepth The current indentation level
			*/
		  public Assign( String oname, int odepth )
		  {
					 super( oname, "Assign", odepth );
		  }

}
