package proj.fzy.campfire.service.relationship.service;

import cn.hutool.core.date.DateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import proj.fzy.campfire.model.db.Group;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.dto.*;
import proj.fzy.campfire.model.enums.GroupRole;
import proj.fzy.campfire.model.enums.GroupStatus;
import proj.fzy.campfire.model.enums.JoiningStatus;
import proj.fzy.campfire.service.common.utils.DbIdUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.relationship.repository.GroupRepository;
import proj.fzy.campfire.service.relationship.repository.JoiningRepository;
import proj.fzy.campfire.servicecalling.auth.AccountServiceCalling;
import proj.fzy.campfire.servicecalling.message.GroupChatServiceCalling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GroupService {

    private final DbIdUtils dbIdUtils;
    private final GroupRepository groupRepository;
    private final JoiningRepository joiningRepository;
    private final AccountServiceCalling accountServiceCalling;
    private final GroupChatServiceCalling groupChatServiceCalling;

    public GroupService(
            DbIdUtils dbIdUtils,
            GroupRepository groupRepository,
            JoiningRepository joiningRepository,
            AccountServiceCalling accountServiceCalling, GroupChatServiceCalling groupChatServiceCalling) {
        this.dbIdUtils = dbIdUtils;
        this.groupRepository = groupRepository;
        this.joiningRepository = joiningRepository;
        this.accountServiceCalling = accountServiceCalling;
        this.groupChatServiceCalling = groupChatServiceCalling;
    }

    @Transactional
    public boolean createGroup(String groupName, String groupDescription) {
        boolean result = false;
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        try {
            Long newGroupId = dbIdUtils.getNextId();
            groupRepository.insert(newGroupId, myAccountId, groupName, groupDescription);
            joiningRepository.insert(dbIdUtils.getNextId(), myAccountId, ServiceUtils.getUsernameFromSecurityContext(), newGroupId, GroupRole.LEADER, JoiningStatus.IN, "");
            try {
                // build group chat
                CommonResponse<Void> startGroupChatResp = groupChatServiceCalling.startGroupChat(newGroupId, groupName);
                if (startGroupChatResp.getCode().equals(HttpStatus.OK.value())) {
                    groupChatServiceCalling.joinGroupChat(newGroupId, myAccountId, ServiceUtils.getUsernameFromSecurityContext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return result;
    }

    public boolean applyToJoin(Long groupId, String comment) {
        boolean result = false;
        try {
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            Group dbGroup = groupRepository.queryById(groupId);
            if (dbGroup != null && dbGroup.getStatus().equals(GroupStatus.ACTIVE)) {
                Joining dbJoining = joiningRepository.queryByAccountIdAndGroupId(myAccountId, groupId);
                if (dbJoining == null) {
                    joiningRepository.insert(dbIdUtils.getNextId(), myAccountId, ServiceUtils.getUsernameFromSecurityContext(), groupId, GroupRole.MEMBER, JoiningStatus.WAITING, comment);
                    result = true;
                } else {
                    JoiningStatus status = dbJoining.getStatus();
                    if (status.equals(JoiningStatus.OUT) || status.equals(JoiningStatus.DENY)) {
                        dbJoining.setStatus(JoiningStatus.WAITING);
                        dbJoining.setComment(comment);
                        dbJoining.setJoinTime(null);
                        joiningRepository.update(dbJoining);
                        result = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean confirmJoiningApplication(Long joiningId, boolean accept) {
        boolean result = false;
        try {
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            Joining dbJoining = joiningRepository.queryById(joiningId);
            if (dbJoining != null && dbJoining.getStatus().equals(JoiningStatus.WAITING)) {
                Joining operatorJoining = joiningRepository.queryByAccountIdAndGroupId(myAccountId, dbJoining.getGroupId());
                Group dbGroup = groupRepository.queryById(dbJoining.getGroupId());
                if (dbGroup != null && dbGroup.getStatus().equals(GroupStatus.ACTIVE) &&
                        operatorJoining.getRole().equals(GroupRole.LEADER)) {
                    try {
                        if (accept) {
                            dbJoining.setStatus(JoiningStatus.IN);
                            dbJoining.setJoinTime(DateUtil.date());
                            joiningRepository.update(dbJoining);
                            try {
                                groupChatServiceCalling.joinGroupChat(
                                        dbGroup.getId(),
                                        dbJoining.getAccountId(),
                                        accountServiceCalling.queryById(dbJoining.getAccountId()).getData().getUsername()
                                );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            joiningRepository.deleteById(joiningId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Transactional
    public boolean leave(Long groupId) {
        boolean result = false;
        try {
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            Joining dbJoining = joiningRepository.queryByAccountIdAndGroupId(myAccountId, groupId);
            if (dbJoining != null && dbJoining.getStatus().equals(JoiningStatus.IN) && dbJoining.getRole().equals(GroupRole.MEMBER)) {
                joiningRepository.deleteById(dbJoining.getId());
                if (!groupChatServiceCalling.leaveGroupChat(groupId).getCode().equals(HttpStatus.OK.value())) {
                    throw new RuntimeException();
                }
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return result;
    }

    @Transactional
    public boolean dismissGroup(Long groupId) {
        boolean result = false;
        Joining dbJoining = joiningRepository.queryByAccountIdAndGroupId(ServiceUtils.getAccountIdFromSecurityContext(), groupId);
        if (dbJoining != null &&
                dbJoining.getRole().equals(GroupRole.LEADER) &&
                dbJoining.getStatus().equals(JoiningStatus.IN)) {
            try {
                joiningRepository.deleteByGroupId(groupId);
                groupRepository.deleteById(groupId);
                if (!groupChatServiceCalling.deleteGroupChat(groupId).getCode().equals(HttpStatus.OK.value())) {
                    throw new RuntimeException();
                }
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }
        return result;
    }

    @Transactional
    public boolean removeMember(Long accountId, Long groupId) {
        boolean result = false;
        Group dbGroup = groupRepository.queryById(groupId);
        if (dbGroup != null) {
            Joining dbJoining = joiningRepository.queryByAccountIdAndGroupId(ServiceUtils.getAccountIdFromSecurityContext(), groupId);
            if (dbJoining != null && dbJoining.getRole().equals(GroupRole.LEADER) && dbJoining.getStatus().equals(JoiningStatus.IN)) {
                Joining membweJoining = joiningRepository.queryByAccountIdAndGroupId(accountId, groupId);
                if (membweJoining != null) {
                    try {
                        joiningRepository.deleteById(membweJoining.getId());
                        groupChatServiceCalling.makeMemberLeaveGroupChat(accountId, groupId);
                        result = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    }
                }
            }
        }
        return result;
    }

    @Transactional
    public boolean transferLeader(Long accountId, Long groupId) {
        boolean result = false;
        Group dbGroup = groupRepository.queryById(groupId);
        if (dbGroup != null) {
            Joining dbJoining = joiningRepository.queryByAccountIdAndGroupId(ServiceUtils.getAccountIdFromSecurityContext(), groupId);
            if (dbJoining != null && dbJoining.getRole().equals(GroupRole.LEADER) && dbJoining.getStatus().equals(JoiningStatus.IN)) {
                Joining targetUserJoining = joiningRepository.queryByAccountIdAndGroupId(accountId, groupId);
                if (targetUserJoining != null && targetUserJoining.getRole().equals(GroupRole.MEMBER) && targetUserJoining.getStatus().equals(JoiningStatus.IN)) {
                    try {
                        joiningRepository.updateRole(targetUserJoining.getId(), GroupRole.LEADER.name());
                        joiningRepository.updateRole(dbJoining.getId(), GroupRole.MEMBER.name());
                        result = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    }
                }
            }
        }
        return result;
    }

    public Group queryGroupById(Long id) {
        return groupRepository.queryById(id);
    }

    public Joining queryJoiningByAccountIdAndGroupId(Long accountId, Long groupId) {
        return joiningRepository.queryByAccountIdAndGroupId(accountId, groupId);
    }

    public GroupHeadListDto queryJoiningInGroups(Long size, Long havingSize) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        return GroupHeadListDto.builder()
                .total(groupRepository.countJoiningInGroups(myAccountId))
                .groupHeads(groupRepository.queryJoiningInGroupsByPage(myAccountId, size, havingSize))
                .build();
    }

    public List<AccountInfo> queryGroupMembers(Long groupId) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        Joining dbJoining = joiningRepository.queryByAccountIdAndGroupId(myAccountId, groupId);
        if (dbJoining != null && dbJoining.getStatus().equals(JoiningStatus.IN)) {
            List<Joining> groupJoinings = joiningRepository.queryGroupJoinings(groupId);
            return groupJoinings.stream()
                    .map(groupJoining -> AccountInfo.builder()
                            .id(String.valueOf(groupJoining.getAccountId()))
                            .username(groupJoining.getUsername())
                            .build())
                    .toList();
        }
        return new ArrayList<>();
    }

    public GeneralListDto<NotJoinInGroupDto> queryNotJoinInGroups(String searchName, Long size, Long havingSize) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        searchName = "%" + searchName + "%";
        return GeneralListDto.<NotJoinInGroupDto>builder()
                .total(groupRepository.countNotJoinInGroups(myAccountId, searchName))
                .items(groupRepository.queryNotJoinInGroupsByPage(myAccountId, searchName, size, havingSize)
                        .stream()
                        .map(group -> NotJoinInGroupDto.builder()
                                .id(String.valueOf(group.getId()))
                                .name(group.getName())
                                .description(group.getDescription())
                                .build())
                        .toList())
                .build();
    }

    public GeneralListDto<WaitingJoinInGroupDto> queryWaitingJoinInGroups(Long size, Long havingSize) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        return GeneralListDto.<WaitingJoinInGroupDto>builder()
                .total(groupRepository.countWaitingJoinInGroups(myAccountId))
                .items(groupRepository.queryWaitingJoinInGroupsByPage(myAccountId, size, havingSize))
                .build();
    }

    public GeneralListDto<JoinInRequestDto> queryJoinInRequests(Long groupId, Long size, Long havingSize) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        Joining dbJoining = joiningRepository.queryByAccountIdAndGroupId(myAccountId, groupId);
        if (dbJoining != null && dbJoining.getRole().equals(GroupRole.LEADER) && dbJoining.getStatus().equals(JoiningStatus.IN)) {
            return GeneralListDto.<JoinInRequestDto>builder()
                    .total(groupRepository.countJoinInRequests(groupId))
                    .items(groupRepository.queryJoinInRequestsByPage(groupId, size, havingSize))
                    .build();
        }
        return GeneralListDto.<JoinInRequestDto>builder().build();
    }

    public GeneralListDto<GroupMemberDto> queryGroupMemberV2(Long groupId, Long size, Long havingSize) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        Joining dbJoining = joiningRepository.queryByAccountIdAndGroupId(myAccountId, groupId);
        if (dbJoining != null && dbJoining.getStatus().equals(JoiningStatus.IN)) {
            return GeneralListDto.<GroupMemberDto>builder()
                    .total(groupRepository.countGroupMembers(myAccountId, groupId))
                    .items(groupRepository.queryGroupMembersByPage(myAccountId, groupId, size, havingSize).stream()
                            .map(GroupMemberDto::transfer).toList())
                    .build();
        }
        return GeneralListDto.<GroupMemberDto>builder().build();
    }

}
