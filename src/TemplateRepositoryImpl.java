package com.nearbuy.spectra.db.repository.impl;

import java.util.List;

import com.nearbuy.spectra.model.db.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.nearbuy.spectra.db.repository.TemplateCustomRepository;


public class TemplateRepositoryImpl implements TemplateCustomRepository {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<Template> search(String term) {
		Criteria ctr = new Criteria().orOperator(Criteria.where("emailTo").regex(term, "i"),  
                Criteria.where("ccTo").regex(term, "i"), 
                Criteria.where("bccTo").regex(term, "i"),
                Criteria.where("content").regex(term, "i"),
                Criteria.where("subject").regex(term, "i"),
                Criteria.where("fromEmail").regex(term, "i"),
                Criteria.where("fromName").regex(term, "i"),
                Criteria.where("key").regex(term, "i"),
                Criteria.where("zipFileName").regex(term, "i"));
		Query query = new Query(ctr);
		List<Template> templates =mongoTemplate.find(query,Template.class);
		return templates;
	}
	
}
