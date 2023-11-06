package com.app.core.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.app.core.entity.dto.cart.ResponseCartDTO;
import com.app.core.entity.model.CType;
import com.app.core.entity.model.CartModel;
import com.app.core.entity.model.UserModel;
import com.app.core.exception.CECartException;
import com.app.core.exception.CJNotFoundException;
import com.app.core.repository.CartRepository;
import com.app.core.security.entity.Role;
import com.app.core.service.OrderService;
import com.app.core.service.UserService;
import com.app.core.utils.Constant;
import com.app.core.utils.CustomCodeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class DefaultOrderService implements OrderService {
	
	private final CartRepository cartRepository;
	private final UserService userService;
	private final ModelMapper modelMapper;


	@Override
	@Transactional(readOnly = true)
	public Page<ResponseCartDTO> getAll(Pageable pageable) {
		Assert.notNull(pageable, "pageable cannot be null");
		UserModel userSession = userService.getSessionUser();
		Page<CartModel> cartsOrder = cartRepository.findCartsByUserAndCTypeAndUserRole(userSession.getId(), CType.ORDER, Role.USER, pageable);
		return  cartsOrder.map(cartOrder -> modelMapper.map(cartOrder, ResponseCartDTO.class));		
	}

	@Override
	@Transactional(readOnly = true)
	public List<ResponseCartDTO> getAll() {
		UserModel userSession = userService.getSessionUser();			
		List<CartModel> cartsOrder = cartRepository.findCartsByUserAndCTypeAndUserRole(userSession.getId(), CType.ORDER, Role.USER);
		return  cartsOrder.stream().map(cartOrder -> modelMapper.map(cartOrder, ResponseCartDTO.class)).collect(Collectors.toList());
	}
	
	@Override
	public ResponseCartDTO getById(String id) {
		
		if( !Pattern.compile(Constant.UUID_REGEX).matcher(id).matches()) {
			throw new CJNotFoundException(CustomCodeException.CODE_404, "Id [ "+id+ " ] does not comply with the correct format");
		}
		
		UserModel userSession = userService.getSessionUser();
		CartModel order = cartRepository.findByCartIdAndUserIdAndCTypeAndUserRole(UUID.fromString(id), userSession.getId(), CType.ORDER, Role.USER).stream().findFirst().orElse(null);
		
		if(ObjectUtils.isEmpty(order)) {
			throw new CJNotFoundException(CustomCodeException.CODE_404, "Order with id [ "+id+ " ] not found");
		}
		
		return  modelMapper.map(order, ResponseCartDTO.class);
	}
	
	@Override
	public CartModel placeOrder() {
		
		if(userService.isAnonymousSession()) {			
			throw new CECartException(CustomCodeException.CODE_500, "The order cannot be placed in an anonymous session");
		}
		
		UserModel userSession = userService.getSessionUser();			
		CartModel cartSession = cartRepository.findCartAvailableByUser(userSession.getId(), CType.CART).stream().findFirst().orElse(null);
		
		if(ObjectUtils.isEmpty(cartSession)) {
			throw new CECartException(CustomCodeException.CODE_500, "No shopping cart found for this user in session, user id "+userSession.getId());
		}
		
		if(CollectionUtils.isEmpty(cartSession.getEntries())) {
			throw new CECartException(CustomCodeException.CODE_500, "No entries were found for the shopping cart in user in session");
		}
		
		cartSession.setType(CType.ORDER);
		
		CartModel orderGenerate = cartRepository.save(cartSession);
		log.info("[Commerce]: order generated from shopping cart with id "+orderGenerate.getId());
		return  orderGenerate;
	}	

}
