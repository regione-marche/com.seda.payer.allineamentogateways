package com.seda.payer.allineamentogateways;

import java.net.URL;

import com.seda.commons.properties.tree.PropertiesTree;
import com.seda.payer.allineamentogateways.config.PrintStrings;
import com.seda.payer.allineamentogateways.config.PropertiesPath;
import com.seda.payer.gateways.webservice.dati.GeneraFlussiRendicontazionePosRequest;
import com.seda.payer.gateways.webservice.dati.GeneraFlussiRendicontazionePosResponse;
import com.seda.payer.gateways.webservice.source.GatewaysSOAPBindingStub;
import com.seda.payer.gateways.webservice.source.GatewaysServiceLocator;

public class GeneraFlusso {
	
	private final String DBSCHEMACODSOCIETA = "dbSchemaCodSocieta";
	
	public GeneraFlusso()
	{
	}
	
	public int start (String dbSchemaCodSocieta, int readTimeOut)
	{
		int exitCode=0;
		try 
		{	
			String envRoot = PrintStrings.ROOT.format();
			String rootPath = System.getenv(envRoot);
			
			if (rootPath==null){
				throw new Exception("Variabile di sistema " + envRoot + " non definita");
			}
			
			PropertiesTree configuration;
			
			try {
				configuration = new PropertiesTree(rootPath);
			} catch (Exception e) {
				throw new Exception("Errore durante la creazione del contesto elaborativo " + e.getMessage(),e);
			}
			
			String gatewayWsUrl = configuration.getProperty(PropertiesPath.wsGatewaysUrl.format(PropertiesPath.baseCatalogName.format()));
					
			GatewaysSOAPBindingStub gatewaysPort = null;
			GatewaysServiceLocator commonsServiceLocator = new GatewaysServiceLocator();
			gatewaysPort = (GatewaysSOAPBindingStub)commonsServiceLocator.getGatewaysPort(new URL(gatewayWsUrl));
			if (readTimeOut > 0)
				gatewaysPort.setTimeout(readTimeOut);
			
			gatewaysPort.clearHeaders();
			gatewaysPort.setHeader("", DBSCHEMACODSOCIETA,dbSchemaCodSocieta);
			
			GeneraFlussiRendicontazionePosRequest generaFlussiRendicontazionePosRequest = new GeneraFlussiRendicontazionePosRequest();
			GeneraFlussiRendicontazionePosResponse generaFlussiRendicontazionePosResponse = gatewaysPort.generaFlussiRendicontazionePos(generaFlussiRendicontazionePosRequest);
			
			if ( generaFlussiRendicontazionePosResponse != null)
			{
				System.err.println(generaFlussiRendicontazionePosResponse.getResponse().getRetMessage());
				System.err.println(generaFlussiRendicontazionePosResponse.getResponse().getRetCode());
			
				exitCode = Integer.parseInt(generaFlussiRendicontazionePosResponse.getResponse().getRetCode().getValue());
			}
			else
			{
				exitCode = -2;
			}
			
			return exitCode;
		} catch (Exception e) {
			
			e.printStackTrace();
			return 1;
		} 
	}

}
