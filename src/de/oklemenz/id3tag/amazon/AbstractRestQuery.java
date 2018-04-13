package de.oklemenz.id3tag.amazon;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

/**
 * Abstract class that implements some of the methods in RestQuery.
 * This class provides functionality that is common to all Rest Queries
 */

public abstract class AbstractRestQuery implements RestQuery {
	private String[] commonParameterNames = {"SubscriptionId", "AssociateTag",
											 "Validate"};
	private String host="webservices.amazon.com";
	private String path="/onca/xml?";
	protected String[] queryParameterNames;
	protected String queryName;
	protected Hashtable commonParameters;
	protected Hashtable queryParameters;
	protected String response;
	protected boolean batched;
	private DataInput dataInput;

    private static SignedRequestsHelper helper;
	
    
    private static void initHelper() {
        if (helper == null) {
            try {
                helper = SignedRequestsHelper.getInstance(GetAmazonImages.endPoint, GetAmazonImages.accessKey, GetAmazonImages.secretKey);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
    
	/**
	 * Creates the widget for this query
	 * @param listener The listener object for this query
	 * @param mode The mode that should be passed to the DataInput constructor
	 * 
	 * @return The widget for this query
	 */
	public Component createWidget(Query.Listener listener, String mode) {
		this.dataInput = new DataInput(this.commonParameters, this.queryParameters,
									   this.commonParameterNames, this.queryParameterNames,	
									   listener, this, mode);
		return this.dataInput;
	}				
	
	/**
	 * Get this query's name
	 * @return This query's name
	 */
	public String getQueryName() {
		return this.queryName;							 
	}
	
	/**
	 * Get the Operation performed by this query. 
	 * This is the same as the name of the query, except for the Multi-Operation query
	 * @return
	 */
	protected String getOperation() {
		return this.queryName;
	}
	
	/**
	 * Get the section of the url that is common to all queries.
	 * That is, SubscriptionId, AssociateTag and Validate
	 * @return The section of the url that is common to all queries.
	 */
	public String getCommonURL() {
		String url = "";
		for(int i = 0; i < this.commonParameterNames.length; i++) {
			String value = (String)this.commonParameters.get(this.commonParameterNames[i]);
			if(!value.equals("")) {
				url += "&" + this.commonParameterNames[i] + "=" + value;	
			}	
		}
		return url;		
	}

	/**
	 * Get the section of the url that is specific to this query
	 * @return The section of the url that is specific to this query
	 */
	public String getQueryURL() {
		String url = "";
		for(int i = 0; i < this.queryParameterNames.length; i++) {
			String value = (String)this.queryParameters.get(this.queryParameterNames[i]);
			if(!value.equals("")) {
				url += "&" + this.queryParameterNames[i] + "=" + value;	
			}	
		}		
		return url;		
	}

	/**
	 * Construct the url for this query.
	 * @return The url for this query
	 */
	public String constructURL() {
		return "http://" + this.host + this.path + "Service=AWSECommerceService&Operation=" + this.getOperation() 
				+ this.getCommonURL() + this.getQueryURL();
	}
	
	/**
	 * Populates the hashtable for common parameters
	 */
	protected void constructCommonParameters() {
		this.commonParameters = new Hashtable();
		for(int i = 0; i < this.commonParameterNames.length; i++) {
			this.commonParameters.put(this.commonParameterNames[i], "");
		}
	}

	/**
	 * Replaces all spaces with %20 since a url cannot have a space in it
	 * @param url The url in which to make the substitution
	 * @return The url after the substitution has been made
	 */
	private String convertWhiteSpace(String url) {
		return url.replaceAll(" ", "%20");	
	}

	/**
	 * Makes the webservice request
	 */
	public void issueRequest() throws IOException, SecurityException {
        // Begin: signing request
        initHelper();
	    URL url; // = new URL(this.convertWhiteSpace(this.constructURL()));
	    url = new URL(helper.sign(queryParameters, this.getOperation()));
        URLConnection connection = url.openConnection();
        // End: signing request
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        InputStream input = connection.getInputStream();
        byte[] buffer = new byte[1000];
        int amount=0;
        while(amount != -1)
        {
            result.write(buffer, 0, amount);
            amount = input.read(buffer);
        }
        this.response = result.toString();   
	}
	
	/**
	 * Returns the response received after making the webservice request
	 */
	public String getResponse() {
		return this.response;	
	}

	/**
	 * Calls the dialogFinished method in DataInput, thus forcing DataInput to collect the input
	 */
	public void forceDialogFinished() {
		this.dataInput.dialogFinished();
	}
}
