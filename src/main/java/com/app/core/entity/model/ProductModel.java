package com.app.core.entity.model;

import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "product")
@EqualsAndHashCode(of = {"id"})
public class ProductModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true, nullable = false)
	private String code;
	private String name;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_at")
	private Date createAt;
	
	@ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private CategoryModel category;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "price_id", referencedColumnName = "id")
	private PriceModel price;
	
	@JsonIgnoreProperties(value = {"examen"}, allowSetters = true)
	 @OneToMany (cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	 private Set<ImageModel> images;

	@PrePersist
	private void prePersist() {
		this.createAt = new Date();
	}
	
	public void setNewImages(Set<ImageModel> images) {
		this.images.clear();
		images.forEach(this::addImages);
	}
	
	public void addImages(ImageModel image) {
		this.images.add(image);
		image.setProduct(this);
	}
	
	public void removeImages(ImageModel image) {
		this.images.remove(image);
		image.setProduct(null);
	}	

}
