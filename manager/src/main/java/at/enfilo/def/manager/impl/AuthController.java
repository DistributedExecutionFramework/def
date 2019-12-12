package at.enfilo.def.manager.impl;

import at.enfilo.def.common.util.DEFHashHelper;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.persistence.api.IPersistenceFacade;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.persistence.dao.PersistenceFacade;
import at.enfilo.def.security.util.DEFTokenStorage;
import at.enfilo.def.transfer.dto.AuthDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AuthController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);
	private final IPersistenceFacade persistenceFacade;

	/**
	 * Private class to provide thread safe singleton
	 */
	private static class ThreadSafeLazySingletonWrapper {
		private static final AuthController INSTANCE = new AuthController(new PersistenceFacade());

		private ThreadSafeLazySingletonWrapper() {}
	}

	/**
	 * Private constructor, use getInstance();
	 */
	private AuthController(IPersistenceFacade persistenceFacade) {
		this.persistenceFacade = persistenceFacade;
	}


	/**
	 * Singleton pattern.
	 * @return a AuthController instance.
	 */
	static AuthController getInstance() {
		return ThreadSafeLazySingletonWrapper.INSTANCE;
	}


	/**
	 * Returns a token for user/password, empty token if authentication failed.
	 *
	 * @param name - username
	 * @param password - user password
	 * @return
	 */
	public AuthDTO getToken(String name, String password) {
		try {
			User user = persistenceFacade.getNewUserDAO().getUserByName(name);
			if (user != null && DEFHashHelper.doPasswordMatch(password, user.getSalt(), user.getPass())) {
				// Login successful
				return DEFTokenStorage.getInstance().getNewAuthDTO(user.getId());
			}

		} catch (PersistenceException e) {
			LOGGER.warn("Error occurred while fetching user by name for auth.", e);
		} catch (SecurityException e) {
			LOGGER.warn("Error occurred while checking password for match.", e);
		}

		return new AuthDTO();
	}
}
