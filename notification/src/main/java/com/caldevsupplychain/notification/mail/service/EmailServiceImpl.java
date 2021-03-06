package com.caldevsupplychain.notification.mail.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.caldevsupplychain.notification.mail.model.EmailTemplate;
import com.caldevsupplychain.notification.mail.repository.EmailTemplateRepository;
import com.caldevsupplychain.notification.mail.type.EmailType;

@Slf4j
@Service
@PropertySource("classpath:link.properties")
public class EmailServiceImpl implements EmailService {

	private final String ROOT_URL = "{rootURL}";
	private final String TOKEN_PLACEHOLDER = "{token}";
	@Value("${api.client-port}")
	String clientPort;
	@Autowired
	private Environment env;
	@Autowired
	private EmailTemplateRepository emailTemplateRepository;
	@Autowired
	private JavaMailSender javaMailSender;

	@Async
	@Override
	public void sendVerificationTokenEmail(String targetEmailAddress, String token, String type) throws MessagingException {

		if (env.getProperty("ADMIN_EMAIL_ADDRESS") == null) {
			return;
		}

		EmailTemplate emailTemplate = emailTemplateRepository.findByType(EmailType.valueOf(type));
		emailTemplate.setToEmail(targetEmailAddress);
		emailTemplate.setFromEmail(env.getProperty("ADMIN_EMAIL_ADDRESS"));

		String content = emailTemplate.getContent();
		content = content.contains(ROOT_URL) ? content.replace(ROOT_URL, getRootURL()).replace(TOKEN_PLACEHOLDER, token) : content.replace(TOKEN_PLACEHOLDER, token);
		emailTemplate.setContent(content);

		log.debug("Sending activation email: {}", content);

		sendMimeMessage(emailTemplate);
	}

	public void sendMimeMessage(EmailTemplate emailTemplate) throws MessagingException {

		MimeMessage message = javaMailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setSubject(emailTemplate.getSubject());
		helper.setTo(emailTemplate.getToEmail());
		helper.setFrom(emailTemplate.getFromEmail());
		helper.setText(emailTemplate.getContent(), true);

		javaMailSender.send(message);
	}

	public String getRootURL() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String url = request.getRequestURL().toString().replace(request.getRequestURI(), "");

		if (url.contains("localhost")) {
			url = url.replace(Integer.toString(request.getServerPort()), clientPort);
		}
		return url;
	}
}
