package PolyInfonuagique.RMIFileManager.shared;

public interface ServerInterface {
	void generateclientid();
	void create(String nom);
	String list();
	void syncLocalDir();
	Integer get(String name, String checksum);
	Integer lock(String nom, Integer clientid, String checksum);
	void push(String nom, byte[] contenu, Integer clientid);
}
