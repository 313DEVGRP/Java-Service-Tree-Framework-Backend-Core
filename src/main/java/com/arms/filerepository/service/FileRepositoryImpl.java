/*
 * @author Dongmin.lee
 * @since 2022-11-04
 * @version 22.11.04
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.filerepository.service;

import com.egovframework.ple.treeframework.springhibernate.service.CoreServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("fileRepository")
public class FileRepositoryImpl extends CoreServiceImpl implements FileRepository{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

}