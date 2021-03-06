package com.caldevsupplychain.account.security;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caldevsupplychain.account.service.AccountService;
import com.caldevsupplychain.account.service.PermissionService;
import com.caldevsupplychain.account.util.UserMapper;
import com.caldevsupplychain.account.vo.UserBean;

@Slf4j
@Component
public class JpaRealm extends AuthorizingRealm {

	@Autowired
	private AccountService accountService;
	@Autowired
	private PermissionService permissionService;
	@Autowired
	private PasswordService passwordService;
	@Autowired
	private UserMapper userMapper;

	@Override
	public boolean supports(AuthenticationToken token) {
		return token != null && token.getClass().isAssignableFrom(UsernamePasswordToken.class);
	}

	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) {
		UsernamePasswordToken token = (UsernamePasswordToken) authcToken;

		UserBean user = accountService.findByEmailAddress(token.getUsername()).orElse(null);

		if (user != null && passwordService.passwordsMatch(token.getPassword(), user.getPassword())) {
			return new SimpleAuthenticationInfo(user, user.getPassword(), getName());
		}

		return null;
	}

	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		if (principals.fromRealm(getName()).isEmpty()) {
			return null;
		}
		String uuid = (String) principals.getPrimaryPrincipal();
		UserBean user = accountService.findByUuid(uuid).orElse(null);

		if (user != null) {
			Set<String> roles = user.getRoles().stream().map(r -> r.toString()).collect(Collectors.toSet());
			SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roles);
			info.addStringPermissions(permissionService.getPermissions(roles));
			return info;
		}
		return null;
	}
}
