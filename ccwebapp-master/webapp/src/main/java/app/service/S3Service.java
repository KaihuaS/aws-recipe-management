package app.service;

import app.model.Image;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class S3Service {
    @Value("${cloud.aws.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3client;

    public Image uploadFileTos3bucket(String fileName, File file) {
        try {
            long startTime = System.currentTimeMillis();

            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
            s3client.putObject(request);
            ObjectMetadata objectMetadata = s3client.getObjectMetadata(bucketName, fileName);
            String url = s3client.getUrl(bucketName, fileName).toExternalForm();

            long endTime = System.currentTimeMillis();
            MyStatsDClient.getStatsDClient().recordExecutionTime("upload file to s3", endTime-startTime);
            return new Image(url, objectMetadata.getETag(), objectMetadata.getContentLength());
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteFileFromS3Bucket(String url) {
        try {
            long startTime = System.currentTimeMillis();

            AmazonS3URI as3uri = new AmazonS3URI(url);
            s3client.deleteObject(new DeleteObjectRequest(as3uri.getBucket(), as3uri.getKey()));

            long endTime = System.currentTimeMillis();
            MyStatsDClient.getStatsDClient().recordExecutionTime("delete file in s3", endTime-startTime);
        } catch (AmazonServiceException ex) {
            ex.printStackTrace();
        }
    }
}
