package proj.fzy.campfire.service.publish.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.Comment;
import proj.fzy.campfire.model.dto.CommentHeadDto;
import proj.fzy.campfire.model.enums.CommentStatus;
import proj.fzy.campfire.model.enums.CommentTargetType;

import java.util.List;

@Repository
public interface CommentRepository {

    void insert(Long id, Long creatorId, String username, Long targetId, CommentTargetType targetType, String content);

    void updateStatusById(Long id, String status);

    Comment queryById(Long id);

    Long countComments(Long targetId, List<CommentStatus> statuses);

    List<CommentHeadDto> queryCommentsByPage(Long targetId, List<CommentStatus> statuses, Long limit, Long offset);
}
