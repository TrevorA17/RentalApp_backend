package com.rentalapp.module.admin.service.impl;

import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.module.admin.entity.ModerationAction;
import com.rentalapp.module.admin.entity.ModerationActionType;
import com.rentalapp.module.admin.entity.ModerationTargetType;
import com.rentalapp.module.admin.repository.ModerationActionRepository;
import com.rentalapp.module.admin.service.IModerationAuditService;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModerationAuditService implements IModerationAuditService {
    private final ModerationActionRepository moderationActionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void recordStatusChange(
            ModerationTargetType targetType,
            String targetId,
            ModerationActionType actionType,
            String previousStatus,
            String newStatus,
            String reasonOrNote
    ) {
        String actorUserId = SecurityUtils.requireCurrentUserId();
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found."));

        ModerationAction action = new ModerationAction();
        action.setTargetType(targetType);
        action.setTargetId(targetId);
        action.setActionType(actionType);
        action.setPreviousStatus(previousStatus);
        action.setNewStatus(newStatus);
        action.setReasonOrNote(normalize(reasonOrNote));
        action.setActorUser(actor);
        moderationActionRepository.save(action);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
