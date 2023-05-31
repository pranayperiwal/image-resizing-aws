import boto3
from PIL import Image
import io

# Initialize S3 and SQS clients
s3 = boto3.resource('s3')
sqs = boto3.client('sqs')

# Define the queue URLs and bucket names
inbox_queue_url = 'https://ap-northeast-1.queue.amazonaws.com/642186390297/comp3358-assignment4-in'
outbox_queue_url = 'https://sqs.ap-northeast-1.amazonaws.com/642186390297/comp3358-assignment4-out'
bucket_name = 'comp3358-assignment4'

while True:
    # Receive messages from the inbox queue
    response = sqs.receive_message(
        QueueUrl=inbox_queue_url,
        MaxNumberOfMessages=10,
        WaitTimeSeconds=20
    )

    if 'Messages' not in response:
        continue

    for message in response['Messages']:
        # Get the original image from S3
        s3_object = s3.Object(bucket_name, message['Body'])
        image_content = s3_object.get()['Body'].read()
        image = Image.open(io.BytesIO(image_content))

        # Process the image
        baseWidth = 100
        wpercent = (baseWidth / float(image.size[0]))
        hsize = int((float(image.size[1]) * float(wpercent)))
        processed_image = image.resize((baseWidth, hsize), Image.Resampling.LANCZOS)
        # processed_image = image.rotate(90)

        # Upload the processed image to S3
        processed_image_key = message['Body'] + '_processed.jpeg'
        processed_image_content = io.BytesIO()
        processed_image.save(processed_image_content, format='JPEG')
        s3.Bucket(bucket_name).put_object(
            Key=processed_image_key,
            Body=processed_image_content.getvalue()
        )
        print("New image placed in s3 bucket")

        # Place the key to the processed image in the outbox queue
        sqs.send_message(
            QueueUrl=outbox_queue_url,
            MessageBody=processed_image_key)
        print("Key placed in outbox queue:", processed_image_key)

        # delete the key from the inbox queue
        sqs.delete_message(QueueUrl=inbox_queue_url,
                           ReceiptHandle=message['ReceiptHandle'])
