package com.caldevsupplychain.account.controller;

import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.caldevsupplychain.account.service.AccountService;
import com.caldevsupplychain.account.util.UserMapper;
import com.caldevsupplychain.account.validator.EditUserValidator;
import com.caldevsupplychain.account.validator.SignupValidator;
import com.caldevsupplychain.account.vo.UserBean;
import com.caldevsupplychain.common.exception.ApiErrorsExceptionHandler;
import com.caldevsupplychain.common.type.ErrorCode;
import com.caldevsupplychain.common.ws.account.ApiErrorsWS;
import com.caldevsupplychain.common.ws.account.ErrorWS;
import com.caldevsupplychain.common.ws.account.UserWS;
import com.caldevsupplychain.notification.mail.service.EmailService;
import com.caldevsupplychain.notification.mail.type.EmailType;
import com.google.common.collect.Lists;

@Slf4j
@RestController
@RequestMapping("/api/account/v1")
@AllArgsConstructor
public class AccountControllerImpl implements AccountController {

	private AccountService accountService;
	private EmailService emailService;
	private UserMapper userMapper;

	/* validators */
	private SignupValidator signupValidator;
	private EditUserValidator editUserValidator;

	/* exception handler */
	private ApiErrorsExceptionHandler apiErrorsExceptionHandler;

	/************************************************************************************************
	 |									Account API													|
	 ************************************************************************************************/
	@GetMapping("/users")
	public ResponseEntity<?> getUsers() {
		// custommize logic to pass in page search limit
		List<UserBean> users = accountService.getAllUsers();
		return new ResponseEntity<Object>(userMapper.userBeansToUserWSs(users), HttpStatus.OK);
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestParam(required = false, defaultValue = "USER") String role, @Validated @RequestBody UserWS userWS) {
		BindException errors = new BindException(userWS, "UserWS");

		signupValidator.validate(userWS, errors);

		if (errors.hasErrors()) {
			log.error("Error in signup user. Fail in signup validation fields. userWS={}", userWS.toString());
			List<ErrorWS> errorWSList = apiErrorsExceptionHandler.generateErrorWSList(errors);
			return new ResponseEntity<>(new ApiErrorsWS(errorWSList), HttpStatus.UNPROCESSABLE_ENTITY);
		}

		userWS.setRoles(Lists.newArrayList(role));

		if (accountService.userExist(userWS.getEmailAddress())) {
			return new ResponseEntity<>(new ApiErrorsWS(ErrorCode.ACCOUNT_EXIST.name(), "Account already registered."), HttpStatus.CONFLICT);
		}

		UserBean userBean = userMapper.userWSToBean(userWS);

		UserBean user = accountService.createUser(userBean);

		try {
			emailService.sendVerificationTokenEmail(user.getEmailAddress(), user.getToken(), EmailType.REGISTRATION.name());
		} catch (MessagingException e) {
			return new ResponseEntity<>(new ApiErrorsWS(ErrorCode.EMAIL_MESSAGING_EXCEPTION.name(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(userMapper.userBeanToWS(user), HttpStatus.CREATED);
	}

	@RequiresPermissions("account:update")
	@PutMapping("/users/{uuid}")
	public ResponseEntity<?> updateUser(@PathVariable("uuid") String uuid, @Validated @RequestBody UserWS userWS) {
		BindException errors = new BindException(userWS, "UserWS");

		editUserValidator.validate(userWS, errors);

		if (errors.hasErrors()) {
			log.error("Error in update user. Fail in edit user validation fields. userWS={}", userWS.toString());
			List<ErrorWS> errorWSList = apiErrorsExceptionHandler.generateErrorWSList(errors);
			return new ResponseEntity<>(new ApiErrorsWS(errorWSList), HttpStatus.UNPROCESSABLE_ENTITY);
		}

		Optional<UserBean> user = accountService.findByUuid(uuid);
		if (!user.isPresent()) {
			log.error("Error in update user. Fail in finding user's uuid={}", uuid);
			return new ResponseEntity<>(new ApiErrorsWS(ErrorCode.ACCOUNT_NOT_EXIST.name(), "Cannot find user account."), HttpStatus.NOT_FOUND);
		}

		Subject subject = SecurityUtils.getSubject();
		if(!user.get().isAdmin() || !subject.getPrincipal().toString().equals(uuid)) {
			return new ResponseEntity<>(new ApiErrorsWS(ErrorCode.PERMISSION_DENIED_ON_ROLE_UPDATE.name(), "Cannot update user information"), HttpStatus.BAD_REQUEST);
		}

		if(!StringUtils.isNotBlank(userWS.getEmailAddress())) {
			return new ResponseEntity<>(new ApiErrorsWS(ErrorCode.PERMISSION_DENIED_ON_EMAIL_UPDATE.name(), "User cannot update email address"), HttpStatus.BAD_REQUEST);
		}

		UserBean userBean = userMapper.userWSToBean(userWS);

		UserBean updatedUser = accountService.updateUser(userBean);

		log.info("Success in update user={}", updatedUser.toString());

		return new ResponseEntity<>(userMapper.userBeanToWS(updatedUser), HttpStatus.OK);
	}

	@GetMapping("/activate/{token}")
	public ResponseEntity<?> activateAccount(@PathVariable("token") String token) {
		Optional<UserBean> user = accountService.findByToken(token);

		if (!user.isPresent()) {
			return new ResponseEntity<Object>(new ApiErrorsWS(ErrorCode.INVALID_TOKEN.name(), "Invalid Token."), HttpStatus.BAD_REQUEST);
		}
		accountService.activateUser(user.get().getId());

		return new ResponseEntity<>(userMapper.userBeanToWS(user.get()), HttpStatus.OK);
	}
}