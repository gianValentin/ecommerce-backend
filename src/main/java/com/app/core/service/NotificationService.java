package com.app.core.service;

import com.app.core.entity.model.CartModel;

public interface NotificationService {
    void notifyOrderGenerated(CartModel order);
}
