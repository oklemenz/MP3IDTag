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
public class MyItemSearch extends ItemSearchRest {

    public MyItemSearch(String subscriptionID, String artist, String title) {
        super(false);
        queryParameterNames = new String [] { "Artist", "Title", "SearchIndex" };
        queryName = "ItemSearch";
        commonParameters = new Hashtable();
        commonParameters.put("SubscriptionId", subscriptionID);
        commonParameters.put("AssociateTag", "");
        commonParameters.put("Validate", "");
        queryParameters = new Hashtable();
        queryParameters.put("Artist", artist);
        queryParameters.put("Title", title);
        queryParameters.put("SearchIndex", "Music");
    }    
}

