package proj.fzy.campfire.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class CommonResponse<T> {
    private Integer code;
    private String message;
    private T data;

    public static CommonResponse<Void> simpleSuccess() {
        return CommonResponse.<Void>builder()
                .code(200)
                .message("success")
                .data(null)
                .build();
    }

    public static <T> CommonResponse<T> simpleSuccessWithData(T data) {
        return CommonResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    public static CommonResponse<Void> simpleResponse(Integer code, String message) {
        return CommonResponse.<Void>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> CommonResponse<T> build(Integer code, String message, T data) {
        return CommonResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

}
