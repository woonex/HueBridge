package com.mammoth.hueBridge;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@Validated
public class HueController {
	private static final String hueAppKey = "yvGtO369LGN3rc1nsF6ymtR7tUJmr6L2kb3pEJcj";
	private static final String hueBridgeIp = "192.168.0.125";
	
	private static final Map<String, String> lightIds = Map.of(
			"Nightstand".toUpperCase(), "2dd316e5-27e1-4bea-b50d-d5cb4a467137", 
			"StainedGlass".toUpperCase(), "720ca1b3-b241-4fcb-9711-881790d99f5c"
			);
	
	
	@PostMapping("/wakeup")
	public ResponseEntity<String> activateWakeup(@RequestBody LightRequest lightRequest) {
		String responseBody = "";
		
		int fadeMins = lightRequest.getFadeMins();
		boolean allSuccess = true;
		try {
			
			for (String lightName : lightRequest.getLights()) {
				String lightId = lightIds.get(lightName.toUpperCase());
				boolean success = activateLight(fadeMins, lightId);
				allSuccess &= success;
				responseBody += "Light " + lightName + " was " + (success ? "" : "NOT ") + "activated\n";
			}
//			responseBody = "Light successfully activated";
		} catch (Exception e) {
			e.printStackTrace();
			responseBody = e.getMessage();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
		}
		
		if (allSuccess) {
			return ResponseEntity.status(HttpStatus.OK).body(responseBody);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
		}
	}
	
	private boolean activateLight(Integer minutesWakeup, String lightId) throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		String url = "https://" + hueBridgeIp + "/clip/v2/resource/light/" + lightId;
		CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(SSLContexts.custom()
                        .loadTrustMaterial(new TrustSelfSignedStrategy())
                        .build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();

        // Create an HttpPut request
        HttpPut httpPut = new HttpPut(url);

        // Set the header
        httpPut.setHeader("hue-application-key", hueAppKey);
        httpPut.setHeader("Content-Type", "application/json");

        // Create the JSON body
        String jsonBody = "{\n" +
                "    \"on\":{\n" +
                "        \"on\":true\n" +
                "    },\n" +
                "    \"dynamics\": {\n" +
                "        \"duration\": " + minutesWakeup * 60000 + "\n" +
                "    },\n" +
                "    \"dimming\":{\n" +
                "        \"brightness\":100.0\n" +
                "    }\n" +
                "}";

        // Set the JSON body
        StringEntity entity = new StringEntity(jsonBody);
        httpPut.setEntity(entity);

        // Execute the request
        HttpResponse response = httpClient.execute(httpPut);

        // Get the response entity
        HttpEntity responseEntity = response.getEntity();

        // Print the response status code
        int responseCode = response.getStatusLine().getStatusCode();
        System.out.println("Response Status Code: " + responseCode);

        // Print the response content
        if (responseEntity != null) {
            String responseBody = EntityUtils.toString(responseEntity);
            System.out.println("Response Body: " + responseBody);
        }

        // Close the HttpClient
        httpClient.close();
        return responseCode == 200 ? true : false;
	}
}
