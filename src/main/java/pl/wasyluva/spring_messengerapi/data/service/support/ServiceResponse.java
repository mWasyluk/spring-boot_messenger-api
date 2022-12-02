package pl.wasyluva.spring_messengerapi.data.service.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Data
public class ServiceResponse<T> {
    @JsonProperty("object")
    private final T object;
    @JsonIgnore
    private final HttpStatus httpStatus;
    @JsonProperty("message")
    private final String message;

    @JsonProperty("httpStatusCode")
    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
