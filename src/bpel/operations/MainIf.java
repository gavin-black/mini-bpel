package bpel.operations;
import bpel.Log;

public class MainIf extends ElseIf
{
		  // No difference from ElseIf except the type
		  public MainIf( String oname, int odepth, String comparison )
		  {
					 super(oname, odepth, comparison, "MainIf");
		  }
}
