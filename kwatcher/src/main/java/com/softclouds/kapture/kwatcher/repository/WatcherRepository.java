package com.softclouds.kapture.kwatcher.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.softclouds.kapture.kwatcher.bo.ImTransaction;

@Repository
public interface WatcherRepository extends JpaRepository<ImTransaction, BigInteger> {

}
