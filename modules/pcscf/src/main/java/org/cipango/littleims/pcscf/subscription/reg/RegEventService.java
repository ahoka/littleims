// ========================================================================
// Copyright 2009 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.cipango.littleims.pcscf.subscription.reg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.cipango.littleims.pcscf.subscription.Subscription;
import org.cipango.littleims.pcscf.subscription.SubscriptionServlet;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.Methods;

public class RegEventService
{
	private final Logger _log = Logger.getLogger(RegEventService.class);
	public static final String EVENT_REG = "reg";
	
	private SipFactory _sipFactory;
	private SipURI _pcscfUri;
	
	private Map<String, RegSubscription> _subscriptions = new HashMap<String, RegSubscription>();
	private Map<String, List<String>> _registeredUsers = new HashMap<String, List<String>>();
	
	private String _userAgent;
	
	/**
	 * See 3GPP 24.229 �5.2.3A	Subscription to the user's debug event package 
	 * @param aor
	 */
	public void subscribe(URI aor, int expires, String privateIdentity, Iterator<Address> associatedUris)
	{
		try
		{
			if (associatedUris != null)
			{
				List<String> uris = new ArrayList<String>();
				synchronized (_registeredUsers)
				{
					while (associatedUris.hasNext())
					{
						String uri = associatedUris.next().getURI().toString();
						uris.add(uri);
						_registeredUsers.put(uri, uris);
					}
				}
			}
			synchronized (_subscriptions)
			{
				RegSubscription subscription = _subscriptions.get(privateIdentity);
				
				if (subscription == null || !subscription.getSession().isValid())
				{
					SipServletRequest request = _sipFactory.createRequest(
							_sipFactory.createApplicationSession(),
							Methods.SUBSCRIBE,
							_pcscfUri,
							aor);
					request.addHeader(Headers.EVENT, EVENT_REG);
					request.addHeader(Headers.P_ASSERTED_IDENTITY, _pcscfUri.toString());
					request.setExpires(expires);
					if (_userAgent != null)
						request.setHeader(Headers.USER_AGENT, _userAgent);
						
					subscription = new RegSubscription(this, request.getSession(), aor.toString(), privateIdentity);
					_subscriptions.put(privateIdentity, subscription);
					request.getApplicationSession().setAttribute(Subscription.class.getName(), 
							subscription);
					request.getSession().setHandler(SubscriptionServlet.class.getSimpleName());
					request.send();
					_log.debug("Start reg subscription of user " + aor);
				}
				else
				{
					SipServletRequest request = subscription.getSession().createRequest(Methods.SUBSCRIBE);
					request.addHeader(Headers.EVENT, EVENT_REG);
					request.addHeader(Headers.P_ASSERTED_IDENTITY, _pcscfUri.toString());
					request.setExpires(expires);
					if (_userAgent != null)
						request.setHeader(Headers.USER_AGENT, _userAgent);
					request.send();
				}
			}		
		}
		catch (Exception e)
		{
			_log.warn("Failed to SUBSCRIBE to reg event", e);
		}
	}
	
	public Iterator<RegSubscription> getRegSubscriptions()
	{
		return _subscriptions.values().iterator();
	}
	
	public Map<String, List<String>> getRegisteredUsers()
	{
		return _registeredUsers;
	}
		
	public boolean isRegistered(URI aor)
	{
		return _registeredUsers.containsKey(aor.toString());
	}
	
	protected void removeIdentitie(List<String> identities)
	{
		if (identities.isEmpty())
			return;
		
		synchronized (_registeredUsers)
		{
			Iterator<String> it = identities.iterator();
			while (it.hasNext())
			{
				String identity = (String) it.next();
				List<String> l = _registeredUsers.remove(identity);
				if (l != null)
					l.remove(identity);
			}
		}
	}
	
	protected void addIdentitie(List<String> identities, boolean fullState)
	{
		if (identities.isEmpty())
			return;
		
		synchronized (_registeredUsers)
		{
			Iterator<String> it = identities.iterator();
			List<String> previous = fullState ? _registeredUsers.put(identities.get(0), identities) : null;
			
			while (it.hasNext())
			{
				String identity = (String) it.next();
				
				if (fullState)
				{
					_registeredUsers.put(identity, identities);
					if (previous != null)
						previous.remove(identity);
				}
				else
				{
					List<String> l = _registeredUsers.get(identity);
					if (l == null)
					{
						l = new ArrayList<String>();
						_registeredUsers.put(identity, l);
					}
					if (!l.contains(identity))
						l.add(identity);
				}
			}
			
			if (previous != null)
				removeIdentitie(previous);
		}
	}
	
	protected void removeSubscription(RegSubscription subscription)
	{
		synchronized (_subscriptions)
		{
			_subscriptions.remove(subscription.getPrivateIdentity());
		}
	}
	
	public SipFactory getSipFactory()
	{
		return _sipFactory;
	}

	public void setSipFactory(SipFactory sipFactory)
	{
		_sipFactory = sipFactory;
	}

	public SipURI getPcscfUri()
	{
		return _pcscfUri;
	}

	public void setPcscfUri(SipURI pcscfUri)
	{
		_pcscfUri = pcscfUri;
	}

	public String getUserAgent()
	{
		return _userAgent;
	}

	public void setUserAgent(String userAgent)
	{
		_userAgent = userAgent;
	}
}