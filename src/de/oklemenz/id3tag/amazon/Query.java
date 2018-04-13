package de.oklemenz.id3tag.amazon;

import java.awt.Component;
import java.io.IOException;
import java.rmi.RemoteException;


/**
 * Interface to be implemented by all Queries.
 */
public interface Query {
	

	/**
	 * Issues this request
	 * @throws ServiceException Thrown by createAPDPortType() method in AbstractSoapQuery
	 * @throws RemoteException Thrown by cartAdd() method in AWSECommerceServicePortType
	 */
	public void issueRequest() throws IOException, RemoteException;
	
	/**
	 * Get this query's name
	 * @return This query's name
	 */
	public String getQueryName();
	
	/**
	 * Creates the widget for this query
	 * @param listener The listener object for this query
	 * @param mode The mode that should be passed to the DataInput constructor
	 * 
	 * @return The widget for this query
	 */
	public Component createWidget(Listener listener, String mode);

	/**
	 * Listener for this query.
	 */
	
	public interface Listener {
		public void finished(Query soapQuery);
	}
}
