package proj.fzy.campfire.service.auth.service;

import org.springframework.stereotype.Service;
import proj.fzy.campfire.model.db.Role;
import proj.fzy.campfire.service.auth.repository.RoleRepository;
import proj.fzy.campfire.service.common.utils.DbIdUtils;

import java.util.List;

@Service
public class RoleService {

    private final DbIdUtils dbIdUtils;
    private final RoleRepository roleRepository;

    public RoleService(DbIdUtils dbIdUtils, RoleRepository roleRepository) {
        this.dbIdUtils = dbIdUtils;
        this.roleRepository = roleRepository;
    }

    public boolean create(String name, String description) {
        boolean result = false;
        try {
            roleRepository.insert(dbIdUtils.getNextId(), name, description);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Role> queryRolesByAccountId(Long accountId) {
        return roleRepository.queryRolesByAccountId(accountId);
    }

}
