package com.example.simplewebsite.gigsurf;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.http.HttpStatus;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

//this class will add each of the requests to the website, not call the approve/decline request

@RestController
@RequestMapping(name = "test")
public class TestController {

	@Autowired
	RequestRepository repo;

	@Autowired
	UserRequestRepo userRequestRepo;

	Request req;

	private static final String endpoint = "";
	private static final String accessKey = "";
	private static final String privateKey = "";
	public static final String accountSid = "";
	public static final String authToken = "";
	public static final String myNumber = "";
	public static final String fordNumber = "";

	private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
			.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, "us-east-2"))
			.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, privateKey))).build();
	private DynamoDB dynamo = new DynamoDB(client);

	@RequestMapping(value = "/")
	public ModelAndView home() {
		ModelAndView mv = new ModelAndView("home.html");
		mv.addObject("request", repo.findAll());
		return mv;
	}

	@RequestMapping(value = "/approve/{id}")
	public ModelAndView approve(@PathVariable("id") String id) throws NoSuchAlgorithmException {
		ModelAndView mv = new ModelAndView("home.html");
		if (!repo.findAll().isEmpty() && repo.findById(Integer.parseInt(id)).isPresent()) {
			repo.deleteById(Integer.parseInt(id));
			ScanRequest requestScan = new ScanRequest().withTableName("UserRequest");
			ScanResult request = client.scan(requestScan);
			String requestId = "";
			UserRequest user = new UserRequest(
					new UserCopy(this.req.getType().trim(), this.req.getName().trim(),
							this.req.getPhoneNumber().trim()),
					this.req.getRequest(), this.req.getPrice(), this.req.getTimeline(), "false");
			for (Map<String, AttributeValue> dbValues : request.getItems()) {
				if (!dbValues.get("requestId").getS().equals("DO NOT USE")
						&& dbValues.get("User").getM().get("userId").getS().equals(user.getUser().getUserId())
						&& dbValues.get("details").getS().equals(user.getDetails())
						&& dbValues.get("price").getS().equals(user.getPrice())
						&& dbValues.get("timeline").getS().equals(user.getTimeline())) {
					requestId = dbValues.get("requestId").getS();
					Table table = dynamo.getTable("UserRequest");
					AttributeUpdate update1 = new AttributeUpdate("approved").put("accept");
					List<AttributeUpdate> attrlist = new ArrayList<AttributeUpdate>();
					attrlist.add(update1);
					UpdateItemSpec updateItem = new UpdateItemSpec().withPrimaryKey("requestId", requestId)
							.withAttributeUpdate(attrlist).withReturnValues(ReturnValue.ALL_NEW);
					table.updateItem(updateItem);
					mv.addObject("request", repo.findAll());
					break;
				}
			}
		}
		return mv;
	}

	@RequestMapping(value = "/decline/{id}")
	public ModelAndView decline(@PathVariable("id") String id) throws NoSuchAlgorithmException {
		ModelAndView mv = new ModelAndView("home.html");
		if (!repo.findAll().isEmpty() && repo.findById(Integer.parseInt(id)).isPresent()) {
			repo.deleteById(Integer.parseInt(id));
			ScanRequest requestScan = new ScanRequest().withTableName("UserRequest");
			ScanResult request = client.scan(requestScan);
			String requestId = "";
			UserRequest user = new UserRequest(
					new UserCopy(this.req.getType().trim(), this.req.getName().trim(),
							this.req.getPhoneNumber().trim()),
					this.req.getRequest(), this.req.getPrice(), this.req.getTimeline(), "false");
			for (Map<String, AttributeValue> dbValues : request.getItems()) {
				if (!dbValues.get("requestId").getS().equals("DO NOT USE")
						&& dbValues.get("User").getM().get("userId").getS().equals(user.getUser().getUserId())
						&& dbValues.get("details").getS().equals(user.getDetails())
						&& dbValues.get("price").getS().equals(user.getPrice())
						&& dbValues.get("timeline").getS().equals(user.getTimeline())) {
					requestId = dbValues.get("requestId").getS();
					Table table = dynamo.getTable("UserRequest");
					AttributeUpdate update1 = new AttributeUpdate("approved").put("decline");
					List<AttributeUpdate> attrlist = new ArrayList<AttributeUpdate>();
					attrlist.add(update1);
					UpdateItemSpec updateItem = new UpdateItemSpec().withPrimaryKey("requestId", requestId)
							.withAttributeUpdate(attrlist).withReturnValues(ReturnValue.ALL_NEW);
					table.updateItem(updateItem);
					mv.addObject("request", repo.findAll());
					break;
				}
			}
		}
		return mv;
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String add(@RequestParam("Request") String req, @RequestParam("Name") String name,
			@RequestParam("PhoneNumber") String number, @RequestParam("Type") String type) {
		String[] arr = req.split(System.lineSeparator()); // 1.P 2.j 3.fas -> [1],[P 2], [j 3], [fas]
		String details = arr[0].substring(3).trim();
		String price = arr[1].substring(3).trim();
		String timeline = arr[2].substring(3).trim();
		
		Request request = new Request(name, number, details, price, type, timeline);
		this.req = request;
		repo.save(request);
		return "Item was saved";
	}

}
