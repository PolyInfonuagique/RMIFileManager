package PolyInfonuagique.RMIFileManager.server;

import PolyInfonuagique.RMIFileManager.shared.MD5Checksum;
import PolyInfonuagique.RMIFileManager.shared.ManageException;
import PolyInfonuagique.RMIFileManager.shared.ServerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server class
 * Daemon file manager to share files between many clients
 *
 * @see ServerInterface to understand each override method offer to the client
 * @author David Livet, Arnaud Baillym
 */
public class Server implements ServerInterface {

    /**
     * File map store.
     * Filename is used as an unique key
     */
    private Map<String, byte[]> files = new ConcurrentHashMap<>();

    /**
     * Last client id number
     */
    private int lastClientId = 0;

    /**
     * Map indicate for each file, who is locking on it
     */
    private Map<String, Integer> lockTable = new ConcurrentHashMap<>();

    /**
     * Main server program
     * @param args no args
     */
    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            ServerInterface server = new Server();
            ServerInterface stub =
                    (ServerInterface) UnicastRemoteObject.exportObject(server, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Server", stub);

            System.out.println("--- Server ready ---");
        } catch (Exception e) {
            System.err.println("Server Exception : "+ e.getMessage());
        }
    }

    @Override
    public int generateClientId() throws RemoteException {
        lastClientId++;
        System.out.println("Generate new id : " + lastClientId);

        return lastClientId;
    }

    @Override
    public void create(String name) throws RemoteException {
        System.out.println("Create " + name);

        if (files.containsKey(name)) {
            throw new RemoteException("File already exists", new ManageException(name + " existe déjà."));
        }

        files.put(name, new byte[0]);
    }

    @Override
    public String list() throws RemoteException {
        StringBuilder buffer = new StringBuilder();
        Set<String> filenames = files.keySet();

        System.out.println("List");

        for (String s : filenames) {
            buffer.append(" * ").append(s);

            // Check if status of file (lock or not)
            if (lockTable.containsKey(s)) {
                buffer.append("\tverrouillé par client ").append(lockTable.get(s));
            } else {
                buffer.append("\tnon verrouillé ");
            }
            buffer.append('\n');
        }

        return buffer.toString() + "Total : " + files.size() + " fichier(s)";
    }

    @Override
    public Map<String, byte[]> syncLocalDir() throws RemoteException {
        return files;
    }

    @Override
    public byte[] get(String name, String checksum) throws RemoteException {
        System.out.println("Get " + name + " (checksum = " + checksum + ")");

        if (files.containsKey(name)) {
            return sendIfNotEquals(name, checksum);
        }
        else {
            throw new RemoteException("Unknown file", new ManageException("Fichier inconnue"));
        }
    }

    @Override
    public byte[] lock(String name, int clientid, String checksum) throws RemoteException {
        if (!files.containsKey(name)) {
            throw new RemoteException("Unknown file", new ManageException("Fichier inconnue"));
        }

        if (lockTable.containsKey(name)) {  // File already locked
            int number = lockTable.get(name);
            throw new RemoteException("The file is alreadey lockes by " + number, new ManageException(name + " est déjà verrouillé par " +
                    "client " + number));
        }
        lockTable.put(name, clientid);

        return sendIfNotEquals(name, checksum);
    }

    @Override
    public void push(String name, byte[] data, int clientid) throws RemoteException {
        if (!files.containsKey(name)) {
            throw new RemoteException("Unknown file", new ManageException("Fichier inconnue"));
        }

        if (!(lockTable.containsKey(name) && lockTable.get(name).equals(clientid))) {
            throw new RemoteException("The file is not available", new ManageException("Opération refusée : vous devez verrouiller d'abord verrouiller le fichier."));
        }

        files.put(name, data);

        // Unlock file after overwriting
        lockTable.remove(name);
    }

    /**
     * Calculate checksum and check if local is equals to client checksum.
     * Send file if it's not equals
     *
     * @param name filename to check
     * @param checksum client checksum
     * @return byte[]|null
     */
    private byte[] sendIfNotEquals(String name, String checksum) {
        byte[] datafile = files.get(name);
        if(MD5Checksum.checkSum(datafile).equals(checksum)){
            System.out.println("\t" + name + " equals with client version");
            return null;
        } else {
            System.out.println("\t" + name + " sent");
            return datafile;
        }
    }

}
