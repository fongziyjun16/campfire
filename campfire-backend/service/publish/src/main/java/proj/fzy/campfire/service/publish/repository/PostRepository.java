package proj.fzy.campfire.service.publish.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.Post;
import proj.fzy.campfire.model.dto.PostHeadDto;
import proj.fzy.campfire.model.enums.PostStatus;

import java.util.List;

@Repository
public interface PostRepository {
    void insert(Long id, Long creatorId, Long groupId, String username, String title, String content);

    void updatePostStatusById(Long postId, String status);

    Post queryById(Long id);

    Long countGroupPosts(Long groupId, List<PostStatus> statuses);

    List<PostHeadDto> queryGroupPostsByPage(Long groupId, List<PostStatus> statuses, Long limit, Long offset);
}
