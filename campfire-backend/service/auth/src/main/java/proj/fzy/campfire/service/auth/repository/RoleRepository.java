package proj.fzy.campfire.service.auth.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.Role;

import java.util.List;

@Repository
public interface RoleRepository {
    void insert(Long id, String name, String description);

    Role queryByName(String name);

    List<Role> queryRolesByAccountId(Long accountId);
}
