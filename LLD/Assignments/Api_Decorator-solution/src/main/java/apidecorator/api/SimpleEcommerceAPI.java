package apidecorator.api;

import apidecorator.utils.ApiUtils;

/**
 * TODO: Task 2 - Implement the API interface to simply call the Simple Ecommerce API, without any additional functionality.
 * Methods you can use
 * 1. {@link apidecorator.utils.ApiUtils#callAPI(String, String)}
 */
public class SimpleEcommerceAPI implements Api {

    private static final String API_URL = "https://api.ecommerce.com";

    @Override
    public String executeRequest(String requestData) {
        return ApiUtils.callAPI(API_URL, requestData);
    }
}
