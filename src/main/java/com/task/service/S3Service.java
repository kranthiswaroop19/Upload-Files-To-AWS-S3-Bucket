package com.task.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class S3Service {
	private String bucketName="myprojectcoursesnotes";
	Regions regions= Regions.US_EAST_1;
	
	private AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(regions)
            .build();
	
	public void uploadToS3(InputStream inputStream,String filename) throws IOException ,AmazonServiceException,SdkClientException{
		/*AmazonS3 s3Client=AmazonS3ClientBuilder.standard()
				          .withRegion(regions).build();*/
		ObjectMetadata metadata=new ObjectMetadata();
		metadata.setContentType("image.jpeg");
		metadata.setContentLength(inputStream.available());
		PutObjectRequest request=new PutObjectRequest(bucketName, filename, inputStream, metadata);
		s3Client.putObject(request);
		
	}
	public String getPresignedUrl(String filename) {
	    AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(regions).build();

	    // Log the key being used
	    //logger.info("Fetching S3 object with key: {}", filename);

	    // Ensure the file exists before generating a URL
	    if (!s3Client.doesObjectExist(bucketName, filename)) {
	       // logger.error("File not found: {}", filename);
	        throw new AmazonS3Exception("The specified key does not exist: " + filename);
	    }

	    return s3Client.getUrl(bucketName, filename).toString();
	}


    

    // Download file from S3 and return it in the response
    public void downloadFile(String filename, HttpServletResponse response) {
        try {
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, filename));
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            InputStream inputStream = s3Object.getObjectContent();
            inputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException | AmazonServiceException e) {
            throw new RuntimeException("Error downloading file from S3", e);
        }
    }
	
}

