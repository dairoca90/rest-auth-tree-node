/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017-2018 ForgeRock AS.
 */


package com.sabadell.restAuthTreeNode;

import com.google.inject.assistedinject.Assisted;
import com.sabadell.restAuthTreeNode.gemalto.service.GemaltoAuthenticationService;
import com.sun.identity.shared.debug.Debug;
import com.google.common.collect.ImmutableList;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.util.i18n.PreferredLocales;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider  = restAuthTreeNode.OutcomeProvider.class,
               configClass      = restAuthTreeNode.Config.class)
public class restAuthTreeNode implements Node {

	 private final static String TRUE_OUTCOME_ID = "true";
	    private final static String FALSE_OUTCOME_ID = "false";
	    private final static String DEBUG_FILE = "RestAuthNode";
	    protected Debug debug = Debug.getInstance(DEBUG_FILE);

    /**
     * Configuration for the node.
     */
    public interface Config {
    	 @Attribute(order = 100)
         default String URL() {
             return "https://server.domain:port/context";
         }

         @Attribute(order = 200)
         default String Body() {
             return "{\"message\": \"{{User}} has logged in\"}";
         }
         
         @Attribute(order= 300)
         List<String> Vars();
         
         
         @Attribute(order = 400)
         default String ResponseCode() {
             return "200";
         }

         @Attribute(order = 500)
         Map<String, String> Headers();
    }

    private final Config config;
    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @param realm The realm the node is in.
     * @throws NodeProcessException If the configuration was not valid.
     */
    
    
    
    @Inject
    public restAuthTreeNode(@Assisted Config config) throws NodeProcessException {
        this.config = config;
    }
    
    @Inject
    GemaltoAuthenticationService gemaltoAuthenticationService;

    @Override
    public Action process(TreeContext context) throws NodeProcessException {

        debug.message("[" + DEBUG_FILE + "]: Started");

        //Call helper function that sends HTTP request
        return sendRequest(context);
    }

private Action sendRequest(TreeContext context) {

    	
    	Map<String, String> requestVars = new HashMap<String,String>();
    	
    	
    	for(int i = 0; i<config.Vars().size();i++) {
    		requestVars.put(config.Vars().get(i), context.sharedState.get(config.Vars().get(i)).asString());
    	}
    	
    	debug.message("[" + DEBUG_FILE + "]: Variables Set :{}", requestVars);
    	
        try {

            URL url = new URL(config.URL());
            debug.message("[" + DEBUG_FILE + "]: Sending request to {}", url);

            //Build HTTP request
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            
            
            //Set the headers
            for (Map.Entry<String, String> entry : config.Headers().entrySet()) {
                conn.setRequestProperty(entry.getKey(),entry.getValue());
            }

            //Set body
            //Read the contents of the config body.  {{User}} is the only variable available
            String submittedBody=config.Body();
            debug.message("[" + DEBUG_FILE + "]: Request Config Body: {}", submittedBody);
            
            //Here we need to change the variables that comes in the body for the ones that comes in the callbacks
            //String userName = context.sharedState.get(SharedStateConstants.USERNAME).asString();
            
            
            
            
            for(int i = 0; i<config.Vars().size(); i++) {
            	
            	if (submittedBody.contains("{{" + config.Vars().get(i) + "}}")) submittedBody = submittedBody.replace("{{"+ config.Vars().get(i)+"}}", requestVars.get(config.Vars().get(i)));
            	
            }
            
            //if (submittedBody.contains("{{userName}}")) submittedBody = submittedBody.replace("{{userName}}", userName);
            

            debug.message("[" + DEBUG_FILE + "]: Replacing Request Body variables: {}", submittedBody);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
          
            
            debug.message("[" + DEBUG_FILE + "]: Sending Request ");
            writer.write(submittedBody);
            writer.close();
            
            
            InputStream ip = conn.getInputStream();
            BufferedReader br1 = new BufferedReader(new InputStreamReader(ip));
            StringBuilder response = new StringBuilder(); 
            String responseSingle = null; 
            while ((responseSingle = br1.readLine()) != null)  
            { 
                response.append(responseSingle); 
            } 
            String fullResponse = response.toString();

            //Check response is 200
            if (conn.getResponseCode() == Integer.valueOf(config.ResponseCode())) {
            	
            	
                debug.message("[" + DEBUG_FILE + "]: Response Code: {} ", conn.getResponseCode());
                debug.message("[" + DEBUG_FILE + "]: Response Body: {}", fullResponse);
                conn.disconnect();
                return goTo(true).build();
            }
            
			       

        } catch (MalformedURLException e) {
        	debug.message("[" + DEBUG_FILE + "]: {}",e.getMessage());
            
        } catch (IOException e) {
        	debug.message("[" + DEBUG_FILE + "]: {}",e.getMessage());   
        }

        return goTo(false).build();
    }

	
    private Action.ActionBuilder goTo(boolean outcome) {
        return Action.goTo(outcome ? TRUE_OUTCOME_ID : FALSE_OUTCOME_ID);
    }

    static final class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        private static final String BUNDLE = restAuthTreeNode.class.getName().replace(".", "/");

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());
            return ImmutableList.of(
                    new Outcome(TRUE_OUTCOME_ID, bundle.getString("true")),
                    new Outcome(FALSE_OUTCOME_ID, bundle.getString("false")));
        }
    }
}
