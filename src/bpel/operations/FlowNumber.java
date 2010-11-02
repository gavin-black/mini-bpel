package bpel.operations;
import bpel.Log;

/**
 * A flow runs a specified number of it's children in parallel
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class FlowNumber extends InternalNode
{
		  private int neededNumber;
		  private class FlowNumberChild implements Runnable
		  {
					 int id;
					 Operation childOperation;

					 public void run()
					 {
								childOperation.execute(id);
					 }

					 public FlowNumberChild( Operation child, int threadId)
					 {
								childOperation = child;
								id = threadId;
					 }

					 public FlowNumberChild() {}
		  }

		  public void action( int id )
		  {
					 String logFunction = "FlowNumber.action";
					 Log.logEntry( id, logFunction);
					 Thread[] t = new Thread[children.size()];
					 for(int i = 0; i < children.size(); i++)
					 {	
								t[i] = new Thread(new FlowNumberChild(children.elementAt(i),id));
								t[i].start();
					 }

					 // Step through and see how many are alive 
					 int numberLeft = neededNumber;
					 while( numberLeft > 0)
					 {
								numberLeft = neededNumber;
								for(int i = 0; i < children.size(); i ++)
								{
										  if( !t[i].isAlive() ) numberLeft --;
								}
					 }
					
					 for(int i = 0; i < children.size(); i ++)
					 {
					    if( t[i].isAlive() ) t[i].stop();
				 	 }

					 Log.logExit( id, logFunction);
		  }

		  public FlowNumber( String oname, int odepth, int number )
		  {
					 super(oname, "FlowNumber", odepth);
					 neededNumber = number;
		  }
}
