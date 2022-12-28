package pl.wasyluva.spring_messengerapi.data.service.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ServiceResponse<T> extends ResponseEntity<T> {
    public static final ServiceResponse<String> INCORRECT_ID = new ServiceResponse<>(ServiceResponseMessages.EXISTING_ID_REQUIRED, HttpStatus.NOT_FOUND);
    public static final ServiceResponse<String> NOT_FOUND = new ServiceResponse<>(ServiceResponseMessages.NOT_FOUND_ENTITY, HttpStatus.NOT_FOUND);
    public static final ServiceResponse<String> UNAUTHORIZED = new ServiceResponse<>(ServiceResponseMessages.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    public static final ServiceResponse<String> OK = new ServiceResponse<>(ServiceResponseMessages.OK, HttpStatus.OK);

    public static ResponseEntity<?> toResponseEntity(ServiceResponse<?> serviceResponse){
        return new ResponseEntity<>(serviceResponse.getBody(), serviceResponse.getStatusCode());
    }

    public ServiceResponse(T body, HttpStatus status) {
        super(body, status);
    }

    @JsonIgnore
    @Override
    public HttpHeaders getHeaders() {
        return super.getHeaders();
    }

    @JsonProperty("httpStatus")
    @Override
    public HttpStatus getStatusCode() {
        return super.getStatusCode();
    }

    @JsonProperty("httpStatusCode")
    @Override
    public int getStatusCodeValue() {
        return super.getStatusCodeValue();
    }

    @JsonIgnore
    public ResponseEntity<?> getResponseEntity(){
        return new ResponseEntity<>(this, this.getStatusCode());
    }

}
