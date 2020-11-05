package com.amazonaws.lambda.apigateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class APIDemoHandler implements RequestStreamHandler {

	private static final String DYNAMODB_TABLE_NAME = "Person";

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		JSONParser parser = new JSONParser();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		JSONObject responseJson = new JSONObject();

		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
		DynamoDB dynamoDB = new DynamoDB(client);
		context.getLogger().log("Outside of try\n");
		try {

			JSONObject event = (JSONObject) parser.parse(reader);

			if (event.get("body") != null) {
				context.getLogger().log("Inside if\n");
				Person person = new Person(event.get("body").toString());
				dynamoDB.getTable(DYNAMODB_TABLE_NAME).putItem(new PutItemSpec()
						.withItem(new Item().withNumber("id", person.getId()).withString("name", person.getName()))
				);

			}
			JSONObject responseBody = new JSONObject();
	        responseBody.put("message", "New item created");
	        JSONObject headerJson = new JSONObject();
	        headerJson.put("x-custom-header", "my custom header value");
	 
	        responseJson.put("statusCode", 200);
	        responseJson.put("headers", headerJson);
	        responseJson.put("body", responseBody.toString());

		} catch (ParseException e) {
			context.getLogger().log(e.getMessage());
		}
		OutputStreamWriter writer = new OutputStreamWriter(outputStream,"UTF-8");
		writer.write(responseJson.toString());
		writer.close();
	}

	public void handleGetByParam(InputStream inputStream, OutputStream outputStream, Context context)
			throws IOException {
		JSONParser parser = new JSONParser();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	    JSONObject responseJson = new JSONObject();
	    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
	    DynamoDB dynamoDb = new DynamoDB(client);
	    Item result = null;
	    
	    try {
	    	JSONObject event = (JSONObject) parser.parse(reader);
	        JSONObject responseBody = new JSONObject();
	        if (event.get("pathParameters")!=null) {
				JSONObject pps = (JSONObject)event.get("pathParameters");
				if (pps.get("id")!=null) {
					int id = Integer.parseInt(pps.get("id").toString());
					result = dynamoDb.getTable(DYNAMODB_TABLE_NAME).getItem("id",id);
					
				}
				
			}
	        
	        if (result !=null) {
				Person person = new Person(result.toJSON());
				person.setName("Cicd testing 01");
				responseBody.put("Person", person);
				responseJson.put("statusCode", 200);
				
			}else {
	            responseBody.put("message", "No item found");
	            responseJson.put("statusCode", 404);
	        }
	        
	        JSONObject headerJson = new JSONObject();
	        headerJson.put("x-custom-header", "my custom header value");
	 
	        responseJson.put("headers", headerJson);
	        responseJson.put("body", responseBody.toString());
	 
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	    
	    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
	    writer.write(responseJson.toString());
	    writer.close();
	}

}
// com.amazonaws.lambda.apigateway.APIDemoHandler::handleRequest