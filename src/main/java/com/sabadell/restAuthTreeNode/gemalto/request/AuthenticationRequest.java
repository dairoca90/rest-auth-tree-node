package com.sabadell.restAuthTreeNode.gemalto.request;
import lombok.Data;

@Data
public class AuthenticationRequest {

	String userID;
	Seed seed;
	String OTP;
	Boolean OpenSession;
	
	
}
