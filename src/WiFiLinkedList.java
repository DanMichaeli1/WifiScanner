

import java.util.LinkedList;

public class WiFiLinkedList {
	//private static int ID = 0;
	LinkedList<WiFi> LLWF;
	private double LAT,LON,ALT;
	private String Time;
	private String UID;
	public WiFiLinkedList(){
		
	}
	public WiFiLinkedList(double LAT, double LON, double ALT, String Time, String UID){
		LLWF = new LinkedList<WiFi>();
		this.LAT=LAT;		//קו אנכי
		this.LON= LON;		//קו אופקי
		this.ALT= ALT;		//גובה
		this.Time=Time;		//זמן
		this.UID = UID;	//מזהה
	}
	public void add(WiFi wf){
		LLWF.add(wf);
	}
	public boolean IsBelong(double LAT, double LON, String Time){
		return (LAT==this.LAT && LON==this.LON && Time.equals(this.Time));
	}
	public WiFi[] getArrWiFi(){
//		WiFi[] arr = new WiFi[LLWF.size()];
//		for (int i = 0; i < arr.length; i++) {
//			arr[i] = LLWF.get(i);
//		}
		
		return LLWF.toArray(new WiFi[LLWF.size()]);
	}
	public void setWiFiList(WiFi[] arr){
		LLWF = new LinkedList<WiFi>();
		for (int i = 0; i < arr.length; i++) {
			LLWF.add(arr[i]);
		}
	}
	public String toString(){
	String basicString= Time+" ,"+UID+" ,"+LAT+" ,"+LON+" ,"+ALT+" ,"+LLWF.size();
	for (int i = 0; i < LLWF.size() ; i++) {
	basicString=basicString+LLWF.get(i);
}
	return basicString;
	}
}
