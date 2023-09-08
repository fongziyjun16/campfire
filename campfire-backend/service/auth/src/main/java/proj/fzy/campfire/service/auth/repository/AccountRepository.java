package proj.fzy.campfire.service.auth.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.Account;
import proj.fzy.campfire.model.enums.AccountStatus;

@Repository
public interface AccountRepository {
    void insert(Long id, String username, String password, String email);

    void assignRole(Long accountId, Long roleId);

    void updateDescriptionById(Long id, String description);

    void updateStatusById(Long id, AccountStatus status);

    void updatePasswordById(Long id, String password);

    void updateAvatarUrlById(Long id, String avatarUrl);

    void updatePassword(Long id, String password);

    Account queryById(Long id);

    Account queryByUsername(String username);
}
