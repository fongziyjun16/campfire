package proj.fzy.campfire.service.publish.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import proj.fzy.campfire.model.db.Comment;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.db.Note;
import proj.fzy.campfire.model.db.Post;
import proj.fzy.campfire.model.dto.CommentHeadListDto;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.enums.*;
import proj.fzy.campfire.service.common.utils.DbIdUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.publish.repository.CommentRepository;
import proj.fzy.campfire.service.publish.repository.NoteRepository;
import proj.fzy.campfire.service.publish.repository.PostRepository;
import proj.fzy.campfire.servicecalling.relationship.GroupServiceCalling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CommentService {

    private final DbIdUtils dbIdUtils;
    private final CommentRepository commentRepository;
    private final NoteRepository noteRepository;
    private final PostRepository postRepository;
    private final GroupServiceCalling groupServiceCalling;

    public CommentService(DbIdUtils dbIdUtils,
                          CommentRepository commentRepository, NoteRepository noteRepository, PostRepository postRepository,
                          GroupServiceCalling groupServiceCalling) {
        this.dbIdUtils = dbIdUtils;
        this.commentRepository = commentRepository;
        this.noteRepository = noteRepository;
        this.postRepository = postRepository;
        this.groupServiceCalling = groupServiceCalling;
    }

    public boolean createComment(Long targetId, String targetType, String content) {
        boolean result = false;
        try {
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            String myUsername = ServiceUtils.getUsernameFromSecurityContext();
            if (targetType.equals(CommentTargetType.NOTE.name())) {
                Note dbNote = noteRepository.queryById(targetId);
                if (dbNote != null && dbNote.getVisibility().equals(NoteVisibility.PUBLIC)) {
                    commentRepository.insert(dbIdUtils.getNextId(), myAccountId, myUsername, targetId, CommentTargetType.NOTE, content);
                    result = true;
                }
            } else if (targetType.equals(CommentTargetType.POST.name())){
                Post dbPost = postRepository.queryById(targetId);
                if (dbPost != null && dbPost.getStatus().equals(PostStatus.OPEN)) {
                    CommonResponse<Joining> joiningCommonResponse = groupServiceCalling.queryJoiningById(myAccountId, dbPost.getGroupId());
                    if (joiningCommonResponse.getCode().equals(HttpStatus.OK.value()) &&
                            joiningCommonResponse.getData().getStatus().equals(JoiningStatus.IN)) {
                        commentRepository.insert(dbIdUtils.getNextId(), myAccountId, myUsername, targetId, CommentTargetType.POST, content);
                        result = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean updateCommentStatus(Long commentId, String newStatus) {
        boolean result = false;
        try {
            Comment dbComment = commentRepository.queryById(commentId);
            if (dbComment != null) {
                Post dbPost = postRepository.queryById(dbComment.getTargetId());
                if (dbPost != null) {
                    Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
                    CommonResponse<Joining> joiningResp = groupServiceCalling.queryJoiningById(myAccountId, dbPost.getGroupId());
                    if (joiningResp.getData() != null && joiningResp.getData().getRole().equals(GroupRole.LEADER)) {
                        commentRepository.updateStatusById(commentId, newStatus);
                        result = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public CommentHeadListDto queryComments(Long targetId, Long size, Long havingSize) {
        Post dbPost = postRepository.queryById(targetId);
        if (dbPost != null) {
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            List<CommentStatus> statuses = new ArrayList<>(List.of(CommentStatus.OPEN));
            CommonResponse<Joining> joiningResp = groupServiceCalling.queryJoiningById(myAccountId, dbPost.getGroupId());
            if (joiningResp.getData() != null && joiningResp.getData().getRole().equals(GroupRole.LEADER)) {
                statuses.add(CommentStatus.HIDE);
            }
            return CommentHeadListDto.builder()
                    .total(commentRepository.countComments(targetId, statuses))
                    .commentHeads(commentRepository.queryCommentsByPage(targetId, statuses, size, havingSize))
                    .build();
        }
        return CommentHeadListDto.builder().build();
    }

}
