package com.nearbuy.spectra.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nearbuy.spectra.model.enums.*;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.Arrays;

/**
 * Created by sudhir on 23/9/15.
 */
@Document(collection = "template")
public class Template extends BaseModel{

    @NotNull
    @Indexed(unique=true)
    private String key;

    @NotNull
    @TextIndexed
    private String content;

    @NotNull
    @Size(min=0, max=200)
    private String subject;
    
    @NotNull
    private String fromEmail;
    
    @NotNull
    private String fromName;

    private String[] bcc;

    @JsonIgnore
    private TemplateType type;

    @JsonIgnore
    private Application application;

    @JsonIgnore
    private Category category;
    
    @JsonIgnore
    private EmailProvider serviceProvider;
    
    @JsonIgnore
    private Priority priority;
    
    @JsonIgnore
    private Integer maxFrequency;
    
    public Template(String key, String subject, String content, String fromEmail, String fromName, String[] bcc) {
        this.key = key;
        this.subject = subject;
        this.content = content;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.bcc = bcc;
    }

    public Template() {

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String[] getBcc() {
        return bcc;
    }

    public void setBcc(String[] bcc) {
        this.bcc = bcc;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public EmailProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(EmailProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Integer getMaxFrequency() {
        return maxFrequency;
    }

    public void setMaxFrequency(Integer maxFrequency) {
        this.maxFrequency = maxFrequency;
    }

    public TemplateType getType() {
        return type;
    }

    public void setType(TemplateType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Template{" +
                "key='" + key + '\'' +
                ", content='" + content + '\'' +
                ", subject='" + subject + '\'' +
                ", fromEmail='" + fromEmail + '\'' +
                ", fromName='" + fromName + '\'' +
                ", bcc=" + Arrays.toString(bcc) +
                ", type=" + type +
                ", application=" + application +
                ", category=" + category +
                ", serviceProvider=" + serviceProvider +
                ", priority=" + priority +
                ", maxFrequency=" + maxFrequency +
                '}';
    }
}
