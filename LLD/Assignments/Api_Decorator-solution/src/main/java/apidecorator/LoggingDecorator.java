package apidecorator;


import apidecorator.api.Api;

/**
 * TODO Task 4a - Extend the {@link BaseApiDecorator} interface to implement the logging functionality.
 * Remember
 * 1. When you inherit from the {@link BaseApiDecorator}, you will have to implement the executeRequest method.
 * 2. You will also need to call the next layer in the chain to execute the request.
 * 3. Each decorator would also need a constructor that takes the next layer of type {@link apidecorator.api.Api}.
 */
public class LoggingDecorator extends BaseApiDecorator {

    public LoggingDecorator(Api nextLayer) {
        super(nextLayer);
    }

    @Override
    public String executeRequest(String requestData) {
        System.out.println("Logging request: " + requestData);
        return nextLayer.executeRequest(requestData);
    }
}