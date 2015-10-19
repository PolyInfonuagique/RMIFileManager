package PolyInfonuagique.RMIFileManager.client;

import PolyInfonuagique.RMIFileManager.shared.MD5Checksum;
import PolyInfonuagique.RMIFileManager.shared.ManageException;
import PolyInfonuagique.RMIFileManager.shared.ServerInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

/**
 *  Client class
 *
 *  Communicate with RMI to server deamon
 *  Commands available:
 *  - list
 *  - create filename
 *  - get filename
 *  - lock filename
 *  - push filename
 *  - syncLocalDir
 *
 *  @see ServerInterface
 *  @author David Livet, Arnaud Baillym
 */
public class Client {

    /**
     * Sever address constant
     */
    public static final String SERVER_ADDR = "127.0.0.1";

    /**
     * Interface with rmi server
     */
    private ServerInterface server;

    /**
     * Current clientID
     * If null read in .clientID file
     * If .clientID file doesn't exists, ask server
     */
    private Integer clientId = null;

    /**
     * Constructor
     * Handle connection with server
     *
     * @throws ManageException when connection doesn't work
     */
    public Client() throws ManageException {

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            Registry registry = LocateRegistry.getRegistry(SERVER_ADDR);
            server = (ServerInterface) registry.lookup("Server");
        } catch (RemoteException | NotBoundException e) {
            throw new ManageException("Echec de la connexion avec le serveur", e);
        }
    }

    /**
     * Main program
     * Read 1 or 2 arguments (depends on command) and display result or error
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        try {
            Client c = new Client();
            String option = "";

            if (args.length == 2) {
                option = args[1];
            }

            // Output program
            if (args.length >= 1 && args.length <= 2) {
                System.out.println(c.execute(args[0], option));
            } else {
                System.err.println("Erreur : nombre d'arguments invalide");
            }
        } catch (ManageException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Execute a command
     *
     * @param command name to execute (list, create, get, push, lock, syncLocalDir)
     * @param param parameter if necessary
     * @return result
     * @throws ManageException when error appeared
     */
    public String execute(String command, String param) throws ManageException {
        try {
            // List command
            if ("list".equals(command)) {
                return server.list();
            }
            // Create command (1 param)
            else if ("create".equals(command)) {
                if (param.isEmpty()) {
                    throw new ManageException("Erreur : create doit avoir un nom de fichier non vide");
                }

                server.create(param);

                return param + " ajouté";
            }
            // Get command (1 param)
            else if ("get".equals(command)) {
                if (param.isEmpty()) {
                    throw new ManageException("Erreur : get doit avoir un nom de fichier non vide");
                }

                String checksum = getChecksum(param);
                byte[] data = server.get(param, checksum);

                createOrUpdateFile(param, data);

                return param + " synchronisé";
            }
            // Lock command (1 param)
            else if ("lock".equals(command)) {
                if (param.isEmpty()) {
                    throw new ManageException("Erreur : lock doit avoir un nom de fichier non vide");
                }

                String checksum = getChecksum(param);
                byte[] data;

                data = server.lock(param, getClientId(), checksum);
                createOrUpdateFile(param, data);


                return param + " verrouillé";
            }
            // Push command (1 param)
            else if ("push".equals(command)) {
                if (param.isEmpty()) {
                    throw new ManageException("Erreur : push doit avoir un nom de fichier non vide");
                }

                File f = new File(param);
                if (f.exists()) {
                    server.push(f.getName(), Files.readAllBytes(f.toPath()), getClientId());

                    return param + " a été envoyé au serveur";
                } else {
                    throw new ManageException("Erreur fichier inconnue");
                }
            }
            // SyncLocalDir
            else if ("syncLocalDir".equals(command)) {
                Map<String, byte[]> files = server.syncLocalDir();

                files.forEach((fileName, data) -> {
                    try {
                        createOrUpdateFile(fileName, data);
                    } catch (ManageException ignored) {}
                });

                return files.size() + " mise à jour";
            } else {
                throw new ManageException("Erreur : commande inconnue");
            }
        } catch (RemoteException e) {
            manageRemoteException(e);
        } catch (IOException e) {
            throw new ManageException("Erreur IO",e);
        }

        return "";
    }

    /**
     * Get client id, ask server when if doesn't know current id.
     * Store in .clientID file
     *
     * @return int id
     * @throws RemoteException
     */
    private int getClientId() throws RemoteException {
        File clientIdFile = new File(".clientID");
        if (!clientIdFile.exists()) {
            try {
                clientIdFile.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(clientIdFile);

                clientId = server.generateClientId();
                outputStream.write(clientId);
                outputStream.close();

            } catch (IOException e) {
                throw new RemoteException("Erreur IO",e);
            }
        }

        if (clientId == null) {
            try {
                FileInputStream inputStream = new FileInputStream(clientIdFile);
                clientId = inputStream.read();
            } catch (IOException e) {
                throw new RemoteException("Erreur IO",e);
            }
        }
        return clientId;
    }

    /**
     * Get checksum MD5 for a file
     *
     * @param fileName file name
     * @return checksum MD5 or -1 if file doesn't exist
     * @see MD5Checksum
     */
    private String getChecksum(String fileName) {
        String checksum = "-1";
        File f = new File(fileName);

        if (f.exists()) {
            checksum = MD5Checksum.checkSum(f.getPath());
        }

        //System.out.println("DEBUG: checksum = " + checksum);
        return checksum;
    }

    /**
     * Create or update local file with data
     *
     * @param fileName file name
     * @param data     array of bytes
     * @throws ManageException when IO error
     */
    private void createOrUpdateFile(String fileName, byte[] data) throws ManageException {
        File localFile = new File(fileName);
        if (data != null) {
            try {
                FileOutputStream output = new FileOutputStream(localFile);
                output.write(data);
                output.close();
            } catch (IOException e) {
                throw new ManageException("Erreur IO",e);
            }
        }
    }

    /**
     * If IOException is caused by an ManageException, display corresponding message
     *
     * @param e exception to handle
     * @throws ManageException
     */
    private void manageRemoteException(RemoteException e) throws ManageException {
        if (e.getCause().getCause() instanceof ManageException) {
            throw (ManageException) e.getCause().getCause();
        }
        else {
            throw new ManageException("Erreur IO");
        }
    }

}
