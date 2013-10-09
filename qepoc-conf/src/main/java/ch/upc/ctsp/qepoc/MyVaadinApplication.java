/*
 * Copyright 2009 IT Mill Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ch.upc.ctsp.qepoc;

import java.util.regex.PatternSyntaxException;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Hierarchical;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.Window;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MyVaadinApplication extends Application {
	private Window window;
	
	class MyCustomFilter implements Container.Filter {
	    protected String propertyId;
	    protected String regex;
	    protected Label  status;
	    
	    public MyCustomFilter(String propertyId, String regex, Label status) {
	        this.propertyId = propertyId;
	        this.regex      = regex;
	        this.status     = status;
	    }

	    /** Apply the filter on an item to check if it passes. */
	    public boolean passesFilter(Object itemId, Item item)
	            throws UnsupportedOperationException {
	        // Acquire the relevant property from the item object
	        Property p = item.getItemProperty(propertyId);
	        
	        // Should always check validity
	        if (p == null || !p.getType().equals(String.class))
	            return false;
	        String value = (String) p.getValue();
	        
	        // Pass all if regex not given
	        if (regex.isEmpty()) {
	            status.setValue("Empty filter");
	            return true;
	        }
	        
	        // The actual filter logic + error handling
	        try {
	            boolean result = value.matches(regex);
	            status.setValue(""); // OK
	            return result;
	        } catch (PatternSyntaxException e) {
	            status.setValue("Invalid pattern");
	            return false;
	        }
	    }

	    /** Tells if this filter works on the given property. */
	    public boolean appliesToProperty(Object propertyId) {
	        return propertyId != null &&
	               propertyId.equals(this.propertyId);
	    }
	}

	@Override
	public void init() {
		window = new Window("My Vaadin Application");
		setMainWindow(window);
		final TreeTable ttable = new TreeTable("My TreeTable");
		ttable.addContainerProperty("Name", String.class, "");
		ttable.addContainerProperty("Reference", String.class, "");
		ttable.addContainerProperty("ScopeName", String.class, "");
		ttable.addContainerProperty("Description", String.class, "");
		ttable.setWidth("100%");

		// Create the tree nodes
		ttable.addItem(new Object[] { "Root", "", "", ""}, 0);
		ttable.addItem(new Object[] { "cmts", "cmts/{cmtsname}", "public", "A Cable Modem Termination System"}, 1);
		ttable.addItem(new Object[] { "modem", "modem/{mac}", "public", "A Cable Modem." }, 2);
		ttable.addItem(new Object[] { "scope", "scope/{scopename}", "public", "Ein Scope ist ein Set von IP Adress Ranges." }, 3);
		ttable.setParent(1, 0);
		ttable.setParent(2, 0);
		ttable.setParent(3, 0);
		ttable.addItem(new Object[] { "ip" }, 4);
		ttable.setParent(4, 1);
		ttable.addItem(new Object[] { "ip" }, 5);
		ttable.setParent(5, 2);
		ttable.addItem(new Object[] { "scopename", "scope/{scopename}", "public", "Der Name eines Scopes."}, 6);
		ttable.setParent(6, 2);
		ttable.addItem(new Object[] { "cmts", "modem/{mac}/scope/cmts", "public", "Der Cmts an welchem dieses Modem derzeit angeschlossen ist." }, 7);
		ttable.setParent(7, 2);
		ttable.addItem(new Object[] { "cmtsname", "modem/{mac}/scope/cmtsname", "public", "Der name des Cmtses an welchem dieses Modem derzeit angeschlossen ist."  }, 8);
		ttable.setParent(8, 2);

		// Expand the tree
		ttable.setCollapsed(2, false);
		// Text field for inputting a filter
		final TextField tf = new TextField("My Own Filter");
		tf.setValue(".*(Ada|Love).*");
		tf.focus();	
		

		HorizontalLayout filterRow = new HorizontalLayout();
		filterRow.setSpacing(true);
		window.addComponent(filterRow);

		Button apply = new Button("Apply Filter");
		filterRow.addComponent(tf);
		filterRow.addComponent(apply);
		filterRow.setComponentAlignment(apply, Alignment.BOTTOM_LEFT);
		        
		final Label status = new Label("");
		filterRow.addComponent(status);
		filterRow.setComponentAlignment(status, Alignment.BOTTOM_LEFT);
		
		final IndexedContainer c = (IndexedContainer) ttable.getContainerDataSource();
		        
		// Filter table according to typed input
		apply.addListener(new ClickListener() {
		    MyCustomFilter filter = null;

		    public void buttonClick(ClickEvent event) {
		        // Remove old filter
		        if (filter != null)
		            c.removeContainerFilter(filter);
		        
		        // Set new filter for the "Name" column
		        filter = new MyCustomFilter("Name",
		                (String) tf.getValue(), status);
		        c.addContainerFilter(filter);
		    }
		});
		
		window.addComponent(ttable);

	}
}
