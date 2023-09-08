package proj.fzy.campfire.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PostHeadListDto {
    private Long pageNo;
    private Long pageSize;
    private Long total;
    private Long totalPage;
    private List<PostHeadDto> postHeads;
}
