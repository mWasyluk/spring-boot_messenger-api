package pl.wasyluva.spring_messengerapi.web.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;

import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages.INCORRECT_OBJECT_PROVIDED;

@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(value = {HttpMessageNotReadableException.class, NullPointerException.class })
    public ResponseEntity<?> handleHttpMessageNotReadableException() {
        StringBuilder message = new StringBuilder(INCORRECT_OBJECT_PROVIDED);
//        if (e instanceof NullPointerException){
//            message.append("; NullPointer");
//        } else if (e instanceof HttpMessageNotReadableException) {
//            message.append("; MessageNotReadable");
//        }
        ServiceResponse<String> stringServiceResponse = new ServiceResponse<>(message.toString(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(stringServiceResponse, stringServiceResponse.getStatusCode());
    }
}
