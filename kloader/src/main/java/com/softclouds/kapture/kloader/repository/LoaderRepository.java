package com.softclouds.kapture.kloader.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.softclouds.kapture.kloader.bo.ImTransactionProcessed;

@Repository
public interface LoaderRepository
		extends JpaRepository<ImTransactionProcessed, BigInteger>, CrudRepository<ImTransactionProcessed, BigInteger> {

}
