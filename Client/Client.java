import java.io.File;
import java.util.UUID;

public class Client {

	public static void main(String[] args) {
		// take the image path from argument
		String imagePath = args[0];
		File file = new File(imagePath);

		printFileDetails(file);

		String key = String.valueOf(UUID.randomUUID());
		String processed_key = key + "_processed";

		S3Bucket s3 = new S3Bucket();

		// upload the file to s3
		if (s3.uploadFile(file, key)) {
			System.out.println("Image upload successful!\n");
		} else {
			System.out.println("Image upload failed!\n");
			return;
		}

		// upload file key to SQS
		SQS sqsIn = new SQS("https://ap-northeast-1.queue.amazonaws.com/642186390297/comp3358-assignment4-in");

		if (sqsIn.sendMessage(key)) {
			System.out.println("Key sent to 'in' queue successfully!\n");
		} else {
			System.out.println("Key failed to be sent!\n");
			return;
		}

		// receive message with the key
		SQS sqsOut = new SQS("https://sqs.ap-northeast-1.amazonaws.com/642186390297/comp3358-assignment4-out");

		if (sqsOut.receiveMessages(processed_key)) {
			System.out.println("Key received from 'out' queue successfully!\n");
		} else {
			System.out.println("Key not received from 'out' queue!\n");
			return;
		}

		// get the new image from the bucket
		if (s3.downloadFile(processed_key, file.getPath(), file.getName())) {
			System.out.println("File downloaded successfully!\n");
		} else {
			System.out.println("File not downloaded successfully!\n");
			return;
		}
	}

	public static void printFileDetails(File file) {
		System.out.println();
		System.out.println("File name is: " + file.getName());
		System.out.println("The path to the file is: " + file.getPath());
		System.out.println("The size of the file is " + file.length() + " bytes.");
		System.out.println();
	}
}