/*
 * @author Dongmin.lee
 * @since 2023-03-13
 * @version 23.03.13
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.egovframework.javaservice.treeframework.remote;

import com.egovframework.javaservice.treeframework.remote.User;
import com.egovframework.javaservice.treeframework.remote.Chat;

import java.util.LinkedHashSet;
import java.util.Set;

public class Global {

	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	public static final String USER_ID = "userId";
	public static final String USERNAME = "username";
	public static final Set<User> onlineSet = new LinkedHashSet<User>();
	public static Chat chat;
}
