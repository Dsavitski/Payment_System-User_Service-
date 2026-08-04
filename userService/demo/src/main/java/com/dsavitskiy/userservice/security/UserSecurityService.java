package com.dsavitskiy.userservice.security;
import com.dsavitskiy.userservice.util.SecurityUtil;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userSecurityService")
public class UserSecurityService {

    public boolean isOwner(UUID userId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        return currentUserId.equals(userId);
    }
}