package com.sabadell.restAuthTreeNode.gemalto.service;

import com.google.inject.Inject;
import com.sabadell.restAuthTreeNode.gemalto.feign.config.GemaltoClientRepository;
import com.sabadell.restAuthTreeNode.gemalto.request.AuthenticationRequest;
import com.sabadell.restAuthTreeNode.gemalto.response.AuthenticationResponse;

public class GemaltoAuthenticationServiceImpl implements GemaltoAuthenticationService{

	@Inject
	GemaltoClientRepository gemaltoClientRepository;

	@Override
	public AuthenticationResponse gemaltoAuthenticateOTP(AuthenticationRequest request) {
		// TODO Auto-generated method stub
		return this.gemaltoAuthenticateOTP(request);
	}
	

}
