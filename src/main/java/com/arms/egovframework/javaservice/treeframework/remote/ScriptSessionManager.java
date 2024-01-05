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
package com.arms.egovframework.javaservice.treeframework.remote;

import org.directwebremoting.event.ScriptSessionEvent;
import org.directwebremoting.event.ScriptSessionListener;
import org.directwebremoting.impl.DefaultScriptSessionManager;

public class ScriptSessionManager extends DefaultScriptSessionManager {

	public ScriptSessionManager() {
		super();
		addScriptSessionListener(new ScriptSessionListener() {

			@Override
			public void sessionDestroyed(ScriptSessionEvent ev) {
				Global.chat.logout((String) ev.getSession().getAttribute(Global.USERNAME));
			}

			@Override
			public void sessionCreated(ScriptSessionEvent ev) {
				System.out.println("springDWR Chat Session Create :" + ev.getSession().getAttribute(Global.USERNAME));
			}
		});
	}

}
