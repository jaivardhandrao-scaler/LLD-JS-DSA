package apidecorator;

import apidecorator.api.Api;
import lombok.AllArgsConstructor;

/**
 * TODO Task 3a - Each decorator should have the same methods as the product it decorates i.e. executeRequest
 * Inherit from the API interface {@link apidecorator.api.Api}.
 */
@AllArgsConstructor
public abstract class BaseApiDecorator implements Api {
    /**
     * TODO Task 3b - Each decorator will have to call the next decorator in the chain and finally call the {@link apidecorator.api.SimpleEcommerceAPI} to execute the request.
     * To achieve this, you will need to store a reference to the next layer. Add a field that stores the next layer of
     * type {@link apidecorator.api.Api}.
     */
    protected Api nextLayer;
}
