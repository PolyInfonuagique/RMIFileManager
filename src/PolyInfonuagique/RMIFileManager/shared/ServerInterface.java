package PolyInfonuagique.RMIFileManager.shared;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    /**
     * Génération d'un ID unique pour le client
     */
	int generateClientId() throws RemoteException;

    /**
     * Création d'un fichier vide sur le serveur.
     * Attention : échec en cas de doublon
     * @param nom : nom du fichier
     * @throws RemoteException
     */
	void create(String nom) throws RemoteException;

    /**
     * Liste les fichiers présent sur le serveur comprenant pour chaque fichier : son nom (et identifiant du client possédant un verrou sur le fichier)
     *
     * Exemple :
     * * fichier1   non verrouillé
     * * fichier2   non verrouillé
     * 2 fichier(s)
     *
     * @return String la liste à afficher sous forme de string.
     * @throws RemoteException
     */
	String list() throws RemoteException;

    /**
     * Synchronisation du dossier local avec le serveur.
     * Les fichiers locaux sont supprimés et remplacé par leur version distante
     * @throws RemoteException
     */
	File[] syncLocalDir() throws RemoteException;

    /**
     * Obtention de la dernière version du fichier
     * @param name fichier souhaité
     * @param checksum hash du fichier local afin de déterminé si le fichier distant est identique à la version locale.
     * @return File|null le fichier correspondant, null si la version local est la même que celle du serveur (checksum identique)
     * @throws RemoteException
     */
    byte[] get(String name, String checksum) throws RemoteException;

    /**
     * Verrouillage d'un fichier. Récupération de la dernière version du fichier en local.
     * Echec en cas de verrouillage par un autre client
     * @param nom fichier à bloquer
     * @param clientid id du client
     * @param checksum hash du fichier local
     * @return File|null le fichier correspondant, null si la version local est la même que celle du serveur (checksum identique)
     * @throws RemoteException
     */
    byte[] lock(String nom, int clientid, String checksum) throws RemoteException;

    /**
     * Transfert d'une nouvelle version du fichier au serveur.
     * Obligation d'avoir lock le fichier avant.
     * Déverrouille le fichier après transfert
     * @param nom fichier à envoyer
     * @param contenu nouveau contenu
     * @param clientid id du client
     * @throws RemoteException
     */
	void push(String nom, byte[] contenu, int clientid) throws RemoteException;
}
