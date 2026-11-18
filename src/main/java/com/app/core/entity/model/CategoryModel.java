package com.app.core.entity.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "category")
public class CategoryModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)	
	private Long id;
	
	private String name;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_at")
	private Date createAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private CategoryModel parent;

	@OneToMany(mappedBy = "parent")
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<CategoryModel> children = new ArrayList<>();

	/*
	 * @OneToOne(mappedBy = "category")
	 * 
	 * @JsonIgnoreProperties(value = "category") private ProductModel product;
	 */

	@PrePersist
	private void prePersist() {
		this.createAt = new Date();
	}
}
