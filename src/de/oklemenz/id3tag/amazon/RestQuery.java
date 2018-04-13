package de.oklemenz.id3tag.amazon;

/**
 * Interface to be implemented by all Rest Queries.
 */
public interface RestQuery extends Query {
	
	/**
	 * Construct the url for this query.
	 * @return The url for this query
	 */
	public String constructURL();
	
	/**
	 * Returns the response received after making the webservice request
	 */
	public String getResponse();
	
	/**
	 * Get the section of the url that is specific to this query
	 * @return The section of the url that is specific to this query
	 */
	public String getQueryURL();
	
	/**
	 * Get the section of the url that is common to all queries.
	 * That is, SubscriptionId, AssociateTag and Validate
	 * @return The section of the url that is common to all queries.
	 */
	public String getCommonURL();
	
	/**
	 * Calls the dialogFinished method in DataInput, thus forcing DataInput to collect the input
	 */
	public void forceDialogFinished();
	
}
