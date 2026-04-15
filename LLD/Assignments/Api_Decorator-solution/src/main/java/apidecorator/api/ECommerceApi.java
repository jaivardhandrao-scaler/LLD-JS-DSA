package apidecorator.api;

import apidecorator.utils.ApiUtils;
import apidecorator.utils.Logger;
import apidecorator.utils.RateLimitExceededException;

public class ECommerceApi {

    private static final String API_ENDPOINT = "http://ecommerce.api";

    public String executeRequest(String requestData) {

        Logger.log("Processing request: " + requestData);

        if (!ApiUtils.rateLimitExceeded(API_ENDPOINT)) {
            throw new RateLimitExceededException("Rate limit exceeded for API endpoint: " + API_ENDPOINT);
        }

        String response = ApiUtils.callAPI(API_ENDPOINT, requestData);
        Logger.log("Response: " + response);

        return response;
    }
}
