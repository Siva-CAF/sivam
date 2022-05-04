package com.softclouds.kapture.kloader.bo;

import java.math.BigInteger;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author sivam
 *
 */
@Entity
@Setter
@Getter
@EqualsAndHashCode
@Table(name = "im_transaction")
public class ImTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true)
	private BigInteger Id;

	@Column(name = "event")
	private String event;

	@Column(name = "full_path")
	private String fullPath;

	@Column(name = "published_ts")
	private Timestamp publishedTs;

}
