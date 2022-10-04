package com.example.demo.Repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.example.demo.Entities.BusinessRequest;

@Repository
public class BusinessRequestRepo {

	@Autowired
	private DynamoDBMapper dynamoDBMapper;

	public BusinessRequest save(BusinessRequest businessRequest) {
		dynamoDBMapper.save(businessRequest);
		return businessRequest;
	}

	public BusinessRequest getUserById(String businessRequestId) {
		return dynamoDBMapper.load(BusinessRequest.class, businessRequestId);
	}

	public String delete(String businessRequestId) {
		BusinessRequest business = dynamoDBMapper.load(BusinessRequest.class, businessRequestId);
		dynamoDBMapper.delete(business);
		return "Business deleted";
	}

	public String update(String businessRequestId, BusinessRequest businessRequest) {
		dynamoDBMapper.save(businessRequest, new DynamoDBSaveExpression().withExpectedEntry("userRequestId",
				new ExpectedAttributeValue(new AttributeValue().withS(businessRequestId))));
		return businessRequestId;
	}
}
