package proj.fzy.campfire.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AuthenticationInfo implements Principal {
    private String id;
    private String username;
    private String token;
    private String csrfToken;
//    private String wsSessionId;
    private String email;
    private String status;
    private List<String> roleNames;

    @Override
    public String getName() {
        return id;
//        return wsSessionId;
    }
}
