package com.bcld.domain.shiro;

import javax.servlet.ServletRequest;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.kaptcha.Constants;

public class CaptchaFormAuthenticationFilter extends FormAuthenticationFilter {

    private static Logger log = LoggerFactory.getLogger(CaptchaFormAuthenticationFilter.class);

    @Override
    protected CaptchaUsernamePasswordToken createToken(ServletRequest request, ServletResponse response) {
        String username = getUsername(request);
        String password = getPassword(request);
        String captcha = WebUtils.getCleanParam(request, "captcha");
        boolean rememberMe = isRememberMe(request);
        String host = getHost(request);
        return new CaptchaUsernamePasswordToken(username, password, rememberMe, host, captcha);
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        CaptchaUsernamePasswordToken captchaUsernamePasswordToken = createToken(request, response);
        try {
            doCaptchaValidate((HttpServletRequest) request, captchaUsernamePasswordToken);
            Subject subject = getSubject(request, response);
            subject.login(captchaUsernamePasswordToken);
            return onLoginSuccess(captchaUsernamePasswordToken, subject, request, response);
        } catch (AuthenticationException e) {
            request.setAttribute("error", "登陆错误");
            return onLoginFailure(captchaUsernamePasswordToken, e, request, response);
        }
    }

    private void doCaptchaValidate(HttpServletRequest httpServletRequest, CaptchaUsernamePasswordToken captchaUsernamePasswordToken) {
       /* String captcha = (String) httpServletRequest.getSession().getAttribute(Constants.KAPTCHA_SESSION_KEY);
        if (StringUtils.isBlank(captcha) || !captcha.equalsIgnoreCase(captchaUsernamePasswordToken.getCaptcha())) {
            throw new AuthenticationException();
        }*/
    }

}
