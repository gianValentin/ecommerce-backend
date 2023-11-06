package com.app.core.service;

import com.app.core.entity.model.CartModel;

public interface CartService {
	CartModel save(CartModel cart);
	CartModel getCart();
	CartModel getCart(String uuid);	
	Boolean existsCartById(String uuid);
	void validateIfExistsCartById(String uuid);
	void calculateCart(CartModel cartModel);	
	CartModel addToCard(CartModel cart, String codeProduct, Integer amount);
	void mergeCart(CartModel currentCart, CartModel oldCart);
	CartModel removeEntry(CartModel cart ,Long entryId);
}
