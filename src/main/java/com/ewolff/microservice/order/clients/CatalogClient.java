package com.ewolff.microservice.order.clients;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CatalogClient {

	private final Logger log = LoggerFactory.getLogger(CatalogClient.class);

	public static class ItemPagedResources extends PagedResources<Item> {

	}

	private RestTemplate restTemplate;
	private String catalogServiceHost;
	private long catalogServicePort;

	@Autowired
	// this will look for application properties first then use the default value after the colon
//	public CatalogClient(@Value("${catalog.service.host:catalog-service}") String catalogServiceHost,
//			@Value("${catalog.service.port:8080}") long catalogServicePort) {
	public CatalogClient(@Value("catalog-service") String catalogServiceHost, @Value("8080") long catalogServicePort) {
		super();
		this.restTemplate = getRestTemplate();
		this.catalogServiceHost = catalogServiceHost;
		this.catalogServicePort = catalogServicePort;
		
		System.out.println("======================================================");
		/*
		System.out.println("catalog.service.host:catalog value = ");
		System.out.println(@Value("${catalog.service.host:catalog}")) ;
		
		System.out.println("catalog-service.service.host value = ");
		System.out.println(@Value("${catalog-service.service.host}")) ;
		
		System.out.println("catalog-service.service.host:catalog value = ");
		System.out.println(@Value("${catalog-service.service.host:catalog}")) ;
		
		System.out.println("catalog-service.service.host:catalog-service value = ");
		System.out.println(@Value("${catalog-service.service.host:catalog-service}")) ;
		*/
		
		System.out.println("catalogServiceHost value = ");
		System.out.println(catalogServiceHost) ;
		
		System.out.println("======================================================");
		/*
		System.out.println("catalog.service.port:8080 value = ");
		System.out.println(@Value("${catalog.service.port:8080}")) ;
		
		System.out.println("catalog-service.service.port:8080 value = ");
		System.out.println(@Value("${catalog-service.service.port:8080}")) ;
		*/
		
		System.out.println("catalogServicePort value = ");
		System.out.println(catalogServicePort) ;	
		System.out.println("======================================================");
	}

	protected RestTemplate getRestTemplate() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModule(new Jackson2HalModule());

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON));
		converter.setObjectMapper(mapper);

		return new RestTemplate(Collections.<HttpMessageConverter<?>>singletonList(converter));
	}

	public double price(long itemId) {
		return getOne(itemId).getPrice();
	}

	public Collection<Item> findAll() {
		PagedResources<Item> pagedResources = restTemplate.getForObject(catalogURL(), ItemPagedResources.class);
		return pagedResources.getContent();
	}

	private String catalogURL() {
		String url = String.format("http://%s:%s/catalog/", catalogServiceHost, catalogServicePort);
		log.trace("Catalog: URL {} ", url);
		return url;
	}

	public Item getOne(long itemId) {
		return restTemplate.getForObject(catalogURL() + itemId, Item.class);
	}
}
