package com.sabadell.restAuthTreeNode.gemalto.feign.config;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.sabadell.restAuthTreeNode.gemalto.service.GemaltoAuthenticationService;
import com.sabadell.restAuthTreeNode.gemalto.service.GemaltoAuthenticationServiceImpl;

import feign.Feign;
import feign.jaxb.JAXBContextFactory;
import feign.jaxb.JAXBDecoder;
import feign.jaxb.JAXBEncoder;



public class GemaltoClientRepositoryConfig extends AbstractModule{

	@Override
    public void configure() {
		bind(GemaltoAuthenticationService.class).to(GemaltoAuthenticationServiceImpl.class);
	}
	
	
	
	@Provides
	GemaltoClientRepository gemaltoClientRepository() {
		return Feign.builder()
				.encoder(new JAXBEncoder(jaxbFactory()))
                .decoder(new JAXBDecoder(jaxbFactory()))
                .target(GemaltoClientRepository.class, "https://localhost:8081"); 
	}
	
	@Provides
	JAXBContextFactory jaxbFactory() {
		return new JAXBContextFactory.Builder()
			    .withMarshallerJAXBEncoding("UTF-8")
			    .build();
	}
	
	
	
}
