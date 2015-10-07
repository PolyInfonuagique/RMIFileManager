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
	private int lastClientId = 0;
	private Map<String, Integer> lockTable = new HashMap<String, Integer>();

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
		lastClientId ++;
		return lastClientId;
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
	public Map<String, byte[]> syncLocalDir() throws RemoteException {
		return files;
	}

	@Override
	public byte[] get(String name, String checksum) throws RemoteException {
		System.out.println("Get " + name + " (checksum = " + checksum + ")");
		if(files.containsKey(name)){
			return files.get(name);
		}
		else throw new RemoteException("Unknown file");
	}

	@Override
	public byte[] lock(String nom, int clientid, String checksum) throws RemoteException {
		if(!files.containsKey(nom)){
			throw new RemoteException("Unknown file");
		}
		if(lockTable.containsKey(nom)){
			throw new RemoteException("The file is alreadey lockes by " + lockTable.get(nom));
		}
		lockTable.put(nom, clientid);
		return files.get(nom);
	}

	@Override
	public void push(String nom, byte[] contenu, int clientid) throws RemoteException {
		if(!files.containsKey(nom)){
			throw new RemoteException("Unknown file");
		}
		if(!(lockTable.containsKey(nom) && lockTable.get(nom).equals(clientid))){
			throw new RemoteException("The file is not available");
		}
		files.put(nom,contenu);
	}
	
}
