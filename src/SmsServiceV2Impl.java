package com.nearbuy.falcon.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.google.gson.Gson;
import com.nearbuy.falcon.exception.FalconSmsException;
import com.nearbuy.falcon.model.data.APIResponse;
import com.nearbuy.falcon.model.data.ErrorResponse;
import com.nearbuy.falcon.model.data.QueryRequestWrapper;
import com.nearbuy.falcon.model.data.SmsWrapper;
import com.nearbuy.falcon.model.db.SmsTemplate;
import com.nearbuy.falcon.model.dto.MessageDetailDTO;
import com.nearbuy.falcon.model.enums.SmsTemplates;
import com.nearbuy.falcon.service.SmsService;
import com.nearbuy.falcon.util.AppConstant;
import com.nearbuy.falcon.util.TemplateUtils;
import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.message.Message;
import com.plivo.helper.api.response.message.MessageResponse;
import com.plivo.helper.exception.PlivoException;

@Component
public class SmsServiceV2Impl implements SmsService {

    private static Logger LOGGER = LoggerFactory.getLogger(SmsServiceV2Impl.class);
    
    private @Value("${plivo.authId}") String authId;
	private @Value("${plivo.authToken}") String authToken;
	private @Value("${plivo.reportUrl}") String reportUrl;
	private @Value("${spectra.templateUrl}") String templateUrl;
	@Autowired
	private RestTemplate restTemplate;
	
	@Override
    public APIResponse sendSms(Long number, String text) {
        APIResponse res= null;
        try {
            MessageResponse msgResponse = sendSms(number.toString(), AppConstant.SENDER_ID, text);
            if (msgResponse != null && msgResponse.serverCode == 202) {
            	res= new APIResponse("sent");
            } else {
            	res= new APIResponse("failed");
            }
        } catch(Exception job) {
            res= new APIResponse("failed");
            LOGGER.error(job.getMessage());
        }
        return res;
    }

    @Override
    public APIResponse sendSms(SmsWrapper smsWrapper, APIResponse apiResponse){

    	//SmsTemplate dbTemplate = smsTemplateFromLocal(smsWrapper);
        SmsTemplate dbTemplate = smsTemplateFromTemplateManager(smsWrapper);
    	
    	if(dbTemplate == null)
    		return new APIResponse(new ErrorResponse("FSE001", "No such sms template registered in falcon."));
        
    	try {
            dbTemplate = TemplateUtils.getUpdatedTemplateWithActualValues(dbTemplate, smsWrapper);
            dbTemplate.setSenderId(AppConstant.SENDER_ID);
            MessageResponse msgResponse = sendSms(smsWrapper.getMobileNo(), dbTemplate.getSenderId(), dbTemplate.getContent());
            if (msgResponse != null && msgResponse.serverCode == 202) {
            	apiResponse= new APIResponse("sent");
            } else {
            	apiResponse= new APIResponse(new ErrorResponse("FSE002", msgResponse.error));
            }
        } catch (FalconSmsException e) {
            apiResponse= new APIResponse(new ErrorResponse(e.getCode(), e.getMessage()));
            LOGGER.error(e.getMessage());
        } catch (Exception e) {
            apiResponse= new APIResponse(new ErrorResponse("FSE003", e.getMessage()));
            LOGGER.error(e.getMessage());
        } 
        return apiResponse;
    }


    private SmsTemplate smsTemplateFromLocal(SmsWrapper smsWrapper) {
    	 SmsTemplates smsTemplates = null;
    	 for (SmsTemplates templates: SmsTemplates.values()) {
	         if (smsWrapper != null && smsWrapper.getTemplateId().equals(templates.getKey())) {
	             smsTemplates = templates;
	             break;
	         }
    	 }
	     if (smsTemplates == null)
	        return null;
	     SmsTemplate dbTemplate = new SmsTemplate(smsTemplates.getKey(), smsTemplates.getSenderId(), smsTemplates.getText(), smsTemplates.getUdhNumber());
	     return dbTemplate;
	}

	private MessageResponse sendSms(String receiver, String sender, String text) {
    	
    	RestAPI api = new RestAPI(authId, authToken, "v1");

        LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
        parameters.put("src", sender); // Sender's phone number with country code
        parameters.put("dst", AppConstant.INDIA_COUNTRY_CODE + receiver); // Receiver's phone number with country code
        parameters.put("text", text); // Your SMS text message
        parameters.put("url", reportUrl); // The URL to which with the status of the message is sent
        parameters.put("method", "GET"); // The method used to call the url

        MessageResponse msgResponse = null;
        try {
            // Send the message
            msgResponse = api.sendMessage(parameters);

            // Print the response
            LOGGER.info("Plivo Response for number " + receiver + ": " + msgResponse.toString());
            
        } catch (PlivoException e) {
        	LOGGER.error("Plivo Exception for number " + receiver + ": " + e.getLocalizedMessage());
        }
    	return msgResponse;
    }

	@Override
	public MessageDetailDTO getMessageDetails(String messageUUID) {
    	
    	RestAPI api = new RestAPI(authId, authToken, "v1");

        LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
        parameters.put("record_id", messageUUID);
        parameters.put("method", "GET");
        
        MessageDetailDTO messageDetail = null;

        try {
            Message msgResponse = api.getMessage(parameters);
            
            messageDetail = new MessageDetailDTO();
            messageDetail.setFromNumber(msgResponse.fromNumber);
            messageDetail.setStatus(msgResponse.messageState);
            messageDetail.setTime(msgResponse.messageTime);
            messageDetail.setToNumber(msgResponse.toNumber);
            messageDetail.setUuid(msgResponse.messageUUID);
            
            // Print the response
            LOGGER.info("Plivo Response: " + msgResponse.toString());
            
        } catch (PlivoException e) {
        	LOGGER.error("Plivo error : " + e.getLocalizedMessage());
        }
    	return messageDetail;
	}
	
	private SmsTemplate smsTemplateFromTemplateManager(
			SmsWrapper smsWrapper){
		
		QueryRequestWrapper queryRequestWrapper = new QueryRequestWrapper();
		queryRequestWrapper.setPlaceHolderMap(smsWrapper.getPlaceHolderMap());
		queryRequestWrapper.setTemplateId(smsWrapper.getTemplateId());
		String res = restTemplate.postForObject(templateUrl, queryRequestWrapper, String.class);
		APIResponse apiResponse = new Gson().fromJson(res, APIResponse.class);
		if (apiResponse != null && apiResponse.getSt().equals(0)) {
			return null;
		}else{
			ObjectMapper objectMapper = new ObjectMapper();						
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			SmsTemplate template = objectMapper.convertValue(apiResponse.getRs(), SmsTemplate.class);
			return template;
		}
	}
}
