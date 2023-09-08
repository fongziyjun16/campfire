package proj.fzy.campfire.service.relationship.service;

import cn.hutool.core.date.DateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import proj.fzy.campfire.model.db.Contact;
import proj.fzy.campfire.model.dto.AccountInfo;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.dto.ContactDto;
import proj.fzy.campfire.model.dto.GeneralListDto;
import proj.fzy.campfire.model.enums.ContactQueryType;
import proj.fzy.campfire.model.enums.ContactStatus;
import proj.fzy.campfire.service.common.utils.DbIdUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.relationship.repository.ContactRepository;
import proj.fzy.campfire.servicecalling.auth.AccountServiceCalling;
import proj.fzy.campfire.servicecalling.message.ContactChatServiceCalling;

import java.util.List;

@Service
public class ContactService {

    private final DbIdUtils dbIdUtils;
    private final ContactRepository contactRepository;
    private final AccountServiceCalling accountServiceCalling;
    private final ContactChatServiceCalling contactChatServiceCalling;

    public ContactService(
            DbIdUtils dbIdUtils,
            ContactRepository contactRepository,
            AccountServiceCalling accountServiceCalling,
            ContactChatServiceCalling contactChatServiceCalling) {
        this.dbIdUtils = dbIdUtils;
        this.contactRepository = contactRepository;
        this.accountServiceCalling = accountServiceCalling;
        this.contactChatServiceCalling = contactChatServiceCalling;
    }

    public boolean buildContact(Long targetId, String comment) {
        boolean result = false;
        Long sourceId = ServiceUtils.getAccountIdFromSecurityContext();
        if (!sourceId.equals(targetId)) {
            try {
                Contact dbContact = contactRepository.queryByAccountIds(sourceId, targetId);
                if (dbContact == null) {
                    CommonResponse<AccountInfo> targetAccountInfoResp = accountServiceCalling.queryById(targetId);
                    if (targetAccountInfoResp.getCode().equals(HttpStatus.OK.value())) {
                        contactRepository.insert(dbIdUtils.getNextId(),
                                sourceId, ServiceUtils.getUsernameFromSecurityContext(),
                                targetId, targetAccountInfoResp.getData().getUsername(),
                                comment);
                        result = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean confirmContact(Long contactId, boolean accept) {
        boolean result = false;
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        Contact dbContact = contactRepository.queryById(contactId);
        if (dbContact != null && dbContact.getStatus().equals(ContactStatus.WAITING) && dbContact.getTargetId().equals(myAccountId)) {
            try {
                if (accept) {
                    contactRepository.updateStatus(dbContact.getId(), ContactStatus.ACCEPT.name());
                    try {
                        Long dbContactSourceId = dbContact.getSourceId();
                        contactChatServiceCalling.startNewContactChat(
                                dbContactSourceId,
                                accountServiceCalling.queryById(dbContactSourceId).getData().getUsername());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    contactRepository.deleteById(dbContact.getId());
                }
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Transactional
    public boolean breakContact(Long contactId) {
        boolean result = false;
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        Contact dbContact = contactRepository.queryById(contactId);
        if (dbContact != null && dbContact.getStatus().equals(ContactStatus.ACCEPT) &&
                (dbContact.getSourceId().equals(myAccountId) || dbContact.getTargetId().equals(myAccountId))) {
            try {
                contactRepository.deleteById(dbContact.getId());
                CommonResponse<Void> deleteContactResp =
                        contactChatServiceCalling.deleteContact(dbContact.getSourceId().equals(myAccountId) ? dbContact.getTargetId() : myAccountId);
                if (!deleteContactResp.getCode().equals(HttpStatus.OK.value())) {
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

    public Contact queryPersonalContactWith(Long targetId) {
        return contactRepository.queryByAccountIds(
                ServiceUtils.getAccountIdFromSecurityContext(),
                targetId
        );
    }

    public GeneralListDto<ContactDto> queryContactsByQueryType(ContactQueryType queryType, Long size, Long havingSize) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        return GeneralListDto.<ContactDto>builder()
                .total(contactRepository.countQueryContacts(myAccountId, queryType.name()))
                .items(contactRepository.queryQueryContactsByPage(myAccountId, queryType.name(), size, havingSize)
                        .stream()
                        .map(ContactDto::transfer)
                        .toList())
                .build();
    }

}
