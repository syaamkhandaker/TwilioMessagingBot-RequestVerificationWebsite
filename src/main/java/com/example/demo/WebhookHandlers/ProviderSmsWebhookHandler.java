package com.example.demo.WebhookHandlers;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.example.demo.Entities.AllProviders;
import com.example.demo.Entities.PastUserRequest;
import com.example.demo.Entities.Provider;
import com.example.demo.Entities.UserCopy;
import com.example.demo.Entities.UserRequest;
import com.example.demo.Repo.AllProviderRepo;
import com.example.demo.Repo.PastUserRequestRepo;
import com.example.demo.Repo.ProviderRepo;
import com.example.demo.Repo.UserRepo;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@RestController
public class ProviderSmsWebhookHandler {

	@Autowired
	private ProviderRepo repo;

	@Autowired
	private AllProviderRepo allRepo;

	@Autowired
	private PastUserRequestRepo pastRepo;

	@Autowired
	private UserRepo userRepo;

	private static final String endpoint = "dynamodb.us-east-2.amazonaws.com";
	private static final String accessKey = "AKIAUFHDV5GBNYWFDPO7";
	private static final String privateKey = "2vHUqJhPDduAt5QdKAdiLTY3AGlFUEpkDyuJezPH";
	public static final String accountSid = "ACb80a5699bbcf32c554a17698071dc8c1";
	public static final String authToken = "5da29f9724c483857a32e1cbecbff52e";
	public static final String myNumber = "+19134122893";

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
	@RequestMapping(value = "/addProvider", method = RequestMethod.POST)
	public void handleType(@RequestParam("Name") String name, @RequestParam("Body") String body,
			@RequestParam("Number") String number) throws NoSuchAlgorithmException {
		Provider provider = new Provider(body.trim(), name.trim(), number.trim());
		AllProviders allProvider = new AllProviders(body.trim(), name.trim(), number.trim());
		Table table = dynamo.getTable("Providers");
		GetItemSpec item = new GetItemSpec().withPrimaryKey("providerId", provider.getProviderId());
		Item t = table.getItem(item);
		if (!Objects.nonNull(t)) {
			repo.save(provider);
			allRepo.save(allProvider);
		} else {
			repo.update(provider.getProviderId(), provider);
			allRepo.update(allProvider.getProviderId(), allProvider);
		}
	}

	public String types(String val) {
		String add = "";
		switch (val) {
		case "1":
			add += "Homework Help";
			break;
		case "2":
			add += "Moving Help";
			break;
		case "3":
			add += "Cleaning Help";
			break;
		case "4":
			add += "Freelancing Help";
			break;
		case "5":
			add += "Pet Sitting Help";
			break;
		case "6":
			add += "Driving Help";
			break;
		}
		return add;
	}

	/*
	 * Method used to send each user with the same type the request. It searches
	 * through the DynamoDB database reading everyone's types. If it matches the
	 * original type then it sends them a message. In twilio, we check if the
	 * providers response matches the users name in the request.
	 * 
	 * UserRequest DB is supposed to only hold one value at a time. After each
	 * iteration it deletes the value from UserRequest and puts it into the
	 * PastUserRequest DB.
	 */
	@RequestMapping(value = "/providerRequests", method = RequestMethod.GET)
	public void sendRequests() throws NoSuchAlgorithmException {
		ScanRequest userScan = new ScanRequest().withTableName("UserRequest");
		ScanResult res = client.scan(userScan);
		UserCopy user = new UserCopy(res.getItems().get(0).get("User").getM().get("type").getS(),
				res.getItems().get(0).get("User").getM().get("userName").getS(),
				res.getItems().get(0).get("User").getM().get("phoneNumber").getS());
		UserRequest userRequest = new UserRequest(user, res.getItems().get(0).get("details").getS(),
				res.getItems().get(0).get("price").getS(), res.getItems().get(0).get("timeline").getS());
		PastUserRequest pastUserRequest = new PastUserRequest(user, userRequest.getDetails(), userRequest.getPrice(),
				userRequest.getTimeline());

		Twilio.init(accountSid, authToken);

		Table table = dynamo.getTable("UserRequest");
		table.deleteItem("requestId", userRequest.getRequestId());
		pastRepo.save(pastUserRequest);

		ScanRequest providerScan = new ScanRequest().withTableName("Providers");
		ScanResult providers = client.scan(providerScan);

		for (Map<String, AttributeValue> dbValues : providers.getItems()) {
			String test = dbValues.get("type").getN() == null ? dbValues.get("type").getS()
					: dbValues.get("type").getN();
			if (test.contains("" + user.getType())
					&& !dbValues.get("phoneNumber").getS().equals(user.getPhoneNumber())) {
				String text = user.userName + " needs " + types(user.getType()) + ".\nDetails: "
						+ userRequest.getDetails() + "\nPrice: " + userRequest.getPrice() + "\nTimeline: "
						+ userRequest.getTimeline();
				String number = "" + dbValues.get("phoneNumber").getS();
				Message message = Message.creator(new PhoneNumber(number), new PhoneNumber(myNumber), text).create();
			}
		}
	}

	/*
	 * Checks who is approved for their request. If the name the provider replies
	 * with is in the DB, then test becomes true and it automatically sends them a
	 * message saying their offer has been sent to the creator. If it isn't in the
	 * DB, then it sends a 500 error, which makes the HTTP Request fail and send a
	 * message back saying that they did'nt properly put the name in. Makes the user
	 * retry to put the correct name in.
	 * 
	 * @param body is the name the provider puts in response to each request they
	 * get
	 * 
	 * @param providerName is the provider's name, so we can send it to the user who
	 * asked for the requests
	 * 
	 * @param providerPhoneNumber is the provider's phoneNumber, so the user who
	 * made the request can message them.
	 */
	@RequestMapping(value = "/handleIncomingMessage", method = RequestMethod.GET)
	public void handleIncomingMessage(@RequestParam("Body") String body,
			@RequestParam("ProviderName") String providerName,
			@RequestParam("ProviderNumber") String providerPhoneNumber) throws NoSuchAlgorithmException {
		String userPhoneNumber = "";
		ScanRequest userScan = new ScanRequest().withTableName("Users");
		ScanResult users = client.scan(userScan);
		boolean test = false;
		end: for (Map<String, AttributeValue> dbValues : users.getItems()) {// whole for loop just looks through the
																			// entire DynamoDB Users DB to check if the
																			// user's name is in there
			if (dbValues.get("userName").getS().trim().equals(body.trim())) {
				userPhoneNumber = dbValues.get("phoneNumber").getS();
				test = true;
				break end;
			}
		}
		if (test) {
			Twilio.init(accountSid, authToken);
			Message message = Message.creator(new PhoneNumber(providerPhoneNumber), new PhoneNumber(myNumber),
					"Your offer has been sent to " + body + ".\nThey will message you if they choose to accept.")
					.create();
			String provName = providerName.split(" ").length > 1 ? providerName.split(" ")[0] : providerName;
			Message m = Message.creator(new PhoneNumber(userPhoneNumber), new PhoneNumber(myNumber),
					provName + " wants to fulfill your gig. Their number is " + providerPhoneNumber
							+ "\nSend them a message to begin the gig.")
					.create();

			return;
		}
		throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "");
	}

	/*
	 * After each provider iteration, I delete them from the DB to make sure that
	 * they can't receive requests once they accept one request. They have to go
	 * through the entire same flow again. This ensures that users can't randomly
	 * receive requests when they aren't expecting to receive requests. Twilio won't
	 * properly be able to read their requests either if they are in a totally
	 * different section
	 * 
	 * @param name the provider's name
	 * 
	 * @param type the provider's type
	 * 
	 * @param number the provider's number
	 */
	@RequestMapping(value = "/deleteProviders")
	public void deleteProviders(@RequestParam("Name") String name, @RequestParam("Type") String type,
			@RequestParam("Number") String number) throws NoSuchAlgorithmException {
		Provider provider = new Provider(type, name, number);
		Table table = dynamo.getTable("Providers");
		table.deleteItem("providerId", provider.getProviderId());// deletes item
	}
}
