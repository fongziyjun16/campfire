package proj.fzy.campfire.service.relationship.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.Group;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.dto.GroupHeadDto;
import proj.fzy.campfire.model.dto.JoinInRequestDto;
import proj.fzy.campfire.model.dto.WaitingJoinInGroupDto;

import java.util.List;

@Repository
public interface GroupRepository {

    void insert(Long id, Long creatorId, String name, String description);

    void deleteById(Long id);

    Group queryById(Long id);

    Long countJoiningInGroups(Long accountId);

    List<GroupHeadDto> queryJoiningInGroupsByPage(Long accountId, Long limit, Long offset);

    Long countNotJoinInGroups(Long accountId, String searchingName);

    List<Group> queryNotJoinInGroupsByPage(Long accountId, String searchingName, Long limit, Long offset);

    Long countWaitingJoinInGroups(Long accountId);

    List<WaitingJoinInGroupDto> queryWaitingJoinInGroupsByPage(Long accountId, Long limit, Long offset);

    Long countJoinInRequests(Long groupId);

    List<JoinInRequestDto> queryJoinInRequestsByPage(Long groupId, Long limit, Long offset);

    Long countGroupMembers(Long accountId, Long groupId);

    List<Joining> queryGroupMembersByPage(Long accountId, Long groupId, Long limit, Long offset);
}
