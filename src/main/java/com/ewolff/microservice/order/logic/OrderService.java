package com.ewolff.microservice.order.logic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ewolff.microservice.order.clients.CatalogClient;
import com.ewolff.microservice.order.clients.CustomerClient;

@Service
class OrderService {

	private OrderRepository orderRepository;
	private CustomerClient customerClient;
	private CatalogClient itemClient;

	@Autowired
	private OrderService(OrderRepository orderRepository,
			CustomerClient customerClient, CatalogClient itemClient) {
		super();
		this.orderRepository = orderRepository;
		this.customerClient = customerClient;
		this.itemClient = itemClient;
	}

	public Order order(Order order) {
		if (order.getNumberOfLines() == 0) {
			throw new IllegalArgumentException("No order lines!");
		}
		if (!customerClient.isValidCustomerId(order.getCustomerId())) {
			throw new IllegalArgumentException("Customer does not exist!");
		}
    		for(int i=1; i<=100000; i++){
      			fibonacci2(i);
   		}
		return orderRepository.save(order);
	}

	public double getPrice(long orderId) {
		return orderRepository.findById(orderId).get().totalPrice(itemClient);
	}

  	public int fibonacci(int number){
   	 	if(number == 1 || number == 2){
     	 		return 1;
    		}
   		return fibonacci(number-1) + fibonacci(number -2); //tail recursion
 	}

  	public int fibonacci2(int number){
    		if(number == 1 || number == 2) {
      			return 1;
    		}
    		int fibo1=1, fibo2=1, fibonacci=1;
    		for(int i= 3; i<= number; i++){
      			//Fibonacci number is sum of previous two Fibonacci number
      			fibonacci = fibo1 + fibo2;
      			fibo1 = fibo2;
      			fibo2 = fibonacci;
    		}
    		return fibonacci; //Fibonacci number
  	}
}
