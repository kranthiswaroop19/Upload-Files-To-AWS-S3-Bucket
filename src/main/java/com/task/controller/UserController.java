package com.task.controller;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.task.model.User;

import com.task.service.S3Service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.MediaType;

@Controller
public class UserController {
	
	@Autowired
	S3Service s3Service;
	
	private String bucketName="myprojectcoursesnotes";
	Regions regions= Regions.US_EAST_1;
	
	
	@GetMapping("/file-upload")
	public String fileUploadPage() {
		return "form";	
	}
	@PostMapping("/file-upload")
	public String saveFiles(Model model,
			@RequestParam("description") String description,
			@RequestParam("file") MultipartFile multipartFile) {
		
	
	
	String filename=multipartFile.getOriginalFilename();
	User user=new User();
	user.setFilename(filename);
	user.setDescription(description);
	try {
		s3Service.uploadToS3(multipartFile.getInputStream(), filename);
	} catch (AmazonServiceException e) {
		model.addAttribute("error","AWS Service Error");
		e.printStackTrace();
	} catch (SdkClientException e) {
		model.addAttribute("error","SDK Clent Error");
		e.printStackTrace();
	} catch (IOException e) {
		model.addAttribute("error","Error Uploading file");
		e.printStackTrace();
	}
	model.addAttribute("message","File Successfully Upload");
	return "form";

	}
	
	
	 // Generate pre-signed URL to display image
	@GetMapping("/view-image/{filename}")
	public String viewImage(@PathVariable String filename, Model model) {
	   // logger.info("Request to view image with filename: {}", filename);
	    try {
	        String imageUrl = s3Service.getPresignedUrl(filename); // No need to process filename
	        model.addAttribute("imageUrl", imageUrl);
	        model.addAttribute("filename", filename);
	    } catch (AmazonS3Exception e) {
	        model.addAttribute("error", "File not found in S3: " + filename);
	      //  logger.error("Error retrieving file", e);
	    }
	    return "viewImage";
	}


	@GetMapping("/download/{filename}")
	public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) {
	    try {
	        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
	        
	        // Retrieve file from S3
	        S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, filename));
	        S3ObjectInputStream inputStream = s3Object.getObjectContent();

	        // Convert InputStream to byte array
	        byte[] fileBytes = IOUtils.toByteArray(inputStream);

	        // Set response headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
	        headers.setContentDispositionFormData("attachment", filename);

	        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
	    } catch (AmazonS3Exception e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	    } catch (IOException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
    

	}
}
