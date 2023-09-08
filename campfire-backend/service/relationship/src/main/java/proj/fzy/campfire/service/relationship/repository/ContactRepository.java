package proj.fzy.campfire.service.relationship.repository;

import org.apache.ibatis.annotations.Flush;
import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.Contact;
import proj.fzy.campfire.model.enums.ContactQueryType;

import java.util.List;

@Repository
public interface ContactRepository {

    void insert(Long id, Long sourceId, String sourceUsername, Long targetId, String targetUsername, String comment);

    @Flush
    void updateStatus(Long id, String status);

    void deleteById(Long id);

    Contact queryByAccountIds(Long accountId1, Long accountId2);

    Contact queryById(Long id);

    Long countQueryContacts(Long accountId, String queryType);

    List<Contact> queryQueryContactsByPage(Long accountId, String queryType, Long limit, Long offset);
}
