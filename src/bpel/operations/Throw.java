package bpel.operations;
import bpel.calls.*;
import bpel.parse.YamlParser;
import java.util.*;
import bpel.Log;

/**
 * TODO: Throw and fault were giving me headaches
 *       they are currently cut out until I have time to put them back in and debug.
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Throw extends LeafNode 
{
		  public void action(int id)
		  {
		  }

		  public Throw( String oname, int odepth )
		  {
					 super(oname, "If", odepth);
		  }
}
