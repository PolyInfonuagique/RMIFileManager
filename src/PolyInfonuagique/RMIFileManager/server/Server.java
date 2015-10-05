package PolyInfonuagique.RMIFileManager.server;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;

import PolyInfonuagique.RMIFileManager.shared.ServerInterface;

public class Server implements ServerInterface {

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
	public void generateClientId() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void create(String nom) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String list() throws RemoteException {
		// TODO Auto-generated method stub
		return "toto";
	}

	@Override
	public void syncLocalDir() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] get(String name, String checksum) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] lock(String nom, int clientid, String checksum)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void push(String nom, byte[] contenu, int clientid)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
}
