package com.task.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {

    private String description;
    private String filename;
    
    public String getDescription() {
    	return description;
    }
    public void setDescription(String description) {
    	this.description=description;
    }
    public String getFilename() {
    	return filename;
    }
    public void setFilename(String filename) {
    	this.filename=filename;
    }
    
}
