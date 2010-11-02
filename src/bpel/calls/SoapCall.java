package bpel.calls;

import bpel.Log;
import java.io.File;
import java.io.FileOutputStream;
import javax.activation.DataHandler;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.wsdl.WSDLConstants;

/**
 * Class for making an Axis2 based SOAP call
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class SoapCall
{
  /**
	* Call a SOAP service and perform an operation
	* 
	* @param uri The location to call
	* @param operation The SOAP function to use
	* @param outputVars The SOAP variables to retrieve, should be comma separated
	* @return A string containing the comma separated received variables
	*/
	public static String call( String uri, String operation, String responseVariables, String namespaceUri,  String namespacePrefix, String destination, String responseName, String attachmentName )
	{
			  String logFunction = "SoapCall.call";
			  Log.logEntry( -1, logFunction);
			  String ret = "";
			  EndpointReference targetEPR = new EndpointReference( uri );
			  Options options = new Options();

			  SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
			  SOAPEnvelope env = fac.getDefaultEnvelope();
			  OMNamespace omNs = fac.createOMNamespace(namespaceUri, namespacePrefix);

			  try {
						 options.setTo(targetEPR);
						 options.setAction(operation);
						 options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
						 options.setTimeOutInMilliSeconds(10000);
						 ServiceClient sender = new ServiceClient();
						 sender.setOptions(options);
						 OperationClient mepClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);
						 MessageContext mc = new MessageContext();

						 // Make sure the operation is valid and add it to the element
						 if(operation.indexOf(":") < 0 )
						 {
									System.err.println("Improper operation name expected urn:name!");
									System.exit(-1);
						 }
						 OMElement oElement = fac.createOMElement(operation.substring( operation.indexOf(":") + 1), omNs);
						 // Make sure the envelope contains all the appropriate response variables
						 String[] oVars = responseVariables.split(",");
						 String[] output = destination.split(",");
						 for(int i = 0; i < output.length; i++)
						 {
									OMElement nameEle = fac.createOMElement(oVars[i], omNs);
									nameEle.setText(output[i]);
									oElement.addChild(nameEle);
						 }
						 env.getBody().addChild(oElement);

						 // Finish setting up the envelope, execute, and get a response
						 mc.setEnvelope(env);
						 mepClient.addMessageContext(mc);
						 mepClient.execute(true);
						 MessageContext response = mepClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
						 SOAPBody body = response.getEnvelope().getBody();
		             OMElement element = body.getFirstChildWithName(new QName(namespaceUri, responseName));

						 // Grab each variable and put it in the return string
						 for(int i = 0; i < oVars.length; i++)
						 {
									if ( oVars[i].equals(attachmentName) )	
									{
											  OMElement valuesElement = element.getFirstChildWithName(new QName("http://service.sample/xsd",attachmentName));
											  String valuesID = valuesElement.getAttributeValue(new QName("href"));
											  valuesID = valuesID.substring(4);
											  DataHandler dataHandler = response.getAttachment(valuesID);
											  if (dataHandler!=null)
											  {
														 File valuesFile = new File("../bpelDownloads/response");
														 FileOutputStream outputStream = new FileOutputStream(valuesFile);
														 dataHandler.writeTo(outputStream);
														 outputStream.flush();
											  } else {
														 throw new Exception("Cannot find the data handler.");
											  }
									} else {
											  ret += element.getFirstChildWithName(new QName(namespaceUri,oVars[i])).getText();
											  if( i < oVars.length - 1) ret += ",";
									}
						 }

			  } catch (Exception e){
						 System.err.println(e);
						 e.printStackTrace();
			  }
			  Log.logExit( -1, logFunction, ret);
			  return ret;
	}
}
