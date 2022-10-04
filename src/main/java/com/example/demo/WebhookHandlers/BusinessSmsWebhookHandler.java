package com.example.demo.WebhookHandlers;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entities.BusinessCopy;
import com.example.demo.Entities.BusinessRequest;
import com.example.demo.Repo.BusinessRequestRepo;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@RestController
public class BusinessSmsWebhookHandler {

	@Autowired
	private BusinessRequestRepo requestRepo;

	public static final String accountSid = "ACb80a5699bbcf32c554a17698071dc8c1";
	public static final String authToken = "5da29f9724c483857a32e1cbecbff52e";
	public static final String twilioNumber = "+19134122893";
	public static final String fordNumber = "+14048587064";

	@RequestMapping(value = "/addBusinessRequest", method = RequestMethod.POST)
	public void addRequest(@RequestParam("Name") String name, @RequestParam("Type") String types,
			@RequestParam("Number") String number, @RequestParam("Request") String request)
			throws NumberFormatException, NoSuchAlgorithmException {
		String details = request.split("1.")[1];
		String price = request.split("2.")[1];
		String timeTaken = request.split("1.")[1];
		String timeline = request.split("1.")[1];
		BusinessRequest businessRequest = new BusinessRequest(details, price, timeTaken, timeline,
				new BusinessCopy(types.trim(), number.trim(), name.trim()));
		requestRepo.save(businessRequest);

		Twilio.init(accountSid, authToken);
		Message message = Message.creator(new PhoneNumber(fordNumber), new PhoneNumber(twilioNumber),
				"There has been a new request from a business. Here are the details:" + "\nDetails: " + details
						+ "\nPrice: " + price + "\nTime: " + timeTaken + "\nTimeline: " + timeline)
				.create();

	}
}
