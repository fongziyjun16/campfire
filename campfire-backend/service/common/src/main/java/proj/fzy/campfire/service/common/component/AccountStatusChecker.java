package proj.fzy.campfire.service.common.component;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import proj.fzy.campfire.model.dto.AuthenticationInfo;
import proj.fzy.campfire.model.enums.AccountStatus;

@Component("accountStatusChecker")
public class AccountStatusChecker {

    private AuthenticationInfo getAuthenticationInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof AuthenticationInfo ?
                (AuthenticationInfo) authentication.getPrincipal() : null;
    }

    public boolean verified() {
        AuthenticationInfo authenticationInfo = getAuthenticationInfo();
        return authenticationInfo != null && authenticationInfo.getStatus().equals(AccountStatus.VERIFIED.toString());
    }

    public boolean unverified() {
        AuthenticationInfo authenticationInfo = getAuthenticationInfo();
        return authenticationInfo != null && authenticationInfo.getStatus().equals(AccountStatus.UNVERIFIED.toString());
    }

    public boolean anyVerifiedOrUnVerified() {
        AuthenticationInfo authenticationInfo = getAuthenticationInfo();
        return authenticationInfo != null &&
                (authenticationInfo.getStatus().equals(AccountStatus.VERIFIED.toString()) || authenticationInfo.getStatus().equals(AccountStatus.UNVERIFIED.toString()));
    }
}
