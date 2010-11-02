package bpel.parse;
import java.util.*;

/**
 * Highest level element used for parsing text
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 *
 */
abstract public class ParserElement 
{
		  public int depth;
		  public String type;
		  public String name;


		  /**
			* Constructor for a ParserElement
			* @param oname The name of this element
			* @param otype The type of this element
			* @param odepth The indentation level we are currently at
			*/
		  public ParserElement( String oname, String otype, int odepth )
		  {
					 name = oname;
					 type = otype;
					 depth = odepth;
		  }
}
