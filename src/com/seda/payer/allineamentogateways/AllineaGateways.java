package com.seda.payer.allineamentogateways;

import java.io.Serializable;
import java.net.URL;
import java.util.Calendar;

import com.seda.commons.properties.tree.PropertiesTree;
import com.seda.payer.allineamentogateways.config.PrintStrings;
import com.seda.payer.allineamentogateways.config.PropertiesPath;
import com.seda.payer.gateways.webservice.dati.AllineaAutomaticoTransazioneIGResponse;
import com.seda.payer.gateways.webservice.dati.AllineaAutomaticoTransazioneRequest;
import com.seda.payer.gateways.webservice.dati.AllineaAutomaticoTransazioneResponse;
import com.seda.payer.gateways.webservice.dati.RIDAllineaArchiviazioneRequest;
import com.seda.payer.gateways.webservice.dati.RIDAllineaArchiviazioneResponse;
import com.seda.payer.gateways.webservice.dati.RIDAllineaRequest;
import com.seda.payer.gateways.webservice.dati.RIDAllineaResponse;
import com.seda.payer.gateways.webservice.source.GatewaysSOAPBindingStub;
import com.seda.payer.gateways.webservice.source.GatewaysServiceLocator;

public class AllineaGateways implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Object mutex = new Object();
	private String gatewayType;
	private Calendar paymentDate;
	private int readTimeOut;
	private String dbSchemaCodSocieta;
	public final String DBSCHEMACODSOCIETA = "dbSchemaCodSocieta";
	
	public static boolean bIsBAP = false; 
	static ReturnCode returnCode = new ReturnCode();
		
	/**
	 * Richiama il metodo <code><b>allineaAutomaticoTransazione</b></code> del servizio com.seda.payer.gateways.webservice
	 * <br>
	 */
	public static void main(String[] args) {
		if (args == null || args.length <= 0) {
			System.err.println( "Usage: AllineaGateways -g <P or I or C or R or A or N> -s <dbSchemaCodSocieta> [-d <yyyy-MM-dd>] [-t <timeout in millisecondi>]");
			System.err.println( "Example: AllineaGateways start -g I -s 000XX -t 12000");
			//inizio LP PG180290
			//System.err.println( "  -g,      Tipo Gateway (I=InfoGroup, P=PayPal/PagOnline/Nodospc/MyBank/CartaSi/Satispay, C=PagoInConto, R=RID flusso AEA (adesione/revoca)-CBI (insoluti), A=Archiviazione Elettronica RID, N=tracciato RNINCAEXT)");
			System.err.println( "  -g,      Tipo Gateway (I=InfoGroup, P=PayPal/PagOnline/Nodospc/MyBank/CartaSi/Satispay/MyPay, C=PagoInConto, R=RID flusso AEA (adesione/revoca)-CBI (insoluti), A=Archiviazione Elettronica RID, N=tracciato RNINCAEXT)");
			//fine LP PG180290
			System.err.println( "  -d,      Data pagamento nel formato yyyy-MM-dd");
			System.err.println("   -s,      Codice società per connessione dinamica al DB");
			System.err.println( "  -t,      Timeout in millisecondi");
			//return;
			if (bIsBAP)
			{	
				returnCode.setReturnCode("01", "KO - Argomenti mancanti");
			}
			else
				System.exit(1);
		}
		AllineaGateways allineaGateways = new AllineaGateways();
		if (!allineaGateways.parseArgs(args))
		{
			if (bIsBAP)
			{
				returnCode.setReturnCode("01", "KO - Argomenti non validi");
			}
			else
				System.exit(1);
		}
		allineaGateways.allineaTransazioni();
	}

	private void allineaTransazioni() {
		synchronized (mutex) {
			try {
				String envRoot = PrintStrings.ROOT.format();
				String rootPath = System.getenv(envRoot);
				if (rootPath == null)
					throw new Exception("Variabile di sistema " + envRoot + " non definita");

				PropertiesTree configuration;
				try { configuration = new PropertiesTree(rootPath);
				} catch (Exception e) { throw new Exception("Errore durante la creazione del contesto elaborativo " + e.getMessage(), e); }
				
				GatewaysServiceLocator gatewaysServiceLocator = new GatewaysServiceLocator();
				GatewaysSOAPBindingStub stub = (GatewaysSOAPBindingStub) gatewaysServiceLocator.getGatewaysPort(
						new URL(configuration.getProperty(PropertiesPath.wsGatewaysUrl.format(PropertiesPath.baseCatalogName.format()))));
				if (this.readTimeOut > 0)
					stub.setTimeout(this.readTimeOut);
				
				stub.clearHeaders();
				stub.setHeader("", DBSCHEMACODSOCIETA,dbSchemaCodSocieta);
				
				String retCode = "";
				String retMessage = "";
				int exitCode = 0;
				
				if (this.gatewayType.equalsIgnoreCase("P")) {
					AllineaAutomaticoTransazioneRequest request = new AllineaAutomaticoTransazioneRequest(this.paymentDate);
					AllineaAutomaticoTransazioneResponse allineaAutomaticoTransazioneResp = stub.allineaAutomaticoTransazione(request);
					if (allineaAutomaticoTransazioneResp != null) {
						System.out.println("[" + allineaAutomaticoTransazioneResp.getResponse().getRetCode() + "] - " + 
								allineaAutomaticoTransazioneResp.getResponse().getRetMessage());	
						retCode = allineaAutomaticoTransazioneResp.getResponse().getRetCode().getValue();	//_value1="00", _value2 = "01", _value3 = "02"
						retMessage = allineaAutomaticoTransazioneResp.getResponse().getRetMessage();
					}
				} else if (this.gatewayType.equalsIgnoreCase("I") || this.gatewayType.equalsIgnoreCase("C")) {
					AllineaAutomaticoTransazioneIGResponse allineaAutomaticoTransazioneIGResp = stub.allineaAutomaticoTransazioneIG();
					if (allineaAutomaticoTransazioneIGResp != null) {
						System.out.println("[" + allineaAutomaticoTransazioneIGResp.getResponse().getRetCode() + "] - " + 
								allineaAutomaticoTransazioneIGResp.getResponse().getRetMessage());	
						retCode = allineaAutomaticoTransazioneIGResp.getResponse().getRetCode().getValue();	//_value1="00", _value2 = "01", _value3 = "02"
						retMessage = allineaAutomaticoTransazioneIGResp.getResponse().getRetMessage();
					}				 
				} 
				else if (this.gatewayType.equalsIgnoreCase("R")) 
				{
					RIDAllineaRequest in = new  RIDAllineaRequest();
					in.setSEmpty("");
					RIDAllineaResponse ridAllineaResp = stub.allineaRID(in);
					if (ridAllineaResp != null) {
						System.out.println("[" + ridAllineaResp.getRetCode()+ "] - " + ridAllineaResp.getRetMessage());	
						retCode = ridAllineaResp.getRetCode();
						retMessage = ridAllineaResp.getRetMessage();
					}
				} 
				else if (this.gatewayType.equalsIgnoreCase("A")) 
				{
					// decidere l'allineamento del RID A=Archiviazione Elettronica RID
					RIDAllineaArchiviazioneRequest in = new  RIDAllineaArchiviazioneRequest();
					in.setSEmpty("");
					RIDAllineaArchiviazioneResponse ridArchiviaResp = stub.allineaRIDArchiviazione(in);
					if (ridArchiviaResp != null) {
						System.out.println("[" + ridArchiviaResp.getRetCode()+ "] - " + ridArchiviaResp.getRetMessage());	
						retCode = ridArchiviaResp.getRetCode();
						retMessage = ridArchiviaResp.getRetMessage();
					}
				}
				else if (this.gatewayType.equalsIgnoreCase("N"))
				{
					GeneraFlusso generaFlusso = new GeneraFlusso();
					exitCode=generaFlusso.start(dbSchemaCodSocieta, readTimeOut);
//					if (bIsBAP)
//					{
//						MainBAP bap = new MainBAP();
//						if (exitCode == 0)
//							bap.setEsito("00", "OK");
//						else
//							bap.setEsito("01", "KO - Errore di esecuzione processo");
//					}
//					else
//						System.exit(exitCode);
				}
				else if(this.gatewayType.equalsIgnoreCase("W")){
					AllineaAutomaticoTransazioneRequest request = new AllineaAutomaticoTransazioneRequest(this.paymentDate);
					AllineaAutomaticoTransazioneResponse allineaAutomaticoTransazioneResp = stub.allineaAutomaticoTransazione(request);
					if (allineaAutomaticoTransazioneResp != null) {
						System.out.println("[" + allineaAutomaticoTransazioneResp.getResponse().getRetCode() + "] - " + 
								allineaAutomaticoTransazioneResp.getResponse().getRetMessage());
						retCode = allineaAutomaticoTransazioneResp.getResponse().getRetCode().getValue();	//_value1="00", _value2 = "01", _value3 = "02"
						retMessage = allineaAutomaticoTransazioneResp.getResponse().getRetMessage();
					}	
				}
				else {
					System.err.println("Tipo Gateway non supportato");
					retCode = "05";
					retMessage = "Tipo Gateway non supportato";
				}

				if (bIsBAP)
				{
					if (exitCode == 0) {
						if (!retCode.equals("") && !retCode.equals("00")) {
							returnCode.setReturnCode(retCode, retMessage);
						} 
						else 
							returnCode.setReturnCode("00", "OK");
					}
					else
						returnCode.setReturnCode("01", "KO - Errore di esecuzione processo");
				}
				else
					System.exit(exitCode);
			} catch (Exception e) {
				System.err.println("Errore nella procedura : " + e.getMessage());
				e.printStackTrace();
				if (bIsBAP)
				{
					returnCode.setReturnCode("02", "KO - Errore generico");
				}
				else
					System.exit(1);
			}
		}
	}

	public boolean parseArgs(String[] args) {
		boolean ret = true;
		boolean bOk = true;
		for (int i = 0; i < args.length && bOk; i++) {
			if (args[i].length() > 1 && (args[i].charAt(0) == '-')) {
				if (i == args.length - 1) {
					ret = false;
					break;
				}
				switch (args[i].toLowerCase().charAt(1)) {
					case 'g':
						String t_gatewayType = args[++i];
						if ( null == t_gatewayType || 0 >= t_gatewayType.trim().length() 
								|| (!t_gatewayType.equalsIgnoreCase("P") 
										&& !t_gatewayType.equalsIgnoreCase("O")
										&& !t_gatewayType.equalsIgnoreCase("I")
										&& !t_gatewayType.equalsIgnoreCase("C")
										&& !t_gatewayType.equalsIgnoreCase("R")
										&& !t_gatewayType.equalsIgnoreCase("A")
										&& !t_gatewayType.equalsIgnoreCase("N")
										&& !t_gatewayType.equalsIgnoreCase("W"))) {
							System.err.println( "parametro -g valore non permesso, valori accettati: P, O, I , C, R, A , N o W. Parametro obbligatorio");
							ret = bOk = false;
						} else gatewayType = t_gatewayType.toUpperCase();
						break;
					case 'd':
						String t_paymentDate = args[++i];
						try {
							paymentDate = Calendar.getInstance();
							paymentDate.setTime(java.sql.Date.valueOf(t_paymentDate));
						} catch (Exception e) {
							System.err.println( "parametro -d valore non permesso, formato yyyy-MM-dd");
							ret = bOk = false;
						}
						break;
					case 't':
						String t_readTimeOut = args[++i];
						try { readTimeOut = Integer.parseInt(t_readTimeOut);
						} catch (Exception e) {
							System.err.println( "parametro -t valore non permesso, formato in millisecondi");
							ret = bOk = false;
						}
						break;
					case 's':
						dbSchemaCodSocieta = args[++i];
						if (dbSchemaCodSocieta == null || dbSchemaCodSocieta.trim().length() == 0 
								|| dbSchemaCodSocieta.trim().charAt(0) == '-')
						{
							System.err.println("parametro -s mancante. Parametro obbligatorio");
							ret = bOk = false;
						}
						break;
					default:
						ret = false;
					break;
				}
			}
		}
		
		if (gatewayType == null || gatewayType.length() == 0)
		{
			System.err.println("parametro -g obbligatorio");
			ret = false;
		}
		if (dbSchemaCodSocieta == null || dbSchemaCodSocieta.length() == 0)
		{
			System.err.println("parametro -s obbligatorio");
			ret = false;
		}
		return ret;
	}
}