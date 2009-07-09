// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
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
package org.cipango.littleims.scscf.data;

import java.util.HashSet;
import java.util.Set;



public class UserProfile
{

	private String _uri;
	private boolean _barred;
	private ServiceProfile _serviceProfile;
	private String _serviceLevelTraceInfo;
	private Set<UserProfileListener> _listeners;
	
	public UserProfile(String uri)
	{
		_uri = uri;
	}

	public boolean isBarred()
	{
		return _barred;
	}

	public void setBarred(boolean b)
	{
		_barred = b;
	}

	public void setServiceProfile(ServiceProfile profile)
	{
		_serviceProfile = profile;
	}

	public ServiceProfile getServiceProfile()
	{
		return _serviceProfile;
	}

	public String getURI()
	{
		return _uri;
	}

	public String getServiceLevelTraceInfo()
	{
		return _serviceLevelTraceInfo;
	}

	public void setServiceLevelTraceInfo(String serviceLevelTraceInfo)
	{
		if (_listeners != null && 
				(((serviceLevelTraceInfo != null && !serviceLevelTraceInfo.equals(_serviceLevelTraceInfo)))
				|| (serviceLevelTraceInfo == null &&  _serviceLevelTraceInfo != null)))
		{
			for (UserProfileListener l : _listeners)
				l.serviceLevelTraceInfoChanged(serviceLevelTraceInfo);
		}
		_serviceLevelTraceInfo = serviceLevelTraceInfo;
	}
	
	public void addListener(UserProfileListener l)
	{
		if (_listeners == null)
			_listeners = new HashSet<UserProfileListener>();
		_listeners.add(l);
	}
	
	public void removeListener(UserProfileListener l)
	{
		if (_listeners != null)
			_listeners.remove(l);
	}

}
