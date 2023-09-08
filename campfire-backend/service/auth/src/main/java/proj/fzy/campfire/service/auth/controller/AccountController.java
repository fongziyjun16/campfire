package proj.fzy.campfire.service.auth.controller;

import cn.hutool.core.util.RandomUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.db.Account;
import proj.fzy.campfire.model.db.Role;
import proj.fzy.campfire.model.dto.AccountInfo;
import proj.fzy.campfire.model.dto.AuthenticationInfo;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.service.auth.service.AccountService;
import proj.fzy.campfire.service.auth.service.RoleService;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.RedisUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final JwtUtils jwtUtils;
    private final AccountService accountService;
    private final RoleService roleService;
    private final RedisUtils redisUtils;

    public AccountController(JwtUtils jwtUtils,
                             AccountService accountService, RoleService roleService,
                             RedisUtils redisUtils) {
        this.jwtUtils = jwtUtils;
        this.accountService = accountService;
        this.roleService = roleService;
        this.redisUtils = redisUtils;
    }

    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> signUp(@RequestParam String username, @RequestParam String password, @RequestParam String email) {
        return accountService.create(username, password, email) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.UNAUTHORIZED.value(), "Duplicate Username");
    }

    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<AuthenticationInfo> signIn(@RequestParam String username, @RequestParam String password, HttpServletRequest request, HttpServletResponse response) {
        AuthenticationInfo authenticationInfo = accountService.signIn(username, password);
        if (authenticationInfo != null) {
            response.setHeader(
                    HttpHeaders.SET_COOKIE,
                    ResponseCookie.from("token", authenticationInfo.getToken())
                            .httpOnly(true)
                            .secure(false)
                            .domain(null)
                            .path("/")
                            .sameSite("Lax")
                            .build()
                            .toString()
            );
            return CommonResponse.simpleSuccessWithData(authenticationInfo);
        }
        return CommonResponse.build(HttpStatus.UNAUTHORIZED.value(), "Wrong username or password or Account Banned", null);
    }

    @PreAuthorize("@accountStatusChecker.verified()")
    @GetMapping("/authenticate")
    public CommonResponse<AuthenticationInfo> authenticate(HttpServletRequest request) {
        String token = ServiceUtils.extraTokenFromRequest(request);
        Account account = accountService.queryById(jwtUtils.getId(token));
        if (account != null) {
            String key = RedisUtils.getWsCsrfTokenKey(account.getId());
            String csrfToken = RandomUtil.randomString(32);
            redisUtils.setWithDuration(key, csrfToken, jwtUtils.getDuration());
            return CommonResponse.simpleSuccessWithData(AuthenticationInfo.builder()
                            .id(String.valueOf(account.getId()))
                            .username(account.getUsername())
                            .csrfToken(csrfToken)
                            .email(account.getEmail())
                            .status(account.getStatus().name())
                            .roleNames(roleService.queryRolesByAccountId(account.getId()).stream().map(Role::getName).toList())
                    .build());
        }
        return CommonResponse.build(HttpStatus.UNAUTHORIZED.value(), "Authentication Failure", null);
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PutMapping(value = "/description", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> updateDescription(@RequestParam String description) {
        return accountService.updateDescription(description) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong User or Banned");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/description")
    public CommonResponse<String> loadDescription() {
        return CommonResponse.simpleSuccessWithData(accountService.loadDescription());
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PutMapping(value = "/avatarUrl", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> updateAvatarUrl(@RequestParam String avatarUrl, HttpServletRequest request) {
        return accountService.updateAvatarUrl(jwtUtils.getId(ServiceUtils.extraTokenFromRequest(request)), avatarUrl) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong User or Banned");
    }

    @PreAuthorize("@accountStatusChecker.verified()")
    @GetMapping(value = "/id/{id}")
    public CommonResponse<AccountInfo> queryById(@PathVariable Long id) {
        Account account = accountService.queryById(id);
        if (account != null) {
            return CommonResponse.simpleSuccessWithData(AccountInfo.builder()
                    .id(String.valueOf(account.getId()))
                    .username(account.getUsername())
                    .description(account.getDescription())
                    .avatarUrl(account.getAvatarUrl())
                    .build());
        }
        return CommonResponse.build(HttpStatus.NOT_FOUND.value(), "User Not Exists", null);
    }

    @PreAuthorize("@accountStatusChecker.verified()")
    @GetMapping(value = "/username/{username}")
    public CommonResponse<AccountInfo> queryByUsername(@PathVariable String username) {
        Account account = accountService.queryByUsername(username);
        if (account != null) {
            return CommonResponse.simpleSuccessWithData(AccountInfo.builder()
                    .id(String.valueOf(account.getId()))
                    .username(account.getUsername())
                    .description(account.getDescription())
                    .avatarUrl(account.getAvatarUrl())
                    .build());
        }
        return CommonResponse.build(HttpStatus.NOT_FOUND.value(), "User Not Exists", null);
    }

    @PreAuthorize("@accountStatusChecker.unverified()")
    @GetMapping(value = "/request-account-verification")
    public CommonResponse<Void> requestAccountVerification(HttpServletRequest request) {
        return accountService.generateVerificationCode(jwtUtils.getId(ServiceUtils.extraTokenFromRequest(request))) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong User or Verified User");
    }

    @PreAuthorize("@accountStatusChecker.unverified()")
    @GetMapping(value = "/account-verification")
    public CommonResponse<Void> accountVerification(@RequestParam String code, HttpServletRequest request) {
        return accountService.accountVerification(jwtUtils.getId(ServiceUtils.extraTokenFromRequest(request)), code) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong User or Verified User");
    }

    @GetMapping(value = "/request-password-reset/{username}")
    public CommonResponse<Void> requestPasswordReset(@PathVariable String username) {
        return accountService.passwordResetRequest(username) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong User or Banned User or Request Once in Every 10 minutes");
    }

    @PostMapping(value = "/password-reset", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> passwordReset(@RequestParam String username, @RequestParam String code, @RequestParam String password) {
        return accountService.passwordReset(username, code, password) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Request Information");
    }

    @PreAuthorize("@accountStatusChecker.verified()")
    @PutMapping(value = "/password")
    public CommonResponse<Void> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        return accountService.updatePassword(oldPassword, newPassword) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Old Password");
    }
}
