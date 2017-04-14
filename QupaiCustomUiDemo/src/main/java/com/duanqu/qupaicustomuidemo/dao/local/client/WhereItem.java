package com.duanqu.qupaicustomuidemo.dao.local.client;

public class WhereItem {
	
	public WhereItem(String colume, Object value) {
		this.condition = Conditions.EQ;
		this.colume = colume;
		this.value = value;
	}
	
	public WhereItem(Conditions condition, String colume, Object value) {
		this.condition = condition;
		this.colume = colume;
		this.value = value;
	}
	public Conditions condition;
	public String colume;
	public Object value;

}
