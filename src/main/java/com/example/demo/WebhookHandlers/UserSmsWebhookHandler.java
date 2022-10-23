package com.example.demo.WebhookHandlers;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.example.demo.Entities.AllUsers;
import com.example.demo.Entities.User;
import com.example.demo.Entities.UserCopy;
import com.example.demo.Entities.UserRequest;
import com.example.demo.Repo.AllUserRepo;
import com.example.demo.Repo.UserRepo;
import com.example.demo.Repo.UserRequestRepo;

@RestController
public class UserSmsWebhookHandler {

	@Autowired
	private UserRepo repo;

	@Autowired
	private AllUserRepo allRepo;

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
		AllUsers allUser = new AllUsers(body.trim(), name.trim(), number.trim());
		Table table = dynamo.getTable("Users");
		GetItemSpec item = new GetItemSpec().withPrimaryKey("userId", user.getUserId());
		Item t = table.getItem(item);
		if (!Objects.nonNull(t)) {// makes sure no duplicate values, if not included then we make a new one
			repo.save(user);
			allRepo.save(allUser);
		} else {// if already included, then we update the previous one with the new one
			repo.update(user.getUserId(), user);
			allRepo.update(allUser.getUserId(), allUser);
		}

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
		String[] arr = request.split("\\."); // 1.P 2.j 3.fas -> [1],[P 2], [j 3], [fas]
		String details = arr[1].substring(0, arr[1].length() - 1).trim();
		String price = arr[2].substring(0, arr[2].length() - 1).trim();
		String timeline = arr[3].trim();

		UserRequest userRequest = new UserRequest(new UserCopy(type.trim(), name.trim(), number.trim()), details, price,
				timeline);
		requestRepo.save(userRequest);// saves value to dynamodb database

	}

	@RequestMapping(value = "/deleteUser")
	public void deleteUser(@RequestParam("Name") String name, @RequestParam("Type") String type,
			@RequestParam("Number") String number) throws NoSuchAlgorithmException {
		User user = new User(type, name, number);
		Table table = dynamo.getTable("Users");
		table.deleteItem("userId", user.getUserId());// deletes item
	}
}
