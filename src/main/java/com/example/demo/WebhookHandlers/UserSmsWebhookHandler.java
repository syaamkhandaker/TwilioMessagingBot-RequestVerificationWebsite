package com.example.demo.WebhookHandlers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.example.demo.Entities.User;
import com.example.demo.Entities.UserCopy;
import com.example.demo.Entities.UserRequest;
import com.example.demo.Repo.UserRepo;
import com.example.demo.Repo.UserRequestRepo;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@RestController
public class UserSmsWebhookHandler {

	@Autowired
	private UserRepo repo;

	@Autowired
	private UserRequestRepo requestRepo;

	private static final String endpoint = "dynamodb.us-east-2.amazonaws.com";
	private static final String accessKey = "AKIAUFHDV5GBNYWFDPO7";
	private static final String privateKey = "2vHUqJhPDduAt5QdKAdiLTY3AGlFUEpkDyuJezPH";
	public static final String accountSid = "ACb80a5699bbcf32c554a17698071dc8c1";
	public static final String authToken = "5da29f9724c483857a32e1cbecbff52e";
	public static final String myNumber = "+19134122893";
	public static final String fordNumber = "+14048587064";

	private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
			.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, "us-east-2"))
			.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, privateKey))).build();
	private DynamoDB dynamo = new DynamoDB(client);

	/*
	 * This method adds each of the Student users into the database. The database is
	 * formatted as following: userId | userName | type | phoneNumber
	 * ______________________________________
	 * 
	 * The userId is auto generated from the sha256 hash function that I implemented
	 * in. The userName and type are received from twilio.
	 * 
	 * @param name receives the name from the twilio name_request block
	 * 
	 * @param body receives the type that the user wants to request for
	 * 
	 * @param number receives the users phone number in case we want to message them
	 * in the future
	 */
	@RequestMapping(value = "/addUser", method = RequestMethod.POST)
	public void handleType(@RequestParam("Name") String name, @RequestParam("Body") String body,
			@RequestParam("Number") String number) throws NoSuchAlgorithmException {
		User user = new User(body.trim(), name.trim(), number.trim());

		Table table = dynamo.getTable("Users");
		GetItemSpec item = new GetItemSpec().withPrimaryKey("userId", user.getUserId());
		Item t = table.getItem(item);
		if (!Objects.nonNull(t))// makes sure no duplicate values, if not included then we make a new one
			repo.save(user);
		else// if already included, then we update the previous one with the new one
			repo.update(user.getUserId(), user);

	}

	/*
	 * Method adds value to database in the condition that Ford approves it. Still
	 * working on functionality of how to check if Ford does. UserRequest Database
	 * looks like the following:
	 * 
	 * userRequestId | details | price | timeTaken | timeline | User (essentially a
	 * JSON file with data)
	 * ______________________________________________________________ sampleData
	 * "Calc" "$15" "1 hour" "Today" {name:"Syaam",type:"1",number:"+17037084879"}
	 * 
	 * @param name receives the name from the twilio name_request block
	 * 
	 * @param type receives the type that the user wants to request for
	 * 
	 * @param number receives the users phone number in case we want to message them
	 * in the future
	 * 
	 * @param request takes in the specific request with its details for pricing,
	 * timeline, the time it would take, and the specific details
	 */
	@RequestMapping(value = "/userRequests", method = RequestMethod.POST)
	public void addUserRequest(@RequestParam("Name") String name, @RequestParam("Type") String type,
			@RequestParam("Number") String number, @RequestParam("Request") String request)
			throws NumberFormatException, NoSuchAlgorithmException {
		String details = request.split("1.")[1];
		String price = request.split("2.")[1];
		String timeTaken = request.split("3.")[1];
		String timeline = request.split("4.")[1];

		// ford approval beforehand and then add to db

		Twilio.init(accountSid, authToken);
		String text = "Hi Ford, someone else had made a request. Please read with Y or N if you would like to accept it or decline it."
				+ "\nDetails: " + details + "\nPrice: " + price + "\nTime: " + timeTaken + "\nTimeline: " + timeline;// sends
																														// message
																														// to
																														// ford
																														// to
																														// approve
																														// of
																														// request
		Message message = Message.creator(new PhoneNumber(fordNumber), new PhoneNumber(myNumber), text).create();

		UserRequest userRequest = new UserRequest(new UserCopy(type.trim(), name.trim(), number.trim()), details, price,
				timeTaken, timeline);
		requestRepo.save(userRequest);// saves value to dynamodb database

	}

	/*
	 * Currently working on how I can use this to approve ford's request
	 * 
	 * Current plan is to check if Ford resonds with Y or N if Y then it proceeds
	 * with Twilio if N then it tells the user that their request was denied
	 */
	@RequestMapping(value = "/approval", method = RequestMethod.POST)
	@ResponseBody
	public static void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String body = request.getParameter("Body");
		String message = "Message";
		if (body.equals("Y")) {
			// Say hi
			message = "Hi there!";
		} else if (body.equals("N")) {
			// Say goodbye
			message = "Goodbye!";
		}
	}
}
