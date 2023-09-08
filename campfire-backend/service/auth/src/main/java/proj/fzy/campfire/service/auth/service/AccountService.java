package proj.fzy.campfire.service.auth.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.SerializeUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import proj.fzy.campfire.model.db.Account;
import proj.fzy.campfire.model.db.Role;
import proj.fzy.campfire.model.dto.AuthenticationInfo;
import proj.fzy.campfire.model.enums.AccountStatus;
import proj.fzy.campfire.model.mq.EmailAccountVerificationMessage;
import proj.fzy.campfire.model.mq.EmailPasswordResetMessage;
import proj.fzy.campfire.service.auth.repository.AccountRepository;
import proj.fzy.campfire.service.auth.repository.RoleRepository;
import proj.fzy.campfire.service.common.utils.DbIdUtils;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.RedisUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.servicecalling.messagequeue.EmailMessageQueueServiceCalling;

@Service
public class AccountService {

    private final DbIdUtils dbIdUtils;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final EmailMessageQueueServiceCalling emailMessageQueueServiceCalling;

    public AccountService(DbIdUtils dbIdUtils, JwtUtils jwtUtils, RedisUtils redisUtils, PasswordEncoder passwordEncoder,
                          AccountRepository accountRepository, RoleRepository roleRepository, EmailMessageQueueServiceCalling emailMessageQueueServiceCalling) {
        this.dbIdUtils = dbIdUtils;
        this.jwtUtils = jwtUtils;
        this.redisUtils = redisUtils;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.emailMessageQueueServiceCalling = emailMessageQueueServiceCalling;
    }


    @Transactional
    public boolean create(String username, String password, String email) {
        boolean result = false;
        try {
            Long newAccountId = dbIdUtils.getNextId();
            accountRepository.insert(newAccountId, username, passwordEncoder.encode(password), email);
            accountRepository.assignRole(newAccountId, roleRepository.queryByName("regular_user").getId());
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return result;
    }

    public AuthenticationInfo signIn(String username, String password) {
        Account dbAccount = queryByUsername(username);
        if (dbAccount != null && passwordEncoder.matches(password, dbAccount.getPassword()) && !dbAccount.getStatus().equals(AccountStatus.BANNED)) {
            Long dbAccountId = dbAccount.getId();
            String token = jwtUtils.generate(dbAccountId, dbAccount.getUsername());
            AuthenticationInfo authenticationInfo = AuthenticationInfo.builder()
                    .id(String.valueOf(dbAccountId))
                    .username(dbAccount.getUsername())
                    .token(token)
                    .email(dbAccount.getEmail())
                    .status(dbAccount.getStatus().name())
                    .roleNames(roleRepository.queryRolesByAccountId(dbAccountId).stream().map(Role::getName).toList())
                    .build();
            redisUtils.setWithDuration(RedisUtils.getSignedInAccountKey(dbAccountId), JSONUtil.toJsonStr(authenticationInfo), jwtUtils.getDuration());
            return authenticationInfo;
        }
        return null;
    }

    public Account queryByUsername(String username) {
        return accountRepository.queryByUsername(username);
    }

    public String loadDescription() {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        Account account = accountRepository.queryById(myAccountId);
        return account.getDescription();
    }

    public boolean updateDescription(String description) {
        boolean result = false;
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        try {
            accountRepository.updateDescriptionById(myAccountId, description);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean updateAvatarUrl(Long id, String avatarUrl) {
        boolean result = false;
        Account dbAccount = accountRepository.queryById(id);
        if (dbAccount != null && !dbAccount.getStatus().equals(AccountStatus.BANNED)) {
            try {
                accountRepository.updateAvatarUrlById(id, avatarUrl);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean generateVerificationCode(Long accountId) {
        boolean result = false;
        try {
            Account dbAccount = accountRepository.queryById(accountId);
            if (dbAccount != null && dbAccount.getStatus().equals(AccountStatus.UNVERIFIED)) {
                String code = RandomUtil.randomString(16);
                redisUtils.setWithDuration(RedisUtils.getWaitingVerificationAccountKey(accountId), code, 600);
                emailMessageQueueServiceCalling.sendAccountVerificationCode(EmailAccountVerificationMessage.builder()
                        .to(dbAccount.getEmail())
                        .code(code)
                        .build());
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean accountVerification(Long accountId, String code) {
        boolean result = false;
        String key = RedisUtils.getWaitingVerificationAccountKey(accountId);
        String value = redisUtils.get(RedisUtils.getWaitingVerificationAccountKey(accountId));
        if (value != null && value.equals(code)) {
            accountRepository.updateStatusById(accountId, AccountStatus.VERIFIED);
            redisUtils.del(key);
            result = true;
        }
        return result;
    }

    public boolean passwordResetRequest(String username) {
        boolean result = false;
        Account dbAccount = accountRepository.queryByUsername(username);
        if (dbAccount != null && !dbAccount.getStatus().equals(AccountStatus.BANNED)) {
            Long dbAccountId = dbAccount.getId();
            String key = RedisUtils.getForgetPasswordAccountKey(dbAccountId);
            if (!redisUtils.exists(key)) {
                String code = RandomUtil.randomString(20);
                redisUtils.setWithDuration(key, code, 300);
                emailMessageQueueServiceCalling.sendPasswordReset(EmailPasswordResetMessage.builder()
                        .to(dbAccount.getEmail())
                        .code(code)
                        .build());
                result = true;
            }
        }
        return result;
    }

    public boolean passwordReset(String username, String code, String newPassword) {
        boolean result = false;
        Account dbAccount = accountRepository.queryByUsername(username);
        if (dbAccount != null && !dbAccount.getStatus().equals(AccountStatus.BANNED)) {
            Long dbAccountId = dbAccount.getId();
            String key = RedisUtils.getForgetPasswordAccountKey(dbAccountId);
            String realCode = redisUtils.get(key);
            if (realCode != null && realCode.equals(code)) {
                redisUtils.del(key);
                accountRepository.updatePasswordById(dbAccountId, passwordEncoder.encode(newPassword));
                result = true;
            }
        }
        return result;
    }

    public Account queryById(Long id) {
        return accountRepository.queryById(id);
    }

    public boolean updatePassword(String oldPassword, String newPassword) {
        boolean result = false;
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        Account account = queryById(myAccountId);
        if (passwordEncoder.matches(oldPassword, account.getPassword())) {
            try {
                accountRepository.updatePassword(myAccountId, passwordEncoder.encode(newPassword));
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
