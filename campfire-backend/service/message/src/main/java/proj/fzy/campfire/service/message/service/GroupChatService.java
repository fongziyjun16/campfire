package proj.fzy.campfire.service.message.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import proj.fzy.campfire.model.db.AccountGroupChat;
import proj.fzy.campfire.model.db.GroupChat;
import proj.fzy.campfire.model.db.GroupMessage;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.dto.*;
import proj.fzy.campfire.model.enums.GroupRole;
import proj.fzy.campfire.model.enums.JoiningStatus;
import proj.fzy.campfire.service.common.utils.DbIdUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.message.repository.GroupChatRepository;
import proj.fzy.campfire.servicecalling.relationship.GroupServiceCalling;

import java.util.List;
import java.util.Map;

@Service
public class GroupChatService {

    private final DbIdUtils dbIdUtils;
    private final GroupChatRepository groupChatRepository;
    private final GroupServiceCalling groupServiceCalling;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public GroupChatService(
            DbIdUtils dbIdUtils,
            GroupChatRepository groupChatRepository,
            GroupServiceCalling groupServiceCalling,
            SimpMessagingTemplate simpMessagingTemplate) {
        this.dbIdUtils = dbIdUtils;
        this.groupChatRepository = groupChatRepository;
        this.groupServiceCalling = groupServiceCalling;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public boolean startGroupChat(Long groupId, String groupName) {
        boolean result = false;
        try {
            groupChatRepository.insertGroupChat(dbIdUtils.getNextId(), groupId, groupName);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean joinGroupChat(Long groupId, Long accountId, String username) {
        boolean result = false;
        try {
            GroupChat dbGroupChat = groupChatRepository.queryGroupChatByGroupId(groupId);
            groupChatRepository.insertAccountGroupChat(dbGroupChat.getId(), accountId, username);
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(accountId),
                    "/queue/group-chat",
                    Map.of(
                            "groupChatId", String.valueOf(dbGroupChat.getId())
                    )
            );
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean makeMemberLeaveGroupChat(Long accountId, Long groupId) {
        boolean result = false;
        try {
            GroupChat dbGroupChat = groupChatRepository.queryGroupChatByGroupId(groupId);
            if (dbGroupChat != null) {
                CommonResponse<Joining> joiningResp = groupServiceCalling.queryJoiningById(ServiceUtils.getAccountIdFromSecurityContext(), groupId);
                if (joiningResp.getData() != null &&
                        joiningResp.getData().getRole().equals(GroupRole.LEADER) &&
                        joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
                    groupChatRepository.leaveGroupChat(accountId, groupId);
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean leaveGroupChat(Long groupId) {
        boolean result = false;
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        CommonResponse<Joining> joiningResp = groupServiceCalling.queryJoiningById(myAccountId, groupId);
        if (joiningResp != null &&
                joiningResp.getData().getRole().equals(GroupRole.MEMBER) &&
                joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
            try {
                groupChatRepository.leaveGroupChat(myAccountId, groupId);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Transactional
    public boolean deleteGroupChat(Long groupId) {
        boolean result = false;
        CommonResponse<Joining> joiningResp = groupServiceCalling.queryJoiningById(ServiceUtils.getAccountIdFromSecurityContext(), groupId);
        if (joiningResp.getData() != null &&
                joiningResp.getData().getRole().equals(GroupRole.LEADER) &&
                joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
            GroupChat dbGroupChat = groupChatRepository.queryGroupChatByGroupId(groupId);
            if (dbGroupChat != null) {
                try {
                    Long dbGroupChatId = dbGroupChat.getId();
                    groupChatRepository.deleteGroupMessageByGroupChatId(dbGroupChatId);
                    groupChatRepository.deleteAccountGroupChatByGroupChatId(dbGroupChatId);
                    groupChatRepository.deleteGroupChatByGroupId(groupId);
                    result = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                }
            }
        }
        return result;
    }

    @Transactional
    public boolean sendGroupMessage(Long groupChatId, String content) {
        boolean result = false;
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        GroupChat dbGroupChat = groupChatRepository.queryGroupChatById(groupChatId);
        AccountGroupChat dbAccountGroupChat = groupChatRepository.queryAccountGroupChatByIds(groupChatId, myAccountId);
        if (dbGroupChat != null && dbAccountGroupChat != null) {
            CommonResponse<Joining> joiningResp = groupServiceCalling.queryJoiningById(myAccountId, dbGroupChat.getGroupId());
            if (joiningResp.getData() != null && joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
                Long newGroupMessageId = dbIdUtils.getNextId();
                try {
                    groupChatRepository.insertGroupMessage(newGroupMessageId, groupChatId, myAccountId, content);
                    groupChatRepository.updateLastReadTime(groupChatId, myAccountId);
                    groupChatRepository.queryGroupMembers(groupChatId)
                            .forEach(groupMember -> simpMessagingTemplate.convertAndSendToUser(
                                    String.valueOf(groupMember.getAccountId()),
                                    "/queue/group-chat",
                                    Map.of(
                                            "groupChatId", String.valueOf(groupChatId),
                                            "groupMessageId", String.valueOf(newGroupMessageId)
                                    )
                            ));
                } catch (Exception e) {
                    e.printStackTrace();
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                }
                result = true;
            }
        }
        return result;
    }

    public GroupChatHeadListDto queryGroupGhatHeadsByPage(Long size, Long havingSize) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        return GroupChatHeadListDto.builder()
                .pageSize(size)
                .total(groupChatRepository.countGroupChatHeads(myAccountId))
                .groupChatHeads(groupChatRepository.queryGroupChatHeadsByPage(myAccountId, size, havingSize))
                .build();
    }

    public GroupChatHeadDto queryGroupChatHead(Long groupChatId) {
        return groupChatRepository.queryGroupChatHead(ServiceUtils.getAccountIdFromSecurityContext(), groupChatId);
    }

    public GroupMessageListDto queryGroupMessagesByPage(Long groupChatId, Long size, Long havingSize) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        GroupChat dbGroupChat = groupChatRepository.queryGroupChatById(groupChatId);
        if (dbGroupChat != null) {
            AccountGroupChat accountGroupChat = groupChatRepository.queryAccountGroupChatByIds(groupChatId, myAccountId);
            if (accountGroupChat != null) {
                groupChatRepository.updateLastReadTime(groupChatId, myAccountId);
                return GroupMessageListDto.builder()
                        .pageSize(size)
                        .total(groupChatRepository.countGroupMessages(groupChatId))
                        .groupMessages(groupChatRepository.queryGroupMessagesByPage(groupChatId, size, havingSize))
                        .build();
            }
        }
        return GroupMessageListDto.builder()
                .total(0L)
                .build();
    }

    public GroupMessageDto queryGroupMessage(Long groupMessageId) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        GroupMessage dbGroupMessage = groupChatRepository.queryGroupMessageById(groupMessageId);
        if (dbGroupMessage != null) {
            GroupChat dbGroupChat = groupChatRepository.queryGroupChatById(dbGroupMessage.getGroupChatId());
            if (dbGroupChat != null) {
                AccountGroupChat accountGroupChat = groupChatRepository.queryAccountGroupChatByIds(dbGroupChat.getId(), myAccountId);
                if (accountGroupChat != null) {
                    groupChatRepository.updateLastReadTime(dbGroupChat.getId(), myAccountId);
                    return groupChatRepository.queryGroupMessage(dbGroupChat.getId(), groupMessageId);
                }
            }
        }
        return null;
    }

}
