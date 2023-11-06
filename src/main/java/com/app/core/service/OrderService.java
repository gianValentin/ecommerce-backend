package com.app.core.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.core.entity.dto.cart.ResponseCartDTO;
import com.app.core.entity.model.CartModel;

public interface OrderService {
	Page<ResponseCartDTO> getAll(Pageable pageable);
	List<ResponseCartDTO> getAll();
	ResponseCartDTO getById(String id);
	CartModel placeOrder();
}
