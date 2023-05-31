
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SQS {
	private ProfileCredentialsProvider credentialsProvider;
	private AmazonSQS sqs;
	private String queueURL;

	public SQS(String queueURL) {
		this.queueURL = queueURL;
		;

		credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (/Users/pranay/.aws/credentials), and is in valid format.", e);
		}

		sqs = AmazonSQSClientBuilder.standard().withCredentials(credentialsProvider).withRegion(Regions.AP_NORTHEAST_1)
				.build();

	}

	public boolean sendMessage(String message) {
		try {

			// Send a message
			System.out.println("Sending a message to comp3358-assignment4-in queue.\n");
			sqs.sendMessage(new SendMessageRequest(queueURL, message));
			return true;

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon SQS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
			return false;
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with SQS, such as not "
					+ "being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
			return false;
		}
	}

	public boolean receiveMessages(String messageBody) {

		System.out.println("Receive messages");
		try {
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueURL).withWaitTimeSeconds(10)
					.withMaxNumberOfMessages(10);

			List<Message> sqsMessages = sqs.receiveMessage(receiveMessageRequest).getMessages();

			// check for the specific key only
			for (Message message : sqsMessages) {
				if (message.getBody().equals(messageBody)) {
					sqs.deleteMessage(queueURL, message.getReceiptHandle());
					return true;
				}
			}
			return false;
		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon SQS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
			return false;
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with SQS, such as not "
					+ "being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
			return false;
		}

	}

}
