/**
 * 
 */
package com.seda.payer.allineamentogateways.config;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author polenta
 *
 */
public enum PropertiesPath {
	baseCatalogName,
	baseLogger,
	wsGatewaysUrl;

    private static ResourceBundle rb;

    public String format( Object... args ) {
        synchronized(PropertiesPath.class) {
            if(rb==null)
                rb = ResourceBundle.getBundle(PropertiesPath.class.getName());
            return MessageFormat.format(rb.getString(name()),args);
        }
    }
}
