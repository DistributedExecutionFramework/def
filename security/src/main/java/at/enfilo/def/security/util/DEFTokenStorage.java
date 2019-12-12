package at.enfilo.def.security.util;

import at.enfilo.def.common.api.ITimeoutMap;
import at.enfilo.def.common.api.ITouchable;
import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.common.util.DEFHashHelper;
import at.enfilo.def.transfer.dto.AuthDTO;

import javax.ws.rs.NotAuthorizedException;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;

/**
 * Created by mase on 28.10.16.
 */
public class DEFTokenStorage implements ITouchable<String> {

    private static final int TOKEN_EXPIRATION_RANGE = 1;
    private static final TimeUnit TOKEN_EXPIRATION_TIME_UNIT = DAYS;
    private static final int CLEAN_SCHEDULE_DELAY = 1;
    private static final TimeUnit CLEAN_SCHEDULE_TIME_UNIT = HOURS;

    private static final ITimeoutMap<String, AuthDTO> TOKEN_REGISTRY = new TimeoutMap<>(
        TOKEN_EXPIRATION_RANGE,
        TOKEN_EXPIRATION_TIME_UNIT,
        CLEAN_SCHEDULE_DELAY,
        CLEAN_SCHEDULE_TIME_UNIT
    );

    @Override
    public boolean touch(String token) {
        return TOKEN_REGISTRY.touch(token);
    }

    private static class ThreadSafeLazySingletonWrapper {
        private static final DEFTokenStorage INSTANCE = new DEFTokenStorage();
    }

    private DEFTokenStorage() {
        // Should be implemented as thread safe lazy singleton
    }

    /**
     * Returns reference to DEFTokenStorage.
     *
     * @return reference to DEFTokenStorage
     */
    public static DEFTokenStorage getInstance() {
        return DEFTokenStorage.ThreadSafeLazySingletonWrapper.INSTANCE;
    }

    /**
     * Generates and automatically registers (stores) new AuthDTO for specified userId.
     *
     * @param uId uId to be associated with newly generated AuthDTO.
     * @return auto-associated and auto-registered AuthDTO.
     */
    public AuthDTO getNewAuthDTO(String uId) {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setUserId(uId);
        authDTO.setToken(DEFHashHelper.generateNewToken());

        TOKEN_REGISTRY.put(authDTO.getToken(), authDTO);
        TOKEN_REGISTRY.touch(authDTO.getToken());
        return authDTO;
    }

    /**
     * Returns uId associated with given token (if given token is not expired yet).
     *
     * @param token token to be used for distinguishing the correct uId.
     * @return associated uId.
     * @throws NotAuthorizedException
     */
    public String getAssociatedUserId(String token)
    throws NotAuthorizedException {
        if (!TOKEN_REGISTRY.isExpired(token)) {

            AuthDTO authDTO = TOKEN_REGISTRY.get(token);
            if (authDTO != null) return authDTO.getUserId();
        }
        throw new NotAuthorizedException("Not valid token detected.");
    }

    /**
     * Checks if the given token is valid (registered / not expired).
     *
     * @param token token to check.
     * @return true if valid, false if not.
     */
    public synchronized boolean isValidToken(String token) {
        if (!TOKEN_REGISTRY.isExpired(token)) {
            TOKEN_REGISTRY.touch(token);
            return true;
        }
        return false;
    }

    /**
     * Checks if the token stored in the given authDTO is valid (registered / not expired).
     *
     * @param authDTO authDTO token of which will be checked.
     * @return true if valid, false if not.
     */
    public boolean isValidToken(AuthDTO authDTO) {
        return (authDTO != null && authDTO.isSetToken()) && isValidToken(authDTO.getToken());
    }
}
