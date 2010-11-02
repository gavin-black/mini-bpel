package bpel.operations;
import bpel.Log;

/**
 * A flow runs all of it's children in parallel
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Flow extends InternalNode
{
		  private class FlowChild implements Runnable
		  {
					 int id;
					 Operation childOperation;

					 public void run()
					 {
								childOperation.execute(id);
					 }

					 public FlowChild( Operation child, int threadId)
					 {
								childOperation = child;
								id = threadId;
					 }

					 public FlowChild() {}
		  }

		  public void action( int id )
		  {
					 String logFunction = "Flow.action";
					 Log.logEntry( id, logFunction);
					 Thread[] t = new Thread[children.size()];
					 for(int i = 0; i < children.size(); i++)
					 {	
								t[i] = new Thread(new FlowChild(children.elementAt(i),id));
								t[i].start();
					 }

					 // Step through and see if any are still alive
					 boolean anyAlive = true;
					 while( anyAlive )
					 {
								anyAlive = false;
								for(int i = 0; i < children.size(); i ++)
								{
										  if( t[i].isAlive() ) anyAlive = true;
								}
					 }
					 Log.logExit( id, logFunction);
		  }

		  public Flow( String oname, int odepth )
		  {
					 super(oname, "Flow", odepth);
		  }
}
