package com.contact.demo.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;


@Service
public class EmailService {
	public boolean sendEmail(String subject,String message,String to)
	{
		boolean f = false;
		String form = "mehedi08r@gmail.com";
		String host = "smtp.gmail.com";
		
		Properties properties = System.getProperties();
		System.out.println("Properties "+properties);
		
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port","465");
		properties.put("mail.smtp.ssl.enable","true");
		properties.put("mail.smtp.auth","true");
		
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("mehedi08r@gmail.com", "mehedi-ria");
			}
			
		});
		session.setDebug(true);
		
		//step 2
		MimeMessage mime = new MimeMessage(session);
		try {
			mime.setFrom(form);
			mime.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			mime.setSubject(subject);
			//mime.setText(message);
			mime.setContent(message,"text/html");
			
			Transport.send(mime);
			System.out.println("Success....");
			f = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return f;
	}
}
