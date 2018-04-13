package de.oklemenz.id3tag.amazon;

import java.util.Hashtable;

/**
 * Class that provides ItemLookup using REST.
 */
public class ItemLookupRest extends AbstractRestQuery {
	private String[] parameterNames = {"Condition", "DeliveryMethod", "IdType", "ISPUPostalCode",
									   "ItemId", "MerchantId", "OfferPage", "ResponseGroup", "ReviewPage",
									   "SearchIndex", "VariationPage"};
	
	private static int NUM_PARAMS = 11;
	
	/**
	 * Initializes member variables
	 * @param batched Boolean specifying whether the request will be batched or not. This affects the parameters created and displayed 
	 */
	public ItemLookupRest(boolean batched) {
		this.batched = batched;
		this.constructCommonParameters();
		if(this.batched) {
			String[] batchedParameterNames = new String[NUM_PARAMS * 3];
			String[] prefixes = {"ItemLookup.Shared.", "ItemLookup.1.",
								 "ItemLookup.2."};
			for(int i = 0; i < prefixes.length; i++) {
				for(int j = 0; j < this.parameterNames.length; j++) {
					if(!this.parameterNames[j].startsWith(prefixes[i])) {
						int index = j + (i * this.parameterNames.length);
						batchedParameterNames[index] = prefixes[i] + this.parameterNames[j];
					}
				}	
			}
			
			this.parameterNames = batchedParameterNames;
		}
		this.queryParameters = new Hashtable();
		for(int i = 0; i < this.parameterNames.length; i++) {
			this.queryParameters.put(this.parameterNames[i], "");
		}
		this.queryParameterNames = this.parameterNames;
		this.queryName = "ItemLookup";
	}
}
