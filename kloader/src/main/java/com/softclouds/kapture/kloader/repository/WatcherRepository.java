package com.softclouds.kapture.kloader.repository;

import java.math.BigInteger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.softclouds.kapture.kloader.bo.ImTransaction;

@Repository
public interface WatcherRepository
		extends JpaRepository<ImTransaction, BigInteger>, CrudRepository<ImTransaction, BigInteger> {

}
