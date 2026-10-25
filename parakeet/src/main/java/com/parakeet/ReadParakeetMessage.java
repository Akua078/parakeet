package com.parakeet;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

public class ReadParakeetMessage {

    public static void main(String[] args) {

        // Connect to DynamoDB in Oregon
        try (DynamoDbClient client = DynamoDbClient.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(ProfileCredentialsProvider.create("parakeet-dev"))
                .build()) {

            // Identify the item we want
            GetItemRequest request = GetItemRequest.builder()
                    .tableName("parakeetMessages")
                    .key(java.util.Map.of(
                            "id", AttributeValue.builder()
                                    .s("00000001")
                                    .build()
                    ))
                    .build();

            // Retrieve the item from AWS
            var response = client.getItem(request);

            // Display its message
            if (response.hasItem()) {
                String message = response.item()
                        .get("message")
                        .s();

                System.out.println("Message from AWS: " + message);
            } else {
                System.out.println("No item found.");
            }
        }
    }
}
