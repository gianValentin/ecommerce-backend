package com.app.core.service.impl;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.app.core.entity.model.CType;
import com.app.core.entity.model.CartModel;
import com.app.core.entity.model.EntryModel;
import com.app.core.entity.model.PriceModel;
import com.app.core.entity.model.ProductModel;
import com.app.core.entity.model.UserModel;
import com.app.core.exception.CECartException;
import com.app.core.exception.CJNotFoundException;
import com.app.core.repository.CartRepository;
import com.app.core.security.entity.Role;
import com.app.core.service.CartService;
import com.app.core.service.ProductService;
import com.app.core.service.UserService;
import com.app.core.utils.Constant;
import com.app.core.utils.CustomCodeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class DefaultCartService implements CartService{
	
	private final CartRepository cartRepository;
	private final UserService userService;
	private final ProductService productService;
	
	@Override
	@Transactional
	public CartModel save(CartModel cartModel) {
		Assert.notNull(cartModel, "cart cannot be null");
		
		return cartRepository.save(cartModel);
	}
	
	@Override
	public CartModel getCart() {		
		
		// The cart generator always return any cart
		return generateCart(null);
	}
	
	@Override
	public CartModel getCart(String uuid) {		
		
		// If the UUID does not comply with the format, null is sent to the cart generator
		if(Pattern.compile(Constant.UUID_REGEX).matcher(uuid).matches()) {
			return generateCart(UUID.fromString(uuid));
		} else {
			return generateCart(null);
		}
	}	
	
	private CartModel generateCart(UUID uuid) {		
		
		// If user session is not anonymous, get cart by id or create new cart for user session
		if(!userService.isAnonymousSession()) {			
			UserModel userSession = userService.getSessionUser();			
			
			// If cart session is null, create new cart for user in session
			CartModel cartSession = cartRepository.findCartAvailableByUser(userSession.getId(), CType.CART).stream().findFirst().orElse(null);			
			if(ObjectUtils.isEmpty(cartSession)) {				
				CartModel newCart = CartModel.builder()
						.user(userSession)
						.build();				
				calculateCart(newCart);				
				
				cartSession = cartRepository.save(newCart);				
				log.info("[Commerce] Create cart for user session success");
			}
						
			// If exits anonymous cart for UUID with anonymous user role, merge anonymous cart with  user cart in session 
			CartModel cartAnonymousDb = cartRepository.findCartByIdAndUserRole(uuid, CType.CART,Role.ANONYMOUS).stream().findFirst().orElse(null);
			if(ObjectUtils.isNotEmpty(cartAnonymousDb)) {
				mergeCart(cartSession, cartAnonymousDb);
				calculateCart(cartSession);				
				
				cartSession = cartRepository.save(cartSession);
				log.info("[Commerce] Merge and save cart session with cart anonymous success");
			}
			
			return cartSession;			
		}
		
		// If user session is anonymous, get cart anonymous or create new cart for user anonymous 
		CartModel cartAnonymousDb = cartRepository.findCartByIdAndUserRole(uuid, CType.CART, Role.ANONYMOUS).stream().findFirst().orElse(null);		
		if(ObjectUtils.isNotEmpty(cartAnonymousDb)) {
			return cartAnonymousDb;
		}
		
		UserModel user = userService.generateUserAnonymous();		
		cartAnonymousDb = CartModel.builder()
				.user(user)
				.build();
		calculateCart(cartAnonymousDb);		
		return  cartRepository.save(cartAnonymousDb);
	}
	
	@Override
	public void validateIfExistsCartById(String uuid) {
		
		// Throw not found exception if validate cart not found cart by UUID
		if(!existsCartByIdResult(uuid)) {
			throw new CJNotFoundException(CustomCodeException.CODE_404, "Cart with id [ "+uuid+ " ] not found");
		}
	}

	@Override
	public Boolean existsCartById(String uuid) {
		
		// Return  boolean if validate cart not found cart by UUID
		return existsCartByIdResult(uuid);
	}
	
	Boolean existsCartByIdResult(String uuid) {
		if(!Pattern.compile(Constant.UUID_REGEX).matcher(uuid).matches()) {
			return false;	
		}			
		
		CartModel cartAnonymousDb = cartRepository.findCartByIdAndUserRole(UUID.fromString(uuid), CType.CART, Role.ANONYMOUS)
				.stream()
				.findFirst()
				.orElse(null);		
		if(ObjectUtils.isEmpty(cartAnonymousDb) && userService.isAnonymousSession()) {
			return false;
		}		
		
		return true;
	}

	@Override
	@Transactional
	public void calculateCart(CartModel cartModel) {
		Assert.notNull(cartModel, "Cart cannot be null");
		
		AtomicReference<Double> subTotal = new AtomicReference<>(Double.valueOf(0.0f));
		AtomicReference<Double> discount = new AtomicReference<>(Double.valueOf(0.0f));
		AtomicReference<Double> total = new AtomicReference<>(Double.valueOf(0.0f));		
		
		// If cart is empty, set sub total, discount and total in zero
		if(CollectionUtils.isEmpty(cartModel.getEntries())) {
			cartModel.setSubTotal(subTotal.get());
			cartModel.setDiscount(discount.get());
			cartModel.setTotal(total.get());
			return ;
		}		
		
		cartModel.getEntries().forEach(entry -> {
			// Validate
			if(ObjectUtils.isEmpty(entry.getProduct())){
				return;
			}
			
			ProductModel product = entry.getProduct();
			
			if(ObjectUtils.isEmpty(product.getPrice())){
				return;
			}
			
			// Calculate
			PriceModel price = product.getPrice();			
			Double entryTotal =price.getPrice() * entry.getAmount();
			
			// set entry total
			entry.setTotal(entryTotal);
			
			subTotal.set(subTotal.get()+entryTotal);						
			log.info("[Commerce] Calculate cart success");
		});
		
		// TODO get Discount
		
		total.set(subTotal.get()-discount.get());
		
		cartModel.setSubTotal(subTotal.get());
		cartModel.setDiscount(discount.get());
		cartModel.setTotal(total.get());
	}

	@Override
	public CartModel addToCard(CartModel cart, String codeProduct, Integer amount) {
		Assert.notNull(cart, "cart cannot be null for addToCard");
		Assert.notNull(codeProduct, "codeProduct cannot be null for addToCard");
		Assert.notNull(amount, "amount cannot be null for addToCard");
		
		ProductModel product = productService.getProductByCode(codeProduct);
		
		if(ObjectUtils.isEmpty(product) || ObjectUtils.isEmpty(product.getPrice())) {
			throw new CECartException(CustomCodeException.CODE_500, "Product or Price not found");
		}
		
		EntryModel newEntry = EntryModel.builder()
				.amount(amount)
				.product(product)				
				.build();

		cart.addEntry(newEntry);
		
		calculateCart(cart);		
		CartModel cartSaved = cartRepository.save(cart);		
		return  cartSaved;
	}

	@Override
	@Transactional
	public void mergeCart(CartModel currentCart, CartModel oldCart) {
		Assert.notNull(currentCart, "current cart cannot be null");
		Assert.notNull(oldCart, "old cart cannot be null");
		
		if(!CollectionUtils.isEmpty(oldCart.getEntries())) {
			oldCart.getEntries().forEach(entry -> {
				currentCart.getEntries().add(entry);				
			});			
		}		
		
		cartRepository.delete(oldCart);	 
		log.info("[Commerce] Merge cart success");
	}

	@Override
	public CartModel removeEntry(CartModel cart, Long entryId) {
		Assert.notNull(cart, "cart cannot be null");
		Assert.notNull(entryId, "entry id cannot be null");
		
		if(CollectionUtils.isEmpty(cart.getEntries())) {
			throw new CECartException(CustomCodeException.CODE_500, "No entries found to delete");
		}
		
		EntryModel entryFound = cart.getEntries()
				.stream()
				.filter(entry -> entry.getId() == entryId)
				.findFirst()
				.orElseThrow(() -> new CJNotFoundException(CustomCodeException.CODE_404, "Entry with id ["+entryId+ "] not found"));	
		
		cart.removeEntry(entryFound);
		
		calculateCart(cart);		
		return  cartRepository.save(cart);
	}	

}
