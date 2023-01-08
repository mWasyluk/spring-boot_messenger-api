package pl.wasyluva.spring_messengerapi.web.http.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;

import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages.*;

@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(value = {HttpMessageNotReadableException.class, NullPointerException.class })
    public ResponseEntity<?> handleHttpMessageNotReadableException() {
        return new ServiceResponse<>(INCORRECT_OBJECT_PROVIDED, HttpStatus.BAD_REQUEST).getResponseEntity();
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleMediaTypeNotSupportedException(){
        return new ServiceResponse<>(UNSUPPORTED_IMAGE_MEDIA_TYPE, HttpStatus.UNSUPPORTED_MEDIA_TYPE).getResponseEntity();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeExceededException(){
        return new ServiceResponse<>(PAYLOAD_TOO_LARGE, HttpStatus.PAYLOAD_TOO_LARGE).getResponseEntity();
    }
}
