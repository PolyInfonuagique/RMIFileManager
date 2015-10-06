package PolyInfonuagique.RMIFileManager.client;

import PolyInfonuagique.RMIFileManager.shared.ServerInterface;

import java.io.*;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static final String SERVER_ADDR = "127.0.0.1";
    private ServerInterface server;
    private Integer clientId = null;

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
                return param+" ajouté";
            }
            else if("get".equals(command)){
                if(param.isEmpty()){
                    throw new ManageException("Erreur : get doit avoir un nom de fichier non vide");
                }

                String checksum = getChecksum(param);
                byte[] data = server.get(param, checksum);

                createOrUpdateFile(param, data);
                return param+" synchronisé";
            }
            else if("lock".equals(command)){
                if(param.isEmpty()){
                    throw new ManageException("Erreur : lock doit avoir un nom de fichier non vide");
                }

                String checksum = getChecksum(param);

                byte[] data = server.lock(param, getClientId(), checksum);

                createOrUpdateFile(param, data);
                return param+" verrouillé";
            }
            else if("push".equals(command)){
                File f = new File(param);
                if(f.exists()){
                    try {
                        server.push(f.getName(),Files.readAllBytes(f.toPath()),getClientId());

                    } catch (IOException e) {
                        throw new ManageException("Erreur IO");
                    }
                    return param+" a été envoyé au serveur";
                }
                else{
                    throw new ManageException("Erreur fichier inconnue");
                }
            }
            else {
                throw new ManageException("Erreur : commande inconnue");
            }
        } catch (RemoteException e) {
            throw new ManageException("Erreur dans l'execution de la commande "+command+" "+param,e);
        }
    }

    private int getClientId() throws RemoteException {
        if(clientId == null){
            clientId = server.generateClientId();
        }
        return clientId;
    }

    private String getChecksum(String fileName) {
        String checksum = "-1";
        File f = new File(fileName);

        if(f.exists()){
            checksum = MD5Checksum.checkSum(f.getPath());
        }
        return checksum;
    }

    private void createOrUpdateFile(String fileName, byte[] data) throws ManageException {
        File localFile = new File(fileName);
        if(data != null){
            try {
                FileOutputStream output = new FileOutputStream(localFile);
                output.write(data);
                output.close();
            } catch (IOException e) {
                throw new ManageException("Erreur IO");
            }
        }
    }

}
