package com.duanqu.qupaicustomuidemo.dao.local.client;

import java.util.Stack;

public class WhereNode {
	
	public ConditionRelation relation = ConditionRelation.NONE;
	public WhereNode right;
	public WhereNode left;
	public WhereItem item;

	public static class WhereBuilder {

		public WhereNode build(){
			return build(ConditionRelation.AND);
		}

		public WhereNode build(ConditionRelation relation){
			if(clauses.isEmpty()){
				return new WhereNode();
			}
			while (clauses.size() > 1){
				if(relation == ConditionRelation.AND){
					and();
				}else{
					or();
				}
			}
			return clauses.pop();
		}

		public void clear(){
			clauses.clear();
		}

		private Stack<WhereNode> clauses = new Stack<>();

		public WhereBuilder and(){
			clauses.push(creatNode(ConditionRelation.AND, clauses.pop(), clauses.pop()));
			return this;
		}

		public WhereBuilder and(WhereNode node){
			clauses.push(creatNode(ConditionRelation.AND, clauses.pop(), node));
			return this;
		}

		public WhereBuilder and(WhereNode left, WhereNode right){
			clauses.push(creatNode(ConditionRelation.AND, left, right));
			return this;
		}

		public WhereBuilder or(WhereNode node){
			clauses.push(creatNode(ConditionRelation.OR, clauses.pop(), node));
			return this;
		}

		public WhereBuilder or(WhereNode left, WhereNode right){
			clauses.push(creatNode(ConditionRelation.OR, left, right));
			return this;
		}

		public WhereBuilder or(){
			clauses.push(creatNode(ConditionRelation.OR, clauses.pop(), clauses.pop()));
			return this;
		}

		public WhereBuilder not(){
			clauses.push(creatNode(ConditionRelation.NOT, clauses.pop(), clauses.pop()));
			return this;
		}

		public WhereBuilder eq(String colume, Object value){
			clauses.push(creatItemNode(Conditions.EQ, colume, value));
			return this;
		}

		public WhereBuilder ge(String colume, Object value){
			clauses.push(creatItemNode(Conditions.GE, colume, value));
			return this;
		}

		public WhereBuilder notIn(String colume, Object value){
			clauses.push(creatItemNode(Conditions.NOTIN, colume, value));
			return this;
		}

		public WhereBuilder gt(String colume, Object value){
			clauses.push(creatItemNode(Conditions.GT, colume, value));
			return this;
		}

		public WhereBuilder in(String colume, Object value){
			clauses.push(creatItemNode(Conditions.IN, colume, value));
			return this;
		}

		public WhereBuilder le(String colume, Object value){
			clauses.push(creatItemNode(Conditions.LE, colume, value));
			return this;
		}

		public WhereBuilder like(String colume, Object value){
			clauses.push(creatItemNode(Conditions.LIKE, colume, value));
			return this;
		}

		public WhereBuilder lt(String colume, Object value){
			clauses.push(creatItemNode(Conditions.LT, colume, value));
			return this;
		}

		public WhereBuilder ne(String colume, Object value){
			clauses.push(creatItemNode(Conditions.NE, colume, value));
			return this;
		}

		private WhereNode creatItemNode(Conditions condition, String colume, Object value){
			WhereNode where = new WhereNode();
			where.item = new WhereItem(condition, colume, value);
			return where;
		}

		private WhereNode creatNode(ConditionRelation relation, WhereNode right, WhereNode left){
			WhereNode where = new WhereNode();
			where.relation = relation;
			where.left = left;
			where.right = right;
			return where;
		}

	}

}
