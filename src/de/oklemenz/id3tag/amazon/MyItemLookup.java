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
public class MyItemLookup extends ItemLookupRest {

    public MyItemLookup(String subscriptionID, String itemID, String responseGroup) {
        super(false);
        queryParameterNames = new String [] { "ItemId", "ResponseGroup" };
        queryName = "ItemLookup";
        commonParameters = new Hashtable();
        commonParameters.put("SubscriptionId", subscriptionID);
        commonParameters.put("AssociateTag", "");
        commonParameters.put("Validate", "");
        queryParameters = new Hashtable();
        queryParameters.put("ItemId", itemID);
        queryParameters.put("ResponseGroup", responseGroup);
    }    
}

