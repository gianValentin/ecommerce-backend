package com.app.core.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.app.core.entity.dto.cart.ResponseCartDTO;
import com.app.core.entity.model.CartModel;
import com.app.core.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultNotificationService implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ModelMapper modelMapper;

    @Override
    public void notifyOrderGenerated(CartModel order) {
        ResponseCartDTO dto = modelMapper.map(order, ResponseCartDTO.class);
        String username = order.getUser().getUsername();
        messagingTemplate.convertAndSendToUser(username, "/queue/orders", dto);
        log.info("[Commerce] WebSocket notification sent for order {}", order.getId());
    }
}
