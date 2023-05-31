import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3Bucket {

	private AmazonS3 s3;
	private AWSCredentials credentials;
	private String bucketName;

	public S3Bucket() {
		credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (/Users/pranay/.aws/credentials), and is in valid format.", e);
		}

		s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion("ap-northeast-1").build();

		bucketName = "comp3358-assignment4";

	}

	public boolean uploadFile(File file, String key) {
		try {
			System.out.println("Uploading image to S3\n");
			s3.putObject(new PutObjectRequest(bucketName, key, file));
			return true;

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
			return false;
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
			return false;
		}
	}

	public boolean downloadFile(String key, String filePath, String fileName, HttpServletResponse response) {
		try {
			S3Object o = s3.getObject(bucketName, key);
			S3ObjectInputStream s3is = o.getObjectContent();
			OutputStream outputStream = response.getOutputStream();
			byte[] read_buf = new byte[1024];
			int read_len = 0;
			while ((read_len = s3is.read(read_buf)) > 0) {
				outputStream.write(read_buf, 0, read_len);
			}
			s3is.close();
			outputStream.close();

			return true;
		} catch (AmazonServiceException e) {
			System.err.println(e.getErrorMessage());
			e.printStackTrace();
			return false;

		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

	}
}