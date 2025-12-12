package com.example.monghyang.domain.global.annotation.auth;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@Slf4j
public class LoginUserIdArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라메터에 @LoginInfo 어노테이션이 붙어있고,
        // 파라메터 타입이 Long인지 확인하여 결과를 반환
        return parameter.hasParameterAnnotation(LoginUserId.class)
                && Long.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // supportsParameter() 가 true를 반환하면 파라메터에 주입할 실제 값을 만들어 반환한다.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        LoginUserId loginUserId = parameter.getParameterAnnotation(LoginUserId.class);
        boolean required = loginUserId == null || loginUserId.required(); // 인증 정보 필수 여부 옵션의 값

        if(auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken){
            if(required == true) {
                // 인증 정보가 필수로 존재해야 하지만 인증 객체가 존재하지 않거나, '익명 유저'인 경우 userId 파싱 불가
                throw new ApplicationException(ApplicationError.AUTH_INFO_NOT_FOUND);
            }
            return null;
        }

        Object principal = auth.getPrincipal();

        if(principal == null && required == true) {
            // 인증 정보가 필수이면서 인증 정보가 null인 경우에는 예외 발생
            throw new ApplicationException(ApplicationError.AUTH_INFO_NOT_FOUND);
        }
        return principal;
    }
}
