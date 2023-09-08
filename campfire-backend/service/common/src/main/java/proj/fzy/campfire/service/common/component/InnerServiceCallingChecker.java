package proj.fzy.campfire.service.common.component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class InnerServiceCallingChecker {

    private final HttpServletRequest request;

    public InnerServiceCallingChecker(HttpServletRequest request) {
        this.request = request;
    }

    public boolean isInnerServiceCalling() {
        String value = request.getHeader("inner-service-calling");
        return value != null && value.equals("true");
    }

}
