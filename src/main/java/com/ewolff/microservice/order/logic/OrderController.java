package com.ewolff.microservice.order.logic;

import java.util.Collection;
import java.util.Random;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ewolff.microservice.order.clients.CatalogClient;
import com.ewolff.microservice.order.clients.Customer;
import com.ewolff.microservice.order.clients.CustomerClient;
import com.ewolff.microservice.order.clients.Item;

import java.util.Iterator;

@Controller
class OrderController {

	private OrderRepository orderRepository;

	private OrderService orderService;

	private CustomerClient customerClient;
	private CatalogClient catalogClient;
	private final Logger log = LoggerFactory.getLogger(OrderController.class);
	private static Random rand = new Random();


	// @Value("${app.usedummydata:true}")
	private boolean useDummyData = false;

	@Autowired
	private OrderController(OrderService orderService,
			OrderRepository orderRepository, CustomerClient customerClient,
			CatalogClient catalogClient) {
		super();
		this.orderRepository = orderRepository;
		this.customerClient = customerClient;
		this.catalogClient = catalogClient;
		this.orderService = orderService;

		log.trace("Dummy Data: {} ", Boolean.toString(useDummyData));
		System.out.println("Dummy Data: " + Boolean.toString(useDummyData));
	}

	@ModelAttribute("items")
	public Collection<Item> items() {
		if(useDummyData) {
			ArrayList <Item>items = new ArrayList<Item>();
			items.add(new Item(1, "Item 1", 100.00));
			return items;	
		}

		return catalogClient.findAll();
	}

	@ModelAttribute("customers")
	public Collection<Customer> customers() {
		if(useDummyData) {
			Collection<Customer> allCustomers = new ArrayList<Customer>();
			allCustomers.add(new Customer(1, "Andi", "Grabner", "agrabner@dynatrace.com", "freistaedterstrasse", "Linz"));
			return allCustomers;
		}

		Collection<Customer> allCustomers = customerClient.findAll();

		// ************************************************
		// N+1 Problem
		// Add additional lookups for each customer
		// this will cause additional SQL calls
		// ************************************************
		Iterator<Customer> itr = allCustomers.iterator();
		while (itr.hasNext()) {
			Customer cust = itr.next();
			long id = cust.getCustomerId();
			for(int i=1; i<=20; i++){
				customerClient.getOne(id);
			}
		}
		return allCustomers;
	}

	@RequestMapping("/")
	public ModelAndView orderList() {
		return new ModelAndView("orderlist", "orders",
				orderRepository.findAll());
	}

	@RequestMapping(value = "/form.html", method = RequestMethod.GET)
	public ModelAndView form() {
		return new ModelAndView("orderForm", "order", new Order());
	}

	@RequestMapping(value = "/line", method = RequestMethod.POST)
	public ModelAndView addLine(Order order) {
		// ************************************************
		// in 50% of the cases will return incorrect data
		// back resulting in a 500 error in the UI
		// ************************************************
		int n = rand.nextInt(100);
		System.out.println("Random Number: " + Integer.toString(n));
		if(n < 50) {
			log.trace("ADDING A NULL FOR ORDER!!");
			System.out.println("ADDING A NULL FOR ORDER!!");
			return new ModelAndView("orderForm", "order", null);
		}
		order.addLine(0, catalogClient.findAll().iterator().next().getItemId());
		return new ModelAndView("orderForm", "order", order);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ModelAndView get(@PathVariable("id") long id) {
		return new ModelAndView("order", "order", orderRepository.findById(id).get());
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ModelAndView post(Order order) {
		order = orderService.order(order);
		return new ModelAndView("success");
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ModelAndView post(@PathVariable("id") long id) {
		orderRepository.deleteById(id);

		return new ModelAndView("success");
	}
}
