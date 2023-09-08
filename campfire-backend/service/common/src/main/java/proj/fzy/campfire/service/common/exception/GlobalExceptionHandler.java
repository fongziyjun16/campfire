package proj.fzy.campfire.service.common.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import proj.fzy.campfire.model.dto.CommonResponse;

//@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public CommonResponse<Void> exceptionHandler(Exception e) {
        e.printStackTrace();
        return CommonResponse.simpleResponse(400500, "There are exceptions.");
    }

}
