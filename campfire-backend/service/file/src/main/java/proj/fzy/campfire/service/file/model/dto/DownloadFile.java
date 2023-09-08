package proj.fzy.campfire.service.file.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class DownloadFile {
    private String displayName;
    private Resource resource;
}
