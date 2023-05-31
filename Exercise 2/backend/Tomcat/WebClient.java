import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;


@WebServlet("/process")
@MultipartConfig
public class WebClient extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		// Get the file name from the request
		// System.out.println(request.getContentType());
		// System.out.println(request.getContentLength());
		
		Part filePart = request.getPart("file");
		String fileNameFromReq = filePart.getSubmittedFileName();
		InputStream fileContent = filePart.getInputStream();

		System.out.println(fileNameFromReq);

		File saveFile = new File(fileNameFromReq);
		FileOutputStream outputStream = new FileOutputStream(saveFile);
		byte[] buffer = new byte[1024];
		int bytesRead = -1;
		System.out.println("Receiving data...");
		while ((bytesRead = fileContent.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		System.out.println("Data received.");
		outputStream.close();
		fileContent.close();

		// // start the process of processing image
		printFileDetails(saveFile);


		String key = String.valueOf(UUID.randomUUID());
		String processed_key = key + "_processed";

		S3Bucket s3 = new S3Bucket();

		// upload the file to s3
		if (s3.uploadFile(saveFile, key)) {
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
		response.setContentType("image/jpeg");
		response.setHeader("Content-Disposition", "attachment; filename=processed_image.jpeg");

		// File processedFile = s3.download(processed_key, saveFile.getPath(), saveFile.getName());

		if (s3.downloadFile(processed_key, saveFile.getPath(), saveFile.getName(), response)) {
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
