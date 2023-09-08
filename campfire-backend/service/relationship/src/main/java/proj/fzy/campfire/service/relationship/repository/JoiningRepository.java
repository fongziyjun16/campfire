package proj.fzy.campfire.service.relationship.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.enums.GroupRole;
import proj.fzy.campfire.model.enums.JoiningStatus;

import java.util.List;

@Repository
public interface JoiningRepository {

    void insert(Long id, Long accountId, String username, Long groupId, GroupRole role, JoiningStatus status, String comment);

    void deleteById(Long id);

    void deleteByGroupId(Long groupId);

    void update(Joining joining);

    void updateRole(Long id, String role);

    Joining queryByAccountIdAndGroupId(Long accountId, Long groupId);

    Joining queryById(Long id);

    List<Joining> queryGroupJoinings(Long groupId);
}
