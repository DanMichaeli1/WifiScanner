

public class WiFi {
	private String SSID;
	private String MAC;
	private double freq, signal;
	 public WiFi(String SSID,String MAC,  double freq, double signal){
		 this.signal=signal;
		 this.MAC= MAC;
		 this.SSID= SSID;
		 this.freq= freq;
	 }
	 public double getSignal(){
		 return signal;
	 }
	 public String toString(){
		 return","+signal+","+MAC+","+SSID+","+freq;
	 }
}
