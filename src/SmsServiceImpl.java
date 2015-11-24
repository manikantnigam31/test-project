package com.nearbuy.falcon.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.nearbuy.falcon.exception.FalconSmsException;
import com.nearbuy.falcon.model.data.APIResponse;
import com.nearbuy.falcon.model.data.ErrorResponse;
import com.nearbuy.falcon.model.data.SmsWrapper;
import com.nearbuy.falcon.model.db.SmsTemplate;
import com.nearbuy.falcon.model.enums.SmsTemplates;
import com.nearbuy.falcon.rest.client.RestRemoteCall;
import com.nearbuy.falcon.rest.exception.JobFailedException;
import com.nearbuy.falcon.util.AppConstant;
import com.nearbuy.falcon.util.TemplateUtils;
import com.nearbuy.falcon.util.ValueFirstResponse;

/**
 * Created by sudhir on 10/9/15.
 */
@Component
public class SmsServiceImpl { //implements SmsService {

    Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Autowired
    RestRemoteCall remoteCall;
    @Autowired
    RestTemplate restTemplate;

    //@Override
    public APIResponse sendSms(Long number, String text) {
        APIResponse res= null;
        try
        {
            String valueFirstUrl = AppConstant.getSmsUrl(number.toString(), text, AppConstant.UDH_NUMBER);
            logger.info("value first http request - " + valueFirstUrl);
            String response = remoteCall.executePlain(valueFirstUrl, restTemplate);
            logger.info("value first http response - " + response);

            if (ValueFirstResponse.SENT.getResponse().equals(response) || ValueFirstResponse.SENT_SPLIT.getResponse().equals(response)){
                res= new APIResponse("sent");
            }else{
                res= new APIResponse("failed");
            }
        }
        catch(JobFailedException job)
        {
            res= new APIResponse("failed");
            logger.error(job.getMessage());
        }
        return res;
    }

    //@Override
    public APIResponse sendSms(SmsWrapper smsWrapper, APIResponse apiResponse) {

        SmsTemplates smsTemplates = null;

        for (SmsTemplates templates: SmsTemplates.values()){
            if (smsWrapper != null && smsWrapper.getTemplateId().equals(templates.getKey())){
                smsTemplates = templates;
                break;
            }
        }
        if (smsTemplates == null)
           return new APIResponse(new ErrorResponse("FSE001", "No such email template registered in falcon."));

        try
        {
            SmsTemplate dbTemplate = new SmsTemplate(smsTemplates.getKey(), smsTemplates.getSenderId(), smsTemplates.getText(), smsTemplates.getUdhNumber());
            dbTemplate = TemplateUtils.getUpdatedTemplateWithActualValues(dbTemplate, smsWrapper);
            String valueFirstUrl = AppConstant.getSmsUrl(smsWrapper.getMobileNo(), dbTemplate.getContent(), dbTemplate.getUdhNumber());
            logger.info("value first http request - " + valueFirstUrl);
            String response = remoteCall.executePlain(valueFirstUrl, restTemplate);
            logger.info("value first http response - " + response);

            if (ValueFirstResponse.SENT.getResponse().equals(response) || ValueFirstResponse.SENT_SPLIT.getResponse().equals(response)){
                apiResponse= new APIResponse("sent");
            }else{
                apiResponse= new APIResponse(new ErrorResponse("FSE002", response));
            }
        }
        catch(JobFailedException job)
        {
            apiResponse= new APIResponse(new ErrorResponse("FSE003", job.getMessage()));
            logger.error(job.getMessage());
        } catch (FalconSmsException e) {
            apiResponse= new APIResponse(new ErrorResponse(e.getCode(), e.getMessage()));
            logger.error(e.getMessage());
        }
        return apiResponse;
    }
}
