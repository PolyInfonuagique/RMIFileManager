package PolyInfonuagique.RMIFileManager.shared;

public interface ServerInterface {
	public void generateClientId();
	public void create(String nom);
	public String list();
	public void syncLocalDir();
	public Integer get(String name, String checksum);
	public Integer lock(String nom, Integer clientid, String checksum);
	public void push(String nom, byte[] contenu, Integer clientid);
}
