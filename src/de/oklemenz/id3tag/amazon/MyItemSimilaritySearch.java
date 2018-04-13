package de.oklemenz.id3tag.amazon;

import java.util.Hashtable;

/**
 * <p>Title: Advanced Programming Model</p>
 * <p>Description: Advanced Programming Model</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: OK</p>
 * @author Oliver Klemenz
 * @version 1.0
 */
public class MyItemSimilaritySearch extends ItemSearchRest {

    public MyItemSimilaritySearch(String subscriptionID, String textStream) {
        super(false);
        queryParameterNames = new String [] { "TextStream", "SearchIndex" };
        queryName = "ItemSearch";
        commonParameters = new Hashtable();
        commonParameters.put("SubscriptionId", subscriptionID);
        commonParameters.put("AssociateTag", "");
        commonParameters.put("Validate", "");
        queryParameters = new Hashtable();
        queryParameters.put("TextStream", textStream);
        queryParameters.put("SearchIndex", "Music");
    }    
    
    public MyItemSimilaritySearch(String subscriptionID, String artist, String textStream) {
        super(false);
        queryParameterNames = new String [] { "Artist", "TextStream", "SearchIndex" };
        queryName = "ItemSearch";
        commonParameters = new Hashtable();
        commonParameters.put("SubscriptionId", subscriptionID);
        commonParameters.put("AssociateTag", "");
        commonParameters.put("Validate", "");
        queryParameters = new Hashtable();
        queryParameters.put("Artist", artist);
        queryParameters.put("TextStream", textStream);
        queryParameters.put("SearchIndex", "Music");
    }    
}

