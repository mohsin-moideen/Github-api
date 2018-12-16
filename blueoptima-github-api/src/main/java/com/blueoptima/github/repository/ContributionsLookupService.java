package com.blueoptima.github.repository;

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
 * Service class to retrieve contributions of a user in a repository.
 * @author MohsinM
 *
 */
@Service
public class ContributionsLookupService {

	private static final Logger LOGGER = Logger.getLogger(BlueOptimaController.class.getName());
	private static final String CLASS_NAME = "ContributionsLookupService";

	private static final String clientId = "c40e6061684ae0243b7d";
	private static final String clientSecret = "2be3963ae62ba7cae53b95ab74be86e59358a2cc";

	/**
	 * Method to get number of contributions by a user in a given repository
	 * @param login
	 * @param repository
	 * @return
	 */
	@Async
	public CompletableFuture<Void> addContributions(Repository repository, String login) throws InterruptedException {
		final String METHOD_NAME = "addContributions";
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		String contributionsResourceUrl = "https://api.github.com/repos/" + login + "/" + repository.getName()	+ "/contributors";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contributionsResourceUrl)
				.queryParam("client_id", clientId).queryParam("client_secret", clientSecret)
				.queryParam("type", "Users");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()), String.class);
		if(response.getStatusCode() != HttpStatus.OK) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME,
					"Could not fetch contributions for user: " + login + " in repository: " + repository.getName());
			repository.setContributions(0L);
			return  CompletableFuture.completedFuture(null);
		}
		Long contributions = 0L;
		try {
			JSONParser parser = new JSONParser();
			JSONArray contributionsList = (JSONArray) parser.parse(response.getBody());
			for(int i = 0; i < contributionsList.size(); i++) {
				JSONObject contribution = (JSONObject) contributionsList.get(i);
				if(contribution.get("login").equals(login)) {
					contributions += (Long) contribution.get("contributions");
				}
			}
			repository.setContributions(contributions);
		} catch(ParseException ex) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, ex.getMessage());
		}
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		return  CompletableFuture.completedFuture(null);
	}
}