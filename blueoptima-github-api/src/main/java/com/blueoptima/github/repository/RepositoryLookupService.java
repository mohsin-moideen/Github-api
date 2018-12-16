package com.blueoptima.github.repository;

import java.util.ArrayList;
import java.util.List;
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
import com.blueoptima.github.api.BlueOptimaServiceHelper;
import com.blueoptima.github.profile.Profile;

/**
 * Service class to retrieve repository for a user.
 * @author MohsinM
 *
 */
@Service
public class RepositoryLookupService {

    private static final Logger LOGGER = Logger.getLogger(BlueOptimaController.class.getName());
    private static final String CLASS_NAME = "RepositoryLookupService";
    private static final String clientId = "c40e6061684ae0243b7d";
	private static final String clientSecret = "2be3963ae62ba7cae53b95ab74be86e59358a2cc";

	/**
	 * Method to add repository information to a profile
	 * @param profile
	 */
    @Async
    public CompletableFuture<Void> addRepositories(Profile profile) throws InterruptedException {
		final String METHOD_NAME = "addRepositories";
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		BlueOptimaServiceHelper  blueOptimaServiceHelper = new BlueOptimaServiceHelper();
		String repositoriesResourceUrl = "https://api.github.com/users/" + profile.getLogin() + "/repos";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(repositoriesResourceUrl)
				.queryParam("client_id", clientId).queryParam("client_secret", clientSecret);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()), String.class);
		if(response.getStatusCode() != HttpStatus.OK) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Could not fetch repositories for user: " + profile.getLogin());
			return  CompletableFuture.completedFuture(null);
		}
		List<Repository> repositories = new ArrayList<>();
		try {
			JSONParser parser = new JSONParser();
			JSONArray repositoryArray = (JSONArray) parser.parse(response.getBody());
			ContributionsLookupService contributionsLookupService = new ContributionsLookupService();
			CompletableFuture [] threads = new CompletableFuture [repositoryArray.size()];
			for(int i = 0; i < repositoryArray.size(); i++) {
				JSONObject repo = (JSONObject) repositoryArray.get(i);
				Repository tempRepository = new Repository();
				tempRepository.setName((String) repo.get("name"));
				if(!blueOptimaServiceHelper.isCoreRateLimitExceeded()) {
					threads [i]= contributionsLookupService.addContributions(tempRepository, profile.getLogin());
					blueOptimaServiceHelper.coreRateLimit --;
				}
				repositories.add(tempRepository);
				LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "User: " + profile.getLogin() + ", Repository: " +
						tempRepository.getName() + ", Contributions: " + tempRepository.getContributions());
			}
			CompletableFuture.allOf(threads).join();
			profile.setRepositories(repositories);
		} catch (ParseException ex) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, ex.getMessage());
		}
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		return  CompletableFuture.completedFuture(null);
	}
}