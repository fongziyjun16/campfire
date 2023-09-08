package proj.fzy.campfire.service.message.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.AccountGroupChat;
import proj.fzy.campfire.model.db.GroupChat;
import proj.fzy.campfire.model.db.GroupMessage;
import proj.fzy.campfire.model.dto.GroupChatHeadDto;
import proj.fzy.campfire.model.dto.GroupMessageDto;

import java.util.List;

@Repository
public interface GroupChatRepository {

    void insertGroupChat(Long id, Long groupId, String groupName);

    void insertAccountGroupChat(Long groupChatId, Long accountId, String username);

    void insertGroupMessage(Long id, Long groupChatId, Long creatorId, String content);

    void leaveGroupChat(Long accountId, Long groupId);

    void deleteGroupChatByGroupId(Long groupId);

    void deleteAccountGroupChatByGroupChatId(Long groupChatId);

    void deleteGroupMessageByGroupChatId(Long groupChatId);

    void updateLastReadTime(Long groupChatId, Long accountId);

    AccountGroupChat queryAccountGroupChatByIds(Long groupChatId, Long accountId);

    GroupChat queryGroupChatById(Long id);

    GroupChat queryGroupChatByGroupId(Long groupId);

    GroupMessage queryGroupMessageById(Long id);

    Long countGroupChatHeads(Long accountId);

    List<GroupChatHeadDto> queryGroupChatHeadsByPage(Long accountId, Long limit, Long offset);

    GroupChatHeadDto queryGroupChatHead(Long accountId, Long groupChatId);

    List<AccountGroupChat> queryGroupMembers(Long groupChatId);

    Long countGroupMessages(Long groupChatId);

    List<GroupMessageDto> queryGroupMessagesByPage(Long groupChatId, Long limit, Long offset);

    GroupMessageDto queryGroupMessage(Long groupChatId, Long groupMessageId);
}
