package PolyInfonuagique.RMIFileManager.client;

import PolyInfonuagique.RMIFileManager.shared.ServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static final String SERVER_ADDR = "127.0.0.1";
    private ServerInterface server;

    public static void main(String[] args){

        try {
            Client c = new Client();
            String option = "";

            if(args.length == 2){
                option = args[1];
            }

            if(args.length >=1 && args.length <= 2){
                    System.out.println(c.execute(args[0], option));
            }
            else{
                System.err.println("Erreur : nombre d'arguments invalide");
            }
        } catch (ManageException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public Client() throws ManageException {

        if(System.getSecurityManager() == null){
            System.setSecurityManager(new SecurityManager());
        }

        try {
            Registry registry = LocateRegistry.getRegistry(SERVER_ADDR);
            server = (ServerInterface) registry.lookup("Server");
        } catch (RemoteException | NotBoundException e) {
            throw new ManageException("Echec de la connexion avec le serveur",e);
        }
    }

    public String execute(String command, String param) throws ManageException {
        try {
            if("list".equals(command)){
                return server.list();
            }
            else if("create".equals(command)){
                if(param.isEmpty()){
                    throw new ManageException("Erreur : create doit avoir un nom de fichier non vide");
                }

                server.create(param);
                return param+" ajouté.";
            }
            else {
                throw new ManageException("Erreur : commande inconnue");
            }
        } catch (RemoteException e) {
            throw new ManageException("Erreur dans l'execution de la commande "+command+" "+param,e);
        }
    }

}
