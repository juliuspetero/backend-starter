package com.mojagap.backenstarter.infrastructure.security;

import com.mojagap.backenstarter.infrastructure.AppContext;
import com.mojagap.backenstarter.infrastructure.ApplicationConstants;
import com.mojagap.backenstarter.infrastructure.ErrorMessages;
import com.mojagap.backenstarter.infrastructure.PowerValidator;
import com.mojagap.backenstarter.infrastructure.exception.ForbiddenException;
import com.mojagap.backenstarter.infrastructure.exception.UnauthorizedException;
import com.mojagap.backenstarter.infrastructure.utility.CsvUtil;
import com.mojagap.backenstarter.model.account.AccountType;
import com.mojagap.backenstarter.model.common.AuditEntity;
import com.mojagap.backenstarter.model.role.Permission;
import com.mojagap.backenstarter.model.role.PermissionEnum;
import com.mojagap.backenstarter.model.user.AppUser;
import com.mojagap.backenstarter.repository.user.AppUserRepository;
import com.mojagap.backenstarter.service.account.AccountCommandHandlerService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final Logger LOG = Logger.getLogger(JwtAuthorizationFilter.class.getName());

    private static final String ANONYMOUS_USER_PATHS = "" +
            "/v1/account/authenticate:GET" +
            "/v1/account:POST";

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain chain) {
        try {
            String authenticationToken = servletRequest.getHeader(ApplicationConstants.AUTHENTICATION_HEADER_NAME);
            String requestPath = servletRequest.getRequestURI() + ":" + servletRequest.getMethod();
            if (!ANONYMOUS_USER_PATHS.contains(requestPath)) {
                AppUser appUser = verifyAuthenticationToken(authenticationToken);
                AppContext.setLoggedInUser(appUser);
                verifyPermissions(servletRequest, appUser);
                authenticationToken = AccountCommandHandlerService.generateAuthenticationToken(AppContext.getLoggedInUser());
                servletResponse.setHeader(ApplicationConstants.AUTHENTICATION_HEADER_NAME, authenticationToken);
            }
            chain.doFilter(servletRequest, servletResponse);
        } catch (Exception ex) {
            HandlerExceptionResolver handlerExceptionResolver = AppContext.getBean("handlerExceptionResolver");
            handlerExceptionResolver.resolveException(servletRequest, servletResponse, null, ex);
        }
    }

    private AppUser verifyAuthenticationToken(String authentication) {
        try {
            String secretKey = Base64.getEncoder().encodeToString(ApplicationConstants.JWT_SECRET_KEY.getBytes());
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authentication).getBody();
            AppUserRepository appUserRepository = AppContext.getBean(AppUserRepository.class);
            Integer userId = claims.get(ApplicationConstants.APP_USER_ID, Integer.class);
            AppUser appUser = appUserRepository.findOneByIdAndRecordStatus(userId, AuditEntity.RecordStatus.ACTIVE);
            PowerValidator.notNull(appUser, ErrorMessages.INVALID_SECURITY_CREDENTIAL);
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (EnumSet.of(AccountType.BACK_OFFICE, AccountType.COMPANY).contains(appUser.getAccount().getAccountType())) {
                appUser.getRole().getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getName())).forEach(authorities::add);
            }
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(appUser.getEmail(), null, authorities));
            return appUser;
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error while parsing authentication string : " + ex.getMessage(), ex);
            throw new UnauthorizedException(ErrorMessages.INVALID_SECURITY_CREDENTIAL);
        }
    }

    private void verifyPermissions(HttpServletRequest request, AppUser appUser) {
        String requestMethod = request.getMethod();
        String requestURI = request.getRequestURI();
        AccountType accountType = appUser.getAccount().getAccountType();
        List<RequestSecurity> requestSecurities = CsvUtil.parseSecurityCsv()
                .stream()
                .filter(security -> requestMethod.equals(security.getHttpMethod())
                        && UrlSecurityMatcher.matches(security.getUrl(), requestURI)
                        && List.of(security.getAccountTypes().split(",")).contains(accountType.name())).collect(Collectors.toList());
        Set<String> pathPermissions = new HashSet<>();
        if (!CollectionUtils.isEmpty(requestSecurities)) {
            pathPermissions.add(PermissionEnum.SUPER_PERMISSION.name());
            requestSecurities.forEach(security -> pathPermissions.addAll(List.of(security.getPermissions().split(","))));
        }
        Set<String> userPermissions = EnumSet.of(AccountType.BACK_OFFICE, AccountType.COMPANY).contains(accountType) ?
                appUser.getRole().getPermissions().stream().map(Permission::getName).collect(Collectors.toSet()) : new HashSet<>(Collections.singleton(PermissionEnum.SUPER_PERMISSION.name()));
        userPermissions.add(PermissionEnum.AUTHENTICATED.name());
        if (!CollectionUtils.containsAny(pathPermissions, userPermissions)) {
            throw new ForbiddenException(ErrorMessages.FORBIDDEN_INSUFFICIENT_PERMISSION);
        }
    }

    @Data
    public static class RequestSecurity {
        private String httpMethod;
        private String url;
        private String accountTypes;
        private String permissions;
    }
}
