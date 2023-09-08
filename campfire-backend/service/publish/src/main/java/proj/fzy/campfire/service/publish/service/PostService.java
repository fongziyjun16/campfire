package proj.fzy.campfire.service.publish.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import proj.fzy.campfire.model.db.Group;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.db.Post;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.dto.PostHeadListDto;
import proj.fzy.campfire.model.enums.GroupRole;
import proj.fzy.campfire.model.enums.GroupStatus;
import proj.fzy.campfire.model.enums.JoiningStatus;
import proj.fzy.campfire.model.enums.PostStatus;
import proj.fzy.campfire.service.common.utils.DbIdUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.publish.repository.PostRepository;
import proj.fzy.campfire.servicecalling.relationship.GroupServiceCalling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PostService {
    private final DbIdUtils dbIdUtils;
    private final PostRepository postRepository;
    private final GroupServiceCalling groupServiceCalling;

    public PostService(DbIdUtils dbIdUtils, PostRepository postRepository, GroupServiceCalling groupServiceCalling) {
        this.dbIdUtils = dbIdUtils;
        this.postRepository = postRepository;
        this.groupServiceCalling = groupServiceCalling;
    }

    public boolean create(Long groupId, String title, String content) {
        boolean result = false;
        try {
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            CommonResponse<Joining> queryJoiningResponse = groupServiceCalling.queryJoiningById(myAccountId, groupId);
            if (queryJoiningResponse.getCode().equals(HttpStatus.OK.value()) &&
                    queryJoiningResponse.getData().getStatus().equals(JoiningStatus.IN)) {
                CommonResponse<Group> queryGroupResponse = groupServiceCalling.queryGroupById(groupId);
                if (queryGroupResponse.getCode().equals(HttpStatus.OK.value()) &&
                        queryGroupResponse.getData().getStatus().equals(GroupStatus.ACTIVE)) {
                    postRepository.insert(dbIdUtils.getNextId(), myAccountId, groupId, ServiceUtils.getUsernameFromSecurityContext(), title, content);
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean updatePostStatus(Long postId, String status) {
        boolean result = false;
        Post dbPost = postRepository.queryById(postId);
        if (dbPost != null) {
            CommonResponse<Joining> joiningResp =
                    groupServiceCalling.queryJoiningById(ServiceUtils.getAccountIdFromSecurityContext(), dbPost.getGroupId());
            if (joiningResp.getData() != null && joiningResp.getData().getRole().equals(GroupRole.LEADER)) {
                try {
                    postRepository.updatePostStatusById(postId, status);
                    result = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public PostHeadListDto queryGroupPosts(Long groupId, Long size, Long havingSize) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        CommonResponse<Joining> joiningResp = groupServiceCalling.queryJoiningById(myAccountId, groupId);
        if (joiningResp.getData() != null && joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
            List<PostStatus> statuses = new ArrayList<>(Arrays.asList(PostStatus.OPEN, PostStatus.CLOSE));
            if (joiningResp.getData().getRole().equals(GroupRole.LEADER)) {
                statuses.add(PostStatus.HIDE);
            }
            return PostHeadListDto.builder()
                    .total(postRepository.countGroupPosts(groupId, statuses))
                    .postHeads(postRepository.queryGroupPostsByPage(groupId, statuses, size, havingSize))
                    .build();
        }
        return null;
    }

}
