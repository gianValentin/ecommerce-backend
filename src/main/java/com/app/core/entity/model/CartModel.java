package com.app.core.entity.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "cart")
@EqualsAndHashCode(of = {"id"})
public class CartModel {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(name = "sub_total")
	private Double subTotal;
	private Double discount;
	private Double total;
	
	@Enumerated(EnumType.STRING)
	@Builder.Default
	protected CType type = CType.CART;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_at")
	private Date createAt;

	@Builder.Default
	@JsonIgnoreProperties(value = {"cart"}, allowSetters = true)
	@OneToMany( cascade = {CascadeType.ALL, CascadeType.MERGE}, fetch = FetchType.LAZY, orphanRemoval = true)
	private Set<EntryModel> entries = new HashSet<>();
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id", nullable = false)
	private UserModel user;

	@PrePersist
	private void prePersist() {
		this.createAt = new Date();
	}
	
	public void addEntry(EntryModel entry) {
		this.entries.add(entry);		
	}
	
	public void removeEntry(EntryModel entry) {
		this.entries.remove(entry);	
	}
	
	public void removeAllEntries() {		
		this.entries.clear();
	}
}
