package org.cipango.ims.hss.web.publicid;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicPrivate;
import org.cipango.ims.hss.web.privateid.EditPrivateIdPage;

@SuppressWarnings("unchecked")
public class ContextPanel extends Panel {

	
	public ContextPanel(PublicIdentity publicIdentity) {
		super("contextMenu");
		add(new BookmarkablePageLink("editLink", EditPublicIdPage.class, 
				new PageParameters("id=" + publicIdentity.getIdentity())));
		
		final List<String> privateIds = new ArrayList<String>();
		Iterator<PublicPrivate> it = publicIdentity.getPrivateIdentities().iterator();
		while (it.hasNext())
			privateIds.add(it.next().getPrivateId());
		
		add(new RefreshingView("privateIds"){

			@Override
			protected Iterator getItemModels()
			{
				return new ModelIteratorAdapter<String>(privateIds.iterator()) {

					@Override
					protected IModel<String> model(String id)
					{
						return new Model<String>(id);
					}
					
				};
			}

			@Override
			protected void populateItem(Item item)
			{
				MarkupContainer link = new BookmarkablePageLink("identity", 
						EditPrivateIdPage.class, 
						new PageParameters("id=" + item.getModelObject()));
				item.add(link);
				link.add(new Label("name", item.getModel()));
			}
		});
		add(new BookmarkablePageLink("newPrivateIdLink", EditPrivateIdPage.class, new PageParameters("publicId=" + publicIdentity.getIdentity())));
	}


}