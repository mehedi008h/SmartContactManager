package com.contact.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.contact.demo.model.Contact;
import com.contact.demo.repository.ContactRepository;

@Service
public class ContactService {
	
	@Autowired
	ContactRepository contactRepository;
	
	public List<Contact> listAll(String keyword)
	{
		if(keyword != null)
		{
			return contactRepository.findAll(keyword);
		}
		return contactRepository.findAll();
	}

}
