package com.sabadell.restAuthTreeNode.gemalto.service;

import com.sabadell.restAuthTreeNode.gemalto.request.AuthenticationRequest;
import com.sabadell.restAuthTreeNode.gemalto.response.AuthenticationResponse;

public interface GemaltoAuthenticationService {

	AuthenticationResponse gemaltoAuthenticateOTP(AuthenticationRequest request);
	
	
}
