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

import org.directwebremoting.Browser;
import org.directwebremoting.ScriptSessions;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@RemoteProxy(creator = SpringCreator.class, name = "Chat")
@Service
public class Chat {

	public Chat() {
		Global.chat = this;
	}

	public String sendMessageByEngine(String message) {

		Browser.withAllSessions(new Runnable() {
			@Override
			public void run() {
				ScriptSessions.addFunctionCall("dwr_callback","engine","engine", message,time());
			}
		});

		return Global.SUCCESS;
	}

	@RemoteMethod
	public String sendMessage(String message) {

		Object userId = WebContextFactory.get().getScriptSession().getAttribute(Global.USER_ID);
		Object username = WebContextFactory.get().getScriptSession().getAttribute(Global.USERNAME);
		Browser.withAllSessions(new Runnable() {
			@Override
			public void run() {
				ScriptSessions.addFunctionCall("dwr_callback",userId,username, message,time());
			}
		});

		return Global.SUCCESS;
	}
	@RemoteMethod
	public String login(final String userId, final String username) {
		Global.onlineSet.add(new User(userId,username, time()));
		WebContext webContext = WebContextFactory.get();
		webContext.getScriptSession().setAttribute(Global.USER_ID, userId);
		webContext.getScriptSession().setAttribute(Global.USERNAME, username);
		return Global.SUCCESS;
	}

	@RemoteMethod
	public String logout(final String userId) {
		if (!Global.onlineSet.contains(new User(userId))) {
			return Global.ERROR;
		} else {
			Global.onlineSet.remove(new User(userId));
			WebContextFactory.get().getScriptSession().invalidate();
			return Global.SUCCESS;
		}
	}

	@RemoteMethod
	private String time() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}

}
