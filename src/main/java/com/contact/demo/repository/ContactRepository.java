package com.contact.demo.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.contact.demo.model.Contact;
import com.contact.demo.model.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	//pagination..
	
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactByUser(@Param("userId") int userId,Pageable perPage);
	
	//search
	@Query("SELECT p FROM Contact p WHERE p.name LIKE %?1%"
			+"OR p.email LIKE %?1%"
			+"OR p.phone LIKE %?1%")
	public List<Contact> findAll(String keyword);
}
