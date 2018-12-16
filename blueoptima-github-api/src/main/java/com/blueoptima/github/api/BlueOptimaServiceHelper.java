package com.blueoptima.github.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.blueoptima.github.exceptions.RateLimitExceededException;
import com.blueoptima.github.profile.Profile;
import com.blueoptima.github.profile.UserSearchService;
import com.blueoptima.github.repository.Repository;
import com.blueoptima.github.repository.RepositoryLookupService;

/**
 * Helper class for controller to fetch profile information.
 * @author MohsinM
 *
 */
public class BlueOptimaServiceHelper {
	
	private final static Logger LOGGER = Logger.getLogger(BlueOptimaController.class.getName());
	private final static String CLASS_NAME = "BlueOptimaController";
	
	public static Long coreRateLimit = 0L;
	public static Long searchRateLimit = 0L;
	private static final String clientId = "c40e6061684ae0243b7d";
	private static final String clientSecret = "2be3963ae62ba7cae53b95ab74be86e59358a2cc";

	/**
	 * Method to add required profile information
	 * 
	 * @param profiles
	 */
	void addProfileInfo(List<Profile> profiles) {
		final String METHOD_NAME = "addProfileInfo";
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		UserSearchService userSearchService = new UserSearchService();
		CompletableFuture[] threads = new CompletableFuture[profiles.size()];
		int usersNotFound = 0;
		for(int i = 0; i < profiles.size(); i++) {
			if(!isCoreRateLimitExceeded()) {
				try {
					threads[i] = userSearchService.findUser(profiles.get(i));
					if (profiles.get(i).getLogin() == null) {
						usersNotFound++;
					}
				} catch(InterruptedException ex) {
					LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, ex.getMessage());
				}
			}
			searchRateLimit--;
		}
		CompletableFuture.allOf(threads).join();
		CompletableFuture<List<Repository>> [] repositoryServicethreads = new CompletableFuture[profiles.size() - usersNotFound];
		int threadCount = 0;
		RepositoryLookupService repositoryLookupService = new RepositoryLookupService();
		for(int i = 0; i < profiles.size(); i++) {
			if(!isCoreRateLimitExceeded()) {
				if(profiles.get(i).getLogin() != null) {
					try {
						threads[threadCount] = repositoryLookupService.addRepositories(profiles.get(i));
						threadCount++;
					} catch (InterruptedException ex) {
						LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, ex.getMessage());
					}
					coreRateLimit--;
				} else {
					LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
							"Could not find repository for user with first name: " + profiles.get(i).getFirstName()
									+ ", last name: " + profiles.get(i).getLastName());
				}
			}
		}
		CompletableFuture.allOf(threads).join();
		
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
	}

	/**
	 * Method to fetch input data from file and add to profile
	 * 
	 * @param profiles
	 */
	void addInputData(List<Profile> profiles) {
		final String METHOD_NAME = "addInputData";
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		final String inputFilePath = "input.txt";
		File file = new File(inputFilePath);
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String currentLine;
			while((currentLine = bufferedReader.readLine()) != null) {
				String[] inputs = currentLine.split(" ");
				Profile tempProfile = new Profile();
				tempProfile.setFirstName(inputs[0]);
				if(inputs.length > 1) {
					tempProfile.setLastName(inputs[1]);
				}
				if(inputs.length > 2) {
					StringBuilder location = new StringBuilder();
					for(int i = 2; i < inputs.length; i++) {
						location.append(inputs[i] + " ");
					}
					tempProfile.setLocation(location.toString().trim());
				}
				profiles.add(tempProfile);
			}
			bufferedReader.close();
		} catch (IOException ex) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Error reading data from input file" + inputFilePath);
		}
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
	}

	/**
	 * Method checks if locally saved core rate limit has been exceeded. If yes, it
	 * refreshes the limit using the service. If the service returns 0 as remaining
	 * limit, method throws RateLimitExceededException.
	 * 
	 * @return rateLimit
	 * @throws RateLimitExceededException
	 */
	public boolean isCoreRateLimitExceeded() {
		final String METHOD_NAME = "isCoreRateLimitExceeded";
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		if (coreRateLimit == 0) {
			refreshRateLimit();
		} else {
			return false;
		}
		if (coreRateLimit == 0) {
			throw new RateLimitExceededException("Core Rate Limit exceeded");
		} else {
			return false;
		}
	}

	/**
	 * Method checks if locally saved search rate limit has been exceeded. If yes,
	 * it refreshes the limit using the service. If the service returns 0 as
	 * remaining limit, method throws RateLimitExceededException.
	 * 
	 * @return rateLimit
	 * @throws RateLimitExceededException
	 */
	public boolean isSearchRateLimitExceeded() {
		final String METHOD_NAME = "isSearchRateLimitExceeded";
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		if (searchRateLimit == 0) {
			refreshRateLimit();
		} else {
			return false;
		}
		if (searchRateLimit == 0) {
			throw new RateLimitExceededException("Search Rate Limit exceeded");
		} else {
			return false;
		}
	}

	/**
	 * Method invokes the rate limit Github service and resets the core and search
	 * rate limits.
	 */
	private void refreshRateLimit() {
		final String METHOD_NAME = "refreshRateLimit";
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		final String rateLimitResourceUrl = "https://api.github.com/rate_limit";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(rateLimitResourceUrl)
				.queryParam("client_id", clientId).queryParam("client_secret", clientSecret);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()), String.class);
		if (response.getStatusCode() != HttpStatus.OK) {
			throw new RateLimitExceededException("Error connecting to GitHub. could not refresh rate limit");
		}
		try {
			JSONParser parser = new JSONParser();
			JSONObject responseObject = (JSONObject) parser.parse(response.getBody());
			JSONObject resources = (JSONObject) responseObject.get("resources");
			JSONObject core = (JSONObject) resources.get("core");
			JSONObject search = (JSONObject) resources.get("search");
			coreRateLimit = (Long) core.get("limit");
			searchRateLimit = (Long) search.get("limit");
			LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Core rate limit : " + coreRateLimit + ", search rate limit : " + searchRateLimit);
		} catch (ParseException ex) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, ex.getMessage());
		}
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
	}
}
