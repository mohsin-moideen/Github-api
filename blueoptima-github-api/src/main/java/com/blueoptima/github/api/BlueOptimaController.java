package com.blueoptima.github.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.blueoptima.github.profile.Profile;

/**
 * Controller class to handle requests
 * 
 * @author MohsinM
 *
 */
@RestController
public class BlueOptimaController {

	private final static Logger LOGGER = Logger.getLogger(BlueOptimaController.class.getName());
	private final static String CLASS_NAME = "BlueOptimaController";

	/**
	 * Method to handle GET requests to /profiles and returns a list of profiles with
	 * Github profile info, repositories and contributions
	 * 
	 * @return List<Profile>
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/profiles")
	public List<Profile> getProfiles() {
		final String METHOD_NAME = "getProfiles";
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		BlueOptimaServiceHelper  blueOptimaServiceHelper = new BlueOptimaServiceHelper();
		List<Profile> profiles = new ArrayList<>();
		if (!blueOptimaServiceHelper.isCoreRateLimitExceeded() && !blueOptimaServiceHelper.isSearchRateLimitExceeded()) {
			blueOptimaServiceHelper.addInputData(profiles);
			blueOptimaServiceHelper.addProfileInfo(profiles);
		}
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		return profiles;
	}
}