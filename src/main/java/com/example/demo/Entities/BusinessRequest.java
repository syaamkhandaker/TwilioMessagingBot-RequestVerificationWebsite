package com.example.demo.Entities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "BusinessRequest")
public class BusinessRequest {
	
	@DynamoDBHashKey
	private String businessId;
	
	@DynamoDBAttribute
	private String details;

	@DynamoDBAttribute
	private String price;

	@DynamoDBAttribute
	private String timeTaken;

	@DynamoDBAttribute
	private String timeline;
	
	@DynamoDBAttribute(attributeName = "Business")
	@Autowired
	private BusinessCopy business;

	public BusinessRequest(String details, String price, String timeTaken, String timeline, BusinessCopy business) throws NoSuchAlgorithmException {
		super();
		this.details = details;
		this.price = price;
		this.timeTaken = timeTaken;
		this.timeline = timeline;
		this.business = business;
		MessageDigest mes = MessageDigest.getInstance("SHA-256");
		String str = "" + business.phoneNumber;
		mes.update(str.getBytes());
		this.businessId = DatatypeConverter.printHexBinary(mes.digest());
	}
	
	public String getBusinessRequestId() {
		return this.businessId;
	}
	
	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(String timeTaken) {
		this.timeTaken = timeTaken;
	}

	public String getTimeline() {
		return timeline;
	}

	public void setTimeline(String timeline) {
		this.timeline = timeline;
	}
	
	
}
