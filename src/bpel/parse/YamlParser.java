package bpel.parse;
import org.jvyaml.YAML;
import bpel.database.Variable;
import java.io.*;
import java.util.*;

/**
 * Very simple YAML parser, that maps YAML pairs into a hashmap
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class YamlParser
{
        /**
			* Parse YAML and store the results into variables
			* @param id The bpel process to store the variable with
			* @param topLevelVariable The YAML variable name to store int
			* @param yamlText The text of the YAML response
			*/
		  public static void parse(int id, String topLevelVariable, String yamlText)
		  {
					 HashMap<String,String> p = new HashMap<String,String>();
					 Properties properties = new Properties();
					 System.err.println("TopLevelVar: " + topLevelVariable + "--YamlText: " + yamlText);
					 try
					 {
					    Reader r = new InputStreamReader(new ByteArrayInputStream(yamlText.getBytes("UTF-8")));
					    @SuppressWarnings("unchecked")
					    HashMap<String,String> pro = (HashMap<String,String>) YAML.load(r);
					    r.close();
					    p.putAll(pro);

					    Set set = pro.entrySet();
					    Iterator it = set.iterator();

					    while(it.hasNext())
					    {
			             Map.Entry mapE = (Map.Entry) it.next();
				          Variable.update(id, topLevelVariable + "." + mapE.getKey(), mapE.getValue().toString());
					    }
					 } catch(Exception e){
					    System.out.println(e);
					    e.printStackTrace();
					 }
		  }
}
