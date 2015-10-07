package PolyInfonuagique.RMIFileManager.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;

import PolyInfonuagique.RMIFileManager.shared.ServerInterface;

public class Server implements ServerInterface {
	
	private Map<String, byte[]> files = new HashMap<String, byte[]> ();

	protected Server() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args){
		if(System.getSecurityManager()==null){
			System.setSecurityManager(new SecurityManager());
		}
		try {
			String name = "Server";
			ServerInterface Server = new Server();
			ServerInterface stub =
					(ServerInterface) UnicastRemoteObject.exportObject(Server,0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(name,stub);
			System.out.println("Server Bound");
		}catch (Exception e) {
			System.err.println("Server Exception:");
			e.printStackTrace();
		}
	}

	@Override
	public int generateClientId() throws RemoteException {
		return 1;
	}

	@Override
	public void create(String nom) throws RemoteException {
		System.out.println("Create "+ nom);
		files.put(nom, new byte[0]);
	}

	@Override
	public String list() throws RemoteException {
		StringBuffer buffer = new StringBuffer();
		System.out.println("List");
		Set<String> filenames = files.keySet();
		for (String s : filenames) {
			buffer.append("* " + s + "\n");
		}
		return buffer.toString() + "Total : " + files.size() ;
	}

	@Override
	public void syncLocalDir() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] get(String name, String checksum) throws RemoteException {
		System.out.println("Get " + name + " (checksum = " + checksum + ")");
		return null;
	}

	@Override
	public byte[] lock(String nom, int clientid, String checksum)
			throws RemoteException {
		System.out.println("Lock "+ nom + " (checksum = " + checksum + ") by : "+clientid);
		return null;
	}

	@Override
	public void push(String nom, byte[] contenu, int clientid)
			throws RemoteException {
		System.out.println("Push "+ nom + " (size = " + contenu.length + ") by : "+clientid);
		
	}
	
}
