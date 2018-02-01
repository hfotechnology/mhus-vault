package de.mhus.cherry.vault.client;

import java.security.Provider;
import java.security.Security;

import de.mhus.lib.core.console.ConsoleTable;

public class SecurityInfo {

	public static void main(String[] args) {
		
		try {
			EccSigner.init();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		ConsoleTable out = new ConsoleTable();
		out.setHeaderValues("Name","Version","Info");
		for (Provider provider : Security.getProviders()) {
			out.addRowValues(provider.getName(), provider.getVersion(), provider.getInfo());
		}
		out.print(System.out);
		
		out = new ConsoleTable();
		out.setHeaderValues("Provider","Algorithm","Type");

		for (Provider provider : Security.getProviders()) {
			for (java.security.Provider.Service service : provider.getServices()) {
				out.addRowValues(provider.getName(), service.getAlgorithm(),service.getType());
			}
		}
		out.print(System.out);

	}
}
