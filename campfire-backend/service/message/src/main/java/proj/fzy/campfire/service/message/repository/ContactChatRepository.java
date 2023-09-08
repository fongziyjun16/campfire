package proj.fzy.campfire.service.message.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.AccountContactChat;
import proj.fzy.campfire.model.db.ContactChat;
import proj.fzy.campfire.model.db.ContactMessage;
import proj.fzy.campfire.model.dto.ContactChatHeadDto;
import proj.fzy.campfire.model.dto.ContactMessageDto;

import java.util.List;

@Repository
public interface ContactChatRepository {

    void insertContactChat(Long id);

    void insertAccountContactChat(Long contactChatId, Long accountId, String username);

    void insertContactMessage(Long id, Long contactChatId, Long creatorId, String content);

    void deleteContactChatByContactChatId(Long contactChatId);

    void deleteAccountContactChatByContactChatId(Long contactChatId);

    void deleteContactMessageByContactChatId(Long contactChatId);

    void updateLastReadTime(Long contactChatId, Long accountId);

    ContactChat queryContactChatByTwoAccountIds(Long alphaAccountId, Long betaAccountId);

    Long countContactChat(Long accountId);

    ContactChatHeadDto queryContactChatHead(Long accountId, Long contactChatId);

    List<ContactChatHeadDto> queryContactChatHeadsByPage(Long accountId, Long limit, Long offset);

    ContactMessage queryContactMessageById(Long id);

    Long countContactMessage(Long requesterId, Long targetId);

    List<ContactMessageDto> queryContactMessagesByPage(Long requesterId, Long targetId, Long limit, Long offset);
}
