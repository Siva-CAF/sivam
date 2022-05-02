package com.softclouds.kapture.kloader.controller;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.softclouds.kapture.kloader.service.ILoaderService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoaderController implements Job {

	@Autowired
	ILoaderService iloaderService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("Start Of LoaderController:::JobExecution::");
		iloaderService.processEvents();

	}

}
