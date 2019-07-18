package com.authentication.system.auth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class CredentialUserResponse {
	private ArrayList<LinkedHashMap<String, Object>> data;

	public ArrayList<LinkedHashMap<String, Object>> getData() {
		return data;
	}

	public void setData(Object object) {
		this.data = (ArrayList<LinkedHashMap<String, Object>>) object;
	}


	
}
