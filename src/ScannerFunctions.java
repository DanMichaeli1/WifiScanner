import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.ReadOnlyFileSystemException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 
 * @author Dan Michaeli, Dor Levi, Yarden Mizrahi
 *
 */
public class ScannerFunctions{

	/**
	 * 
	 * @param directoryName directory to search csv files
	 * @return list with all the csv files paths
	 */
	public static ArrayList<String> getAllcsvFileListFromFolder(String directoryName){

		ArrayList<String> fileList = new ArrayList<String>();
		File directory = new File(directoryName);

		//get all the files from a directory

		if (!directory.isDirectory()) {
			return fileList;
		}
		File[] fList = directory.listFiles();

		for (File file : fList){

			if (file.isFile()){

				if(file.getAbsolutePath().endsWith(".csv")){
					fileList.add(file.getAbsolutePath());
					System.out.println("Fetching data from: "+file.getAbsolutePath());
				}

			} else if (file.isDirectory()){

				fileList.addAll(getAllcsvFileListFromFolder(file.getAbsolutePath()));

			}

		}
		return fileList;

	}
	/**
	 * 
	 * @param fileName
	 * @param wifiList
	 * 
	 */
	private static void readFileAndAddToList(String fileName, LinkedList<WiFiLinkedList> wifiList ) {

		try {
			FileReader Fr = new FileReader(fileName);
			BufferedReader BR= new BufferedReader(Fr); 
			int count=0;
			WiFiLinkedList wll = new WiFiLinkedList();
			String Line = BR.readLine();
			String[] firstLine = Line.split(",");
			String UID = firstLine[5].substring(8);
			while(Line != null){
				if(count==0){
					if(!Line.contains("WigleWifi")){
						System.out.println("Error: File must be from WigleWifi ("+fileName+").");
						break;
					}
				}
				count++;
				if(count>2){
					String[] arr = Line.split(",");

					//Creating WIFILIST
					String time = arr[3];
					double alt = Double.parseDouble(arr[8]);
					double lat =  Double.parseDouble(arr[6]);
					double lon = Double.parseDouble(arr[7]);
					if (wifiList.size() == 0){
						wll = new WiFiLinkedList(lat, lon, alt, time, UID);
						wifiList.add(wll);
					}

					//System.out.println(Arrays.toString(arr));

					//Creating WiFi
					String SSID = arr[1],MAC = arr[0];
					double freq = Double.parseDouble(arr[4]) , signal = Double.parseDouble(arr[5]);
					WiFi wf = new WiFi(SSID,MAC ,freq ,signal);

					if(!wll.IsBelong(lat, lon, time)){
						wll = new WiFiLinkedList(lat, lon, alt, time, UID);
						wifiList.add(wll);		//adding WiFiLinkedList to LinkedList (ans)
					}
					wll.add(wf);		//adding WiFi to WiFiLinkedList
				}
				Line = BR.readLine();		//next line
			}
			BR.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public static void filtercsvFileByTime(String csvFile, String pathToWriteKML, String startTime, String endTime ){
		try {
			int lineYear, lineMonth, lineDay, lineHours, lineMins;
			int startYear, startMonth, startDay, startHours, startMins;
			int endYear, endMonth, endDay, endHours, endMins;
			startYear = Integer.parseInt(startTime.substring(0, 4));
			startMonth = Integer.parseInt(startTime.substring(5, 7));
			startDay = Integer.parseInt(startTime.substring(8, 10));
			endYear = Integer.parseInt(endTime.substring(0, 4));
			endMonth = Integer.parseInt(endTime.substring(5, 7));
			endDay = Integer.parseInt(endTime.substring(8, 10));

			FileReader fr = new FileReader(csvFile);
			BufferedReader br= new BufferedReader(fr); 
			ArrayList<String> filteredCSV = new ArrayList<String>();
			String brFirstLine = br.readLine();
			String brLine = br.readLine();
			String[] line = brLine.split(",");
			String timeColumn = line[0];
			String hmsStart = startTime.substring(11, timeColumn.length()-1); //hms = hour, minutes, seconds
			String hmsEnd = endTime.substring(11, timeColumn.length()-1);
			String lineDate = "";

			LocalTime lineTime = null;
			while(brLine != null){
				lineTime = LocalTime.parse(brLine.substring(11, timeColumn.length()-1));
				lineDate = brLine.substring(0, 10);
				lineYear = Integer.parseInt(lineDate.substring(0, 4));
				lineMonth = Integer.parseInt(lineDate.substring(5, 7));
				lineDay = Integer.parseInt(lineDate.substring(8, 10));
				if(lineTime.isAfter(LocalTime.parse(hmsStart)) && lineTime.isBefore(LocalTime.parse(hmsEnd)) && lineYear>=startYear 
						&& lineYear<=endYear && lineMonth>=startMonth && lineMonth<=endMonth && lineDay>=startDay && lineDay<=endDay){
					filteredCSV.add(brLine);
					brLine = br.readLine();
				}
				else brLine = br.readLine();
			}
			fr.close();
			br.close();

			ArrayList<String[]> filtered = new ArrayList<String[]>();
			for(int i = 0; i < filteredCSV.size(); i++)
			{
				filtered.add(filteredCSV.get(i).split(","));
			}

			printToKML(filtered, pathToWriteKML);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static void printToKML(ArrayList<String[]> list, String path) {
		HashMap<Integer, String> mac = new HashMap<>();
		for(int i = 0; i < list.size(); i++)
		{
			mac.put(i, list.get(i)[7]);
		}

		String temp = "";
		String[] lineMark = {"#","","","","","","-200"};
		for(int i = 0; i < list.size(); i++)
		{
			temp = mac.get(i);
			for(int j = i + 1; j < list.size(); j++)
			{
				if(temp.equals(mac.get(j)) && i!=j)
				{
					if(Double.parseDouble(list.get(j)[6])<Double.parseDouble(list.get(i)[6]))
					{
						list.set(j, lineMark);
					}
					else list.set(i, lineMark);
				}
			}
		}
		//	printCSVFromArrayList("C:/Users/USER/Desktop/New folder//TimeBook.csv", list);
		PrintWriter pw = null;
		try {
			pw  = new PrintWriter(new File(path));
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		StringBuilder builder = new StringBuilder();
		//http://www.freepngimg.com/download/wifi/4-2-wi-fi-png-images.png
		String kmlHeader = " <kml xmlns=\"http://www.opengis.net/kml/2.2\">\n    <Document>\n       <name>csvToKml.kml</name> <open>1</open>\n "
				+ "      <Style id=\"red\">\n      <IconStyle>\n        <Icon>\n"
				+ "          <href>http://maps.google.com/mapfiles/ms/icons/red-dot.png</href>\n        </Icon>\n      </IconStyle>\n    </Style>\n<Style id=\"Magnifier\">\n      <IconStyle>\n        <Icon>\n          <href>https://images.vexels.com/media/users/3/132064/isolated/preview/27a9fb54f687667ecfab8f20afa58bbb-search-businessman-circle-icon-by-vexels.png</href>\n        </Icon>\n      </IconStyle>\n    </Style><Style id=\"exampleStyleDocument\">           <LabelStyle>\n           <color>ff0000cc</color>\n           </LabelStyle>\n         </Style>\n\n       <Style id=\"transBluePoly\">\n      <LineStyle>\n        <width>1.5</width>\n      </LineStyle>\n      <PolyStyle>\n        <color>7dff0000</color>\n      </PolyStyle>\n    </Style> <Folder><name>Wifi Networks</name>";
		builder.append(kmlHeader);
		for(int i = 0; i < list.size(); i++)
		{
			if(list.get(i)!=lineMark)
			{
				builder.append(kmlWifiCoordinateGenerator(list.get(i)[3], list.get(i)[2], list.get(i)[8], list.get(i)[6]));
			}
			if(i+1==list.size()){
				builder.append("</Folder>\n</Document>\n</kml>");
				pw.write(builder.toString());
				pw.close();
			}
		}
	}

	public static void filtercsvFileByGPS(String csvFile, String pathToWriteKML, double lonStart, double latStart, double lonEnd, double latEnd){

		try {
			double lineLon, lineLat;
			FileReader fr = new FileReader(csvFile);
			BufferedReader br= new BufferedReader(fr); 
			ArrayList<String> filteredCSV = new ArrayList<String>();
			String brFirstLine = br.readLine();
			String brLine = br.readLine();
			String[] line = brLine.split(",");
			while(brLine != null){
				lineLat = Double.parseDouble(line[2]);
				lineLon= Double.parseDouble(line[3]);
				if(lineLat>=latStart && lineLat<latEnd && lineLon>=lonStart && lineLon<lonEnd)
				{
					filteredCSV.add(brLine);
					brLine = br.readLine();
				}
				else brLine = br.readLine();
			}
			fr.close();
			br.close();

			ArrayList<String[]> filtered = new ArrayList<String[]>();
			for(int i = 0; i < filteredCSV.size(); i++)
			{
				filtered.add(filteredCSV.get(i).split(","));
			}

			printToKML(filtered, pathToWriteKML);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param lon = Placemark longitude
	 * @param lat = Placemark latitude
	 * @param pointName = Placemark name
	 * @param Desc = Placemark description
	 * @return string
	 */
	private static String kmlWifiCoordinateGenerator(String lon,String lat,String pointName, String Desc){// adds one point to the kml - used for the wifi networks (with description)
		String all = "<Placemark>\n           <name>"+pointName+"</name>\n           <description>"+ Desc+"</description>\n "
				+ "          <styleUrl>#red</styleUrl>\n           <Point>\n                   "
				+ "         <coordinates>";
		all+= lon+","+lat+"</coordinates>\n           </Point>\n       </Placemark>\n\n		";


		return all;

	}

	private static void swap(WiFi[] arr, int i, int j){
		WiFi temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	}

	/**
	 * 
	 * @param CSVFile
	 * @param wifiList
	 */
	private static void printCSVFromWiFiLinkedList(String CSVFile, LinkedList<WiFiLinkedList> wifiList)
	{
		PrintWriter pw = null;
		try {
			pw  = new PrintWriter(new File(CSVFile));
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		StringBuilder builder = new StringBuilder();
		String ColumnNamesList = "TIME,ID,LAT,LON,ALT,WIFI NETWORK,SIGNAL1,MAC1,SSID1,FREQNCY1,SIGNAL2,MAC2,SSID2,FREQNCY2,SIGNAL3,MAC3,SSID3,FREQNCY3,SIGNAL4,MAC4,SSID4,FREQNCY4,SIGNAL5,MAC5,SSID5,FREQNCY5,SIGNAL6,MAC6,SSID6,FREQNCY6,SIGNAL7,MAC7,SSID7,FREQNCY7,SIGNAL8,MAC8,SSID8,FREQNCY8,SIGNAL9,MAC9,SSID9,FREQNCY9,SIGNAL10,MAC10,SSID10,FREQNCY10";
		for (int i = 0; i < wifiList.size(); i++) {
			if (i==0){
				builder.append(ColumnNamesList +"\n");}
			else{ builder.append(wifiList.get(i));
			builder.append('\n');}
			if(i+1==wifiList.size()){
				pw.write(builder.toString());
				pw.close();
			}
		}
		int slashIndex = 0;
		int count = 0;
		String s = "";
		String slash = "\\";
		while(!s.equals(slash)  || s.equals("/"))
		{
			s = CSVFile.substring(slashIndex, slashIndex+1);
			slashIndex = CSVFile.length() - count - 1;
			count++;
		}
		System.out.println("Created "+CSVFile.substring(slashIndex + 2, CSVFile.length())+" successfuly");
	}
	private static void printCSVFromArrayList(String CSVFile, ArrayList<String[]> list)
	{
		PrintWriter pw = null;
		try {
			pw  = new PrintWriter(new File(CSVFile));
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		StringBuilder builder = new StringBuilder();
		String ColumnNamesList = "TIME,ID,LAT,LON,ALT,WIFI NETWORK,SIGNAL1,MAC1,SSID1,FREQNCY1,SIGNAL2,MAC2,SSID2,FREQNCY2,SIGNAL3,MAC3,SSID3,FREQNCY3,SIGNAL4,MAC4,SSID4,FREQNCY4,SIGNAL5,MAC5,SSID5,FREQNCY5,SIGNAL6,MAC6,SSID6,FREQNCY6,SIGNAL7,MAC7,SSID7,FREQNCY7,SIGNAL8,MAC8,SSID8,FREQNCY8,SIGNAL9,MAC9,SSID9,FREQNCY9,SIGNAL10,MAC10,SSID10,FREQNCY10";
		for (int i = 0; i < list.size(); i++) {
			if (i==0){
				builder.append(ColumnNamesList +"\n");
			}
			else{
				String temp = Arrays.toString(list.get(i));
				builder.append(temp);
				builder.append('\n');
			}
			if(i+1==list.size()){
				pw.write(builder.toString());
				pw.close();
			}
		}
	}
	private static WiFi[] Best10WIFI(WiFi[] ans) {
		boolean flag= true;
		for (int i = 0; i < ans.length && flag; i++) {
			flag=false;
			for (int j = 0; j < ans.length-1; j++) {
				if(ans[j].getSignal()<ans[j+1].getSignal() ){
					flag=true;
					swap(ans,j,j+1);
				}
			}
		}
		WiFi [] first10= new WiFi[Math.min(10,ans.length)];
		for (int i = 0; i < first10.length; i++) {
			first10[i]=ans[i];
		}
		return first10;
	}

	/**
	 * 
	 * @param directoryName directory to collect all csv files from
	 * @param csvWritePath path to write the merged csv file
	 */
	public static void getAllcsvFilesFromFolderAndAddtoOneCSVTable(String directoryName, String csvWritePath)
	{
		ArrayList<String> fileList = getAllcsvFileListFromFolder(directoryName);
		LinkedList<WiFiLinkedList> wifiList = new LinkedList<WiFiLinkedList>();
		for (String csvFileName : fileList) {
			readFileAndAddToList(csvFileName, wifiList);
		}

		for (int i = 0; i < wifiList.size(); i++) {
			WiFi[] result = Best10WIFI(wifiList.get(i).getArrWiFi());
			wifiList.get(i).setWiFiList(result);    
		}
		printCSVFromWiFiLinkedList(csvWritePath, wifiList);
	}

	public static void run()
	{
		System.out.println("Select a folder to scan for csv files: ");
		Scanner folderScanner = new Scanner(System.in);
		String folder = folderScanner.nextLine();
		System.out.println("Enter path to write the CSV file: ");
		Scanner csvScanner = new Scanner(System.in);
		String csvWritePath = folderScanner.nextLine();
		getAllcsvFilesFromFolderAndAddtoOneCSVTable(folder, csvWritePath);
		csvScanner.close();
		folderScanner.close();
		System.out.println("Create a KML file Sorted by (1)Time, (2)GPS, (3)ID: ");
		Scanner sort = new Scanner(System.in);
		int option = Integer.parseInt(sort.nextLine());
		switch(option)
		{
		case 1: {
			System.out.println("Filter by time syntax\n Start time: year:month:day:hr:min:sec \n End time: year:month:day:hr:min:sec");
			System.out.println("Enter path to write the KML file: ");
			Scanner kmlPathScan = new Scanner(System.in);
			String kmlPath = kmlPathScan.nextLine();
			System.out.println("Start time: ");
			Scanner startTimeScan = new Scanner(System.in);
			String startTime = startTimeScan.nextLine();
			System.out.println("End time: ");
			Scanner endTimeScan = new Scanner(System.in);
			String endTime = endTimeScan.nextLine();
			filtercsvFileByTime(csvWritePath, kmlPath,  startTime, endTime);
			System.out.println("Success!");
			kmlPathScan.close();
			startTimeScan.close();
			endTimeScan.close();
			break;
		}
		case 2: {
			System.out.println("Enter path to write the KML file: ");
			Scanner kmlPathScan = new Scanner(System.in);

			kmlPathScan.close();
			break;
		}
		case 3: {
			System.out.println("Enter path to write the KML file: ");
			Scanner kmlPathScan = new Scanner(System.in);

			kmlPathScan.close();
			break;
		}
		}
		sort.close();
	}
	public static void main(String[] args) {


//		String folder = "C:\\Users\\USER\\Desktop\\data\\New folder";
//		String csvWritePath = "C:\\Users\\USER\\Desktop\\data\\New folder\\merged csv file.csv";

		//run();

//
//		String CSVFile="C:/Users/USER/Desktop/New folder/newBook.csv";
//		String kmlFolder="C:\\Users\\USER\\Desktop\\New folder\\BookTime.kml";


		System.out.println("Select a folder to scan for csv files: ");
		Scanner folderScanner = new Scanner(System.in);
		String folder = folderScanner.nextLine();
		System.out.println("Enter path to write the CSV file: ");
		Scanner csvScanner = new Scanner(System.in);
		String csvWritePath = folderScanner.nextLine();
		getAllcsvFilesFromFolderAndAddtoOneCSVTable(folder, csvWritePath);
		csvScanner.close();
		folderScanner.close();
		System.out.println("Create a KML file Sorted by (1)Time, (2)GPS, (3)ID: ");
		Scanner sort = new Scanner(System.in);
		int option = Integer.parseInt(sort.nextLine());
		switch(option)
		{
		case 1: {
			System.out.println("Filter by time syntax\n Start time: year:month:day:hr:min:sec \n End time: year:month:day:hr:min:sec");
			System.out.println("Enter path to write the KML file: ");
			Scanner kmlPathScan = new Scanner(System.in);
			String kmlPath = kmlPathScan.nextLine();
			System.out.println("Start time: ");
			Scanner startTimeScan = new Scanner(System.in);
			String startTime = startTimeScan.nextLine();
			System.out.println("End time: ");
			Scanner endTimeScan = new Scanner(System.in);
			String endTime = endTimeScan.nextLine();
			filtercsvFileByTime(csvWritePath, kmlPath,  startTime, endTime);
			System.out.println("Success!");
			kmlPathScan.close();
			startTimeScan.close();
			endTimeScan.close();
			break;
		}
		case 2: {
			System.out.println("Enter path to write the KML file: ");
			Scanner kmlPathScan = new Scanner(System.in);

			kmlPathScan.close();
			break;
		}
		case 3: {
			System.out.println("Enter path to write the KML file: ");
			Scanner kmlPathScan = new Scanner(System.in);

			kmlPathScan.close();
			break;
		}
		}
		sort.close();
	
		
		System.out.println("done!");
	}


}

