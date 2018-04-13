package de.oklemenz.id3tag.amazon;

import java.util.Hashtable;

/**
 * Class that provides ItemSearch using REST.
 */
public class ItemSearchRest extends AbstractRestQuery {
										
	private String[] parameterNames = {"Actor", "Artist", "AudienceRating", "Author", "Brand", 
									   "BrowseNode", "City", "Composer", "Condition", "Conductor",
									   "Cuisine", "DeliveryMethod", "Director", 
									   "ISPUPostalCode", "ItemPage", "Keywords", "Manufacturer", 
									   "MaximumPrice", "MerchantId", "MinimumPrice", "MusicLabel",
									   "Neighborhood", "Orchestra", "Power", "Publisher", 
									   "ResponseGroup","SearchIndex", "Sort", "TextStream", 
									   "Title"};

	private static int NUM_PARAMS = 30;

	/**
	 * Initializes member variables
	 * @param batched Boolean specifying whether the request will be batched or not. This affects the parameters created and displayed 
	 */
	public ItemSearchRest(boolean batched) {
		this.batched = batched;
		this.constructCommonParameters();
		if(this.batched) {
			String[] batchedParameterNames = new String[NUM_PARAMS * 3];
			String[] prefixes = {"ItemSearch.Shared.", "ItemSearch.1.",
								 "ItemSearch.2."};
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
		this.queryName = "ItemSearch";
	}
}
