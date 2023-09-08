package proj.fzy.campfire.service.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.db.Role;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.service.auth.service.RoleService;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;

import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {

    private final JwtUtils jwtUtils;
    private final RoleService roleService;

    public RoleController(JwtUtils jwtUtils, RoleService roleService) {
        this.jwtUtils = jwtUtils;
        this.roleService = roleService;
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> create(@RequestParam String name, @RequestParam String description) {
        return roleService.create(name, description) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Duplicate Role Name");
    }

    @PreAuthorize("@accountStatusChecker.anyVerifiedOrUnVerified()")
    @GetMapping("/self")
    public CommonResponse<List<String>> querySelfRoles(HttpServletRequest request) {
        return CommonResponse.simpleSuccessWithData(
                roleService.queryRolesByAccountId(jwtUtils.getId(ServiceUtils.extraTokenFromRequest(request)))
                        .stream().map(Role::getName).toList());
    }

}
