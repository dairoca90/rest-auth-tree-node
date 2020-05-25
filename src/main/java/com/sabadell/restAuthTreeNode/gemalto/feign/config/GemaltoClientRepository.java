package com.sabadell.restAuthTreeNode.gemalto.feign.config;

import com.sabadell.restAuthTreeNode.gemalto.request.AuthenticationRequest;
import com.sabadell.restAuthTreeNode.gemalto.response.AuthenticationResponse;

import feign.Headers;
import feign.RequestLine;


public interface GemaltoClientRepository {
	
	@RequestLine("POST /saserver/sabadell/api/auth/")
	@Headers("Content-Type: text/xml")
	AuthenticationResponse authenticateOTP(AuthenticationRequest request);
}
