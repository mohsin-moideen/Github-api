package com.blueoptima.github.profile;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.blueoptima.github.api.BlueOptimaController;

/**
 * Service class to search for a user using first name, last name and location.
 * @author MohsinM
 *
 */
@Service
public class UserSearchService {

	private static final Logger LOGGER = Logger.getLogger(BlueOptimaController.class.getName());
	private static final String CLASS_NAME = "UserSearchService";
	private static final String clientId = "c40e6061684ae0243b7d";
	private static final String clientSecret = "2be3963ae62ba7cae53b95ab74be86e59358a2cc";

	/**
	 * Method to add Github public profile data to a profile
	 * @param profile
	 */
	@Async
	public CompletableFuture<Void> findUser(Profile profile) throws InterruptedException {
		final String METHOD_NAME = "findUser";
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		final String searchResourceUrl = "https://api.github.com/search/users";
		String query = getSearchQuery(profile);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(searchResourceUrl)
				.queryParam("client_id", clientId).queryParam("client_secret", clientSecret).queryParam("q", query)
				.queryParam("type", "Users");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()), String.class);
		if(response.getStatusCode() != HttpStatus.OK) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Could not find user with first name:"
					+ profile.getFirstName() + ", last name " + profile.getLastName());
			return  CompletableFuture.completedFuture(null);
		}
		parseSearchResponse(profile, response);
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Method to parse response from Github search service and add info to a profile
	 * 
	 * @param profile
	 * @param response
	 */
	private void parseSearchResponse(Profile profile, ResponseEntity<String> response) {
		final String METHOD_NAME = "parseSearchResponse";
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		try {
			JSONParser parser = new JSONParser();
			JSONObject responseObject = (JSONObject) parser.parse(response.getBody());
			JSONArray item = (JSONArray) responseObject.get("items");
			if(item.size() == 0) {
				LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Could not find user with first name:"
						+ profile.getFirstName() + ", last name " + profile.getLastName());
				return;
			}
			JSONObject profileData = (JSONObject) item.get(0);
			profile.setLogin((String) profileData.get("login"));
			profile.setId((Long) profileData.get("id"));
			profile.setAvatar_url((String) profileData.get("avatar_url"));
			profile.setProfile_url((String) profileData.get("url"));

		} catch(ParseException ex) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, ex.getMessage());
		}
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
	}

	/**
	 * Method to construct search query for Github search service
	 * 
	 * @param profile
	 * @return query
	 */
	private String getSearchQuery(Profile profile) {
		final String METHOD_NAME = "getSearchQuery";
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		String query = profile.getFirstName() + "+";
		if(profile.getLastName() != null) {
			query += profile.getLastName() + "+in:fullname";
		}
		if(profile.getLocation() != null) {
			query += "+location:" + profile.getLocation().replace(' ', '+');
		}
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		return query;
	}

}