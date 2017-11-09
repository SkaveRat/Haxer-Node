# Haxer node

This is the remote code for the AWS render tool Haxler for blender.

This is a very WIP project, most parts were moved from Java to Kotlin without much code change.

## Usage

This is a jar that will be uploaded to EC2 instances and request SQS messages to get render jobs.

After receiving a message and rendering the frame, it uploads the file to S3