package bpel.operations;
import bpel.Log;

/**
 * Completely empty operation, with a description only
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Empty extends LeafNode
{

		  private String desc = "";
		  /**
			* This is an empty node, so do nothing
			*
			* @param id The current process
			*/
		  public void action(int id)
		  {
					 // NOOP
		  }

		  /**
			* Constructor
			*
			* @param oname The name of the node
			* @param odepth The indentation level
			* @param description Description associated with this node
			*/
		  public Empty( String oname, int odepth, String description)
		  {
					 super(oname, "Empty", odepth);
					 desc = description;
		  }

}
