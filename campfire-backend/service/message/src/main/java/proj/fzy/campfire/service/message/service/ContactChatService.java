package proj.fzy.campfire.service.message.service;

import cn.hutool.core.date.DateUtil;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import proj.fzy.campfire.model.db.Contact;
import proj.fzy.campfire.model.db.ContactChat;
import proj.fzy.campfire.model.db.ContactMessage;
import proj.fzy.campfire.model.dto.*;
import proj.fzy.campfire.model.enums.ContactStatus;
import proj.fzy.campfire.service.common.utils.DbIdUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.message.repository.ContactChatRepository;
import proj.fzy.campfire.servicecalling.relationship.ContactServiceCalling;

import java.util.List;
import java.util.Map;

@Service
public class ContactChatService {

    private final DbIdUtils dbIdUtils;
    private final ContactChatRepository contactChatRepository;
    private final ContactServiceCalling contactServiceCalling;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ContactChatService(
            DbIdUtils dbIdUtils,
            ContactChatRepository contactChatRepository,
            ContactServiceCalling contactServiceCalling,
            SimpMessagingTemplate simpMessagingTemplate) {
        this.dbIdUtils = dbIdUtils;
        this.contactChatRepository = contactChatRepository;
        this.contactServiceCalling = contactServiceCalling;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Transactional
    public boolean startNewContactChat(Long targetId, String targetUsername) {
        boolean result = false;
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        ContactChat dbContactChat = contactChatRepository.queryContactChatByTwoAccountIds(myAccountId, targetId);
        if (dbContactChat == null) {
            CommonResponse<Contact> queryContactResp = contactServiceCalling.queryPersonalContactWith(targetId);
            if (queryContactResp.getData() != null && queryContactResp.getData().getStatus().equals(ContactStatus.ACCEPT)) {
                Long newContactChatId = dbIdUtils.getNextId();
                try {
                    contactChatRepository.insertContactChat(newContactChatId);
                    contactChatRepository.insertAccountContactChat(newContactChatId, myAccountId, ServiceUtils.getUsernameFromSecurityContext());
                    contactChatRepository.insertAccountContactChat(newContactChatId, targetId, targetUsername);
                    simpMessagingTemplate.convertAndSendToUser(
                            String.valueOf(targetId),
                            "/queue/contact-chat",
                            Map.of(
                                    "id", String.valueOf(newContactChatId)
                            )
                    );
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
    public boolean deleteContact(Long targetId) {
        boolean result = false;
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        try {
            ContactChat contactChat = contactChatRepository.queryContactChatByTwoAccountIds(myAccountId, targetId);
            Long contactChatId = contactChat.getId();
            contactChatRepository.deleteContactMessageByContactChatId(contactChatId);
            contactChatRepository.deleteAccountContactChatByContactChatId(contactChatId);
            contactChatRepository.deleteContactChatByContactChatId(contactChatId);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return result;
    }

    @Transactional
    public boolean sendMessage(Long targetId, String content) {
        boolean result = false;
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        if (myAccountId.equals(targetId)) {
            return result;
        }
        ContactChat dbContactChat = contactChatRepository.queryContactChatByTwoAccountIds(myAccountId, targetId);
        if (dbContactChat != null) {
            CommonResponse<Contact> queryContactResp = contactServiceCalling.queryPersonalContactWith(targetId);
            if (queryContactResp.getData() != null && queryContactResp.getData().getStatus().equals(ContactStatus.ACCEPT)) {
                try {
                    Long newContactMessageId = dbIdUtils.getNextId();
                    contactChatRepository.insertContactMessage(newContactMessageId, dbContactChat.getId(), myAccountId, content);
                    contactChatRepository.updateLastReadTime(dbContactChat.getId(), myAccountId);
                    simpMessagingTemplate.convertAndSendToUser(
                            String.valueOf(myAccountId),
                            "/queue/contact-chat",
                            Map.of(
                                    "id", String.valueOf(dbContactChat.getId()),
                                    "contactMessageId", String.valueOf(newContactMessageId)
                            )
                    );
                    simpMessagingTemplate.convertAndSendToUser(
                            String.valueOf(targetId),
                            "/queue/contact-chat",
                            Map.of(
                                    "id", String.valueOf(dbContactChat.getId()),
                                    "contactMessageId", String.valueOf(newContactMessageId)
                            )
                    );
                    result = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                }
            }
        }
        return result;
    }

    public ContactChatHeadListDto queryFirstBatchOfContactHeads(Long number) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        List<ContactChatHeadDto> contactChatHeads = contactChatRepository.queryContactChatHeadsByPage(myAccountId, number, 0L);
        return ContactChatHeadListDto.builder()
                .total(contactChatRepository.countContactChat(myAccountId))
                .pageSize((long) contactChatHeads.size())
                .contactChatHeads(contactChatHeads)
                .build();
    }

    public ContactChatHeadListDto queryMoreContactHeads(Long size, Long havingSize) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        List<ContactChatHeadDto> contactChatHeads = contactChatRepository.queryContactChatHeadsByPage(myAccountId, size, havingSize);
        return ContactChatHeadListDto.builder()
                .total(contactChatRepository.countContactChat(myAccountId))
                .pageSize((long) contactChatHeads.size())
                .contactChatHeads(contactChatHeads)
                .build();
    }

    public ContactChatHeadDto queryContactHead(Long contactChatId) {
        return contactChatRepository.queryContactChatHead(ServiceUtils.getAccountIdFromSecurityContext(), contactChatId);
    }

    public ContactMessageDto queryContactMessageById(Long contactMessageId) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        ContactMessage dbContactMessage = contactChatRepository.queryContactMessageById(contactMessageId);
        ContactMessageDto contactMessageDto = ContactMessageDto.builder()
                .id(String.valueOf(dbContactMessage.getId()))
                .creatorId(String.valueOf(dbContactMessage.getCreatorId()))
                .content(dbContactMessage.getContent())
                .createdTime(DateUtil.format(dbContactMessage.getCreatedTime(), "yyyy-MM-dd HH:mm:ss"))
                .build();
        if (dbContactMessage.getCreatorId().equals(myAccountId)) {
            try {
                contactChatRepository.updateLastReadTime(dbContactMessage.getContactChatId(), myAccountId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return contactMessageDto;
        } else {
            ContactChat dbContactChat = contactChatRepository.queryContactChatByTwoAccountIds(myAccountId, dbContactMessage.getCreatorId());
            if (dbContactChat != null && dbContactChat.getId().equals(dbContactMessage.getContactChatId())) {
                try {
                    contactChatRepository.updateLastReadTime(dbContactMessage.getContactChatId(), myAccountId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return contactMessageDto;
            }
        }
        return null;
    }

    public ContactMessageListDto queryFirstBatchOfContactMessages(Long targetId, Long size) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        ContactChat dbContactChat = contactChatRepository.queryContactChatByTwoAccountIds(myAccountId, targetId);
        if (dbContactChat != null) {
            try {
                contactChatRepository.updateLastReadTime(dbContactChat.getId(), myAccountId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<ContactMessageDto> contactMessages = contactChatRepository.queryContactMessagesByPage(myAccountId, targetId, size, 0L);
            return ContactMessageListDto.builder()
                    .total(contactChatRepository.countContactMessage(myAccountId, targetId))
                    .pageSize((long) contactMessages.size())
                    .contactMessages(contactMessages)
                    .build();
        }
        return ContactMessageListDto.builder().build();
    }

    public ContactMessageListDto queryMoreContactMessages(Long targetId, Long size, Long havingSize) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        List<ContactMessageDto> contactMessages = contactChatRepository.queryContactMessagesByPage(myAccountId, targetId, size, havingSize);
        return ContactMessageListDto.builder()
                .total(contactChatRepository.countContactMessage(myAccountId, targetId))
                .pageSize((long) contactMessages.size())
                .contactMessages(contactMessages)
                .build();
    }

}
