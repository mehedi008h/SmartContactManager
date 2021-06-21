package com.contact.demo.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contact.demo.helper.Message;
import com.contact.demo.model.Contact;
import com.contact.demo.model.User;
import com.contact.demo.repository.ContactRepository;
import com.contact.demo.repository.UserRepository;
import com.contact.demo.service.ContactService;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepo;
	
	@Autowired
	public ContactService service;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal)
	{
		String userName = principal.getName();
		System.out.println("USERNAME "+userName);
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER "+user);
		model.addAttribute("user", user);
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title", "User Dashbord");
		return "normal/user_dashboard";
	}
	
	//open add form controller
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact
	@PostMapping("/process-contact")
	public String precessContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, 
			Principal principal,HttpSession session)
	{
		try {
			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);
			//processing file upload
			if(file.isEmpty())
			{
				System.out.println("File is empty");
				contact.setImage("contact.jpg");
			}
			else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Uploaded");
			}
			contact.setUser(user);
			user.getContacts().add(contact);
			
			this.userRepository.save(user);
			System.out.println("Data "+contact);
			System.out.println("Added to database");
			//success message
			session.setAttribute("message", new Message("Your contact is added!! Add more..", "success"));
			
		} catch (Exception e) {
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			//error message
			session.setAttribute("message", new Message("Something went wrong!! Try again..", "danger"));
		}
		
		return "normal/add_contact_form";
	}

	//show contact
	//per page = 5[n]
	//current page = 0 [page]
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page,Model model,Principal principal)
	{
		model.addAttribute("title", "Show User Contacts");
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepo.findContactByUser(user.getId(),pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	//SHOWING CONTACT DETAILS
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId,Model model,Principal principal)
	{
		System.out.println("CID" +cId);
		Optional<Contact> contactOptional = this.contactRepo.findById(cId);
		Contact contact = contactOptional.get();
		//
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		return "normal/contact_details";
	}
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session,Principal principal)
	{
		Contact contact = this.contactRepo.findById(cId).get();
		//check
		contact.setUser(null);
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		session.setAttribute("message", new Message("Contact delete successfully..", "success"));
		return "redirect:/user/show-contacts/0";
	}
	//update contact handler
	@PostMapping("/update-contact/{cid}")
	public String updateFrom(@PathVariable("cid") Integer cid, Model model)
	{
		model.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepo.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}
	//update process
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") 
								MultipartFile file, Model model,HttpSession session,Principal principal)
	{
		try {
			//old contact details
			Contact oldContact = this.contactRepo.findById(contact.getcId()).get();
			if(!file.isEmpty())
			{
				//delete old photo
				File deleteFile = new ClassPathResource("static/image").getFile();
				File oldFile = new File(deleteFile,oldContact.getImage());
				oldFile.delete();
				//update photo
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				contact.setImage(oldContact.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepo.save(contact);
			session.setAttribute("message", new Message("Your contact is updated..", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	//profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	//search
	@RequestMapping("/search")
	public String search(Model model,@Param("keyword") String keyword)
	{
		List<Contact> listContact = service.listAll(keyword);
		model.addAttribute("listContact", listContact);
		model.addAttribute("keyword", keyword);
		return "normal/search";
	}
	
	//open setting handler
	@GetMapping("/settings")
	public String openSettings()
	{
		return "normal/settings";
	}
	
	//change password
	@PostMapping("/change-password")
	public String changepassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal,HttpSession session)
	{
		System.out.println("Old" +oldPassword);
		System.out.println("new" +newPassword);
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		if(this.passwordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			currentUser.setPassword(this.passwordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is changed", "success"));
		}
		else {
			session.setAttribute("message", new Message("Wrong!!Old Password", "danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
}
