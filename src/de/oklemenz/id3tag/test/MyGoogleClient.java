package de.oklemenz.id3tag.test;

import gsearch.Client;
import gsearch.Response;
import gsearch.Result;
import gsearch.Response.ResponseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MyGoogleClient extends Client {

	public Map<String, String> addParams = new HashMap<String, String>();
	public static int PAGE_SIZE = 8;
	
	public MyGoogleClient()  {
		super();
		addParams.put("rsz", "" + PAGE_SIZE);
		addParams.put("as_filetype", "jpg");
		addParams.put("imgsz", "medium|large");
	}	
	
	public List<Result> searchImages(String query, int start) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("q", query);
		if (start != 0) {
			params.put("start", ""+start);
		}
		Response r = sendImageSearchRequest(params);		
		return r.getResponseData().getResults();
	}
	
	public Response sendSearchRequest(String url, Map<String, String> params)
	{
		params.putAll(addParams);
		Response r = super.sendSearchRequest(url, params);
		if (r == null) {
			r = new Response();
		}	
		if (r.getResponseData() == null) {
			r.setResponseData(new ResponseData());
		}
		if (r.getResponseData().getResults() == null) {
			r.getResponseData().setResults(new ArrayList<Result>());
		}
		return r;
	}	
}
