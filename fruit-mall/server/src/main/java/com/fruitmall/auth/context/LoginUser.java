package com.fruitmall.auth.context;

import com.fruitmall.common.enums.UserTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 当前登录用户。由 AuthInterceptor 解析令牌后写入 UserContext。
 */
@Data
@AllArgsConstructor
public class LoginUser {

    /** 用户ID：系统用户为 sys_user.id，会员为 fm_member.id */
    private Long userId;

    /** 登录名 */
    private String username;

    /** 用户类型 */
    private UserTypeEnum userType;

    /** 角色编码列表，会员为空 */
    private List<String> roleCodes;

    /** 权限标识集合，会员为空 */
    private Set<String> permissions;

    /** 是否具备指定权限标识 */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /** 是否为后台用户 */
    public boolean isAdmin() {
        return UserTypeEnum.ADMIN == userType;
    }

    /** 是否为会员（消费者端） */
    public boolean isMember() {
        return UserTypeEnum.MEMBER == userType;
    }
}
