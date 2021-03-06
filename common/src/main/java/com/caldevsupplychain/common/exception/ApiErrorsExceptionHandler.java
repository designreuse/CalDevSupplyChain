package com.caldevsupplychain.common.exception;

import static java.util.stream.Collectors.toList;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.caldevsupplychain.common.type.ErrorCode;
import com.caldevsupplychain.common.ws.ApiErrorsWS;
import com.caldevsupplychain.common.ws.ErrorWS;
import io.jsonwebtoken.JwtException;

@Slf4j
@ControllerAdvice
public class ApiErrorsExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({JwtException.class})
	public ResponseEntity<?> handleJwtAuthenticationException(JwtException e) {

		log.error("Unexpected error", e);

		return new ResponseEntity<Object>(new ApiErrorsWS(ErrorCode.JWT_EXCEPTION.name(), e.getMessage()), HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler({ShiroException.class})
	public ResponseEntity<?> handleAuthenticationException(ShiroException e) {

		log.error("Unexpectd error: ", e);

		if (e instanceof UnauthenticatedException) {
			return new ResponseEntity<>(new ApiErrorsWS(ErrorCode.UNAUTHENTICATED.name(), e.getMessage()), HttpStatus.UNAUTHORIZED);
		} else if (e instanceof UnauthorizedException) {
			return new ResponseEntity<>(new ApiErrorsWS(ErrorCode.UNAUTHORIZED.name(), e.getMessage()), HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>(new ApiErrorsWS(e.getClass().getSimpleName(), e.getMessage()), HttpStatus.UNAUTHORIZED);
	}

	public List<ErrorWS> generateErrorWSList(BindingResult errors) {
		return errors.getFieldErrors()
				.stream()
				.map(error -> new ErrorWS(
						error.getCode(),
						error.getDefaultMessage()
				))
				.collect(toList());
	}
}
