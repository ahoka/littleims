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
package org.cipango.ims.hss.web.gba;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.ZhHandler;
import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.uss.Uss;
import org.cipango.ims.hss.util.XML;
import org.cipango.ims.hss.web.privateid.ContextPanel;
import org.cipango.ims.oam.util.AbstractListDataProvider;

public class UssBrowserPage extends UssPage
{
	@SpringBean
	private PrivateIdentityDao _privateIdentityDao;
	
	@SpringBean
	private ZhHandler _zhHandler;
	
	private String _title;
	
	@SuppressWarnings("unchecked")
	public UssBrowserPage(PageParameters pageParameters)
	{				
		String privateIdKey = pageParameters.getString("privateId");

		add(new BookmarkablePageLink("createLink", EditUssPage.class,
				new PageParameters("privateId=" + privateIdKey)));
		final PrivateIdentity privateIdentity = _privateIdentityDao.findById(privateIdKey);
		
		if (privateIdentity == null)
		{
			error(MapVariableInterpolator.interpolate(getString("privateIdentity.error.notFound"),
					new MicroMap("identity", privateIdKey)));
			setResponsePage(getApplication().getHomePage());
			return;
		}
				
		IDataProvider provider = new AbstractListDataProvider<Uss>()
		{

			@Override
			public List<Uss> load()
			{
				return new ArrayList<Uss>(privateIdentity.getUssSet());
			}

			public IModel<Uss> model(Uss uss)
			{
				return new CompoundPropertyModel<Uss>(new DaoDetachableModel(uss));
			}
			
		};
		
		DataView dataView = new DataView("uss", provider)
		{

			@Override
			protected void populateItem(Item item)
			{
				Uss uss = (Uss) item.getModelObject();
				Link link = new BookmarkablePageLink("editLink", EditUssPage.class, 
						new PageParameters("id=" + uss.getId()));
				item.add(link);
				link.add(new Label("id", String.valueOf(uss.getId())));
				item.add(new Label("type", uss.getTypeAsString()));
				item.add(new Label("nafGroup"));
				item.add(new Label("flagAsString"));
			}
			
		};
		dataView.setItemsPerPage(getItemByPage());
		add(dataView);

		setContextMenu(new ContextPanel(privateIdentity));
			_title = MapVariableInterpolator.interpolate(getString( "privateId.gba.title"),
						new MicroMap("identity", privateIdKey));
				
		add(new Label("title", _title));
		
		add(new Label("guss", privateIdentity.getGuss(_zhHandler.getKeyLifetime(), XML.getPretty().newOutput())));
		
	}

	@Override
	public String getTitle()
	{
		return _title;
	}
}
