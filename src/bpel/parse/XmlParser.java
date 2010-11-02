package bpel.parse;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import org.w3c.dom.*;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;

/**
 * Traverse an XML tree using the DOMParser
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 *
 */
public class XmlParser 
{

   Document doc = null;

  /**
	* Begin recursively traversing the XML tree
	*
	* @return void
	*/
	public void parseTree()
	{
			  parseTree(doc.getDocumentElement(), 0, null);
	}

  /**
	* Action to perform for each element, this should be overwritten to perform
	* the desired functionality
	*
	* @param n Node we are currently on
	* @param depth The level of the current node
	* @param parent The parent ParserElement 
	* @return A null parser element
	*/
   protected ParserElement doAction( Node n, int depth, ParserElement parent )
	{
			  String depthString = "";
			  for(int i = 0; i < depth; i ++) depthString += " ";
			  System.out.println( "PARSE: " + depthString + n.getNodeName() );
			  NamedNodeMap attrs = n.getAttributes();
			  int len = attrs.getLength();
			  for (int i=0; i<len; i++) 
			  {
			     Attr attr = (Attr)attrs.item(i);
				  System.out.println("       " + depthString + "" + 
										attr.getNodeName() + "=" + attr.getNodeValue());
			  }

			  return null;
	}	  

  /**
	* Recursive function for actually traversing the tree
	*
	* @param n Node we are currently on
	* @param depth The level of the current node
	* @param parent The parent ParserElement 
	* @return void
	*/
   private void parseTree(Node n, int depth, ParserElement parent)
	{
			  ParserElement current = null;
			  if( n == null ) return;
			  try
			  {
						 if (!(n.getNodeName()).equals("#text") ) 
						 {
								 current = doAction(n, depth, parent);
								 NodeList children = n.getChildNodes();
								 for( int i = 0; i < children.getLength(); i ++)
						       {
									parseTree(children.item(i), depth + 3, current);
						       }
						 }
           } catch (Exception ex) {
                   System.out.println(ex);
           }

	}

  /**
	* Constructor that handles parsing the XML and preparing it for traversal
	*
	* @param input The raw XML to process
	*/
   public XmlParser(String input)
	{
        try 
		  {
            DOMParser parser = new DOMParser();
				InputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));
            parser.parse( new InputSource(is) );

	         doc = parser.getDocument();
        } catch (Exception ex) {
            System.out.println(ex);
        }
   }
}
