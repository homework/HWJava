/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package homework.nott.util;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pszgp, 13-17 august 2012
 */
public class HWDataToMySQL {
    
    private static final int MAX_RECORDS_SIZE = 1000;

    enum TABLES {
        //private String[] fields;
        Allowances (new String[]{"", ""}),// {public void setFields(String[] f) {fields = new String[]{""}};},
        Bwstats (),
        
        //@1252c94e41d07060@<|>17<|>169.254.107.207<|>53293<|>255.255.255.255<|>2223<|>1<|>114<|>
        Flows(new String[]{"last", "proto", "daddr", "dport", "saddr", "sport", "npkts", "nbytes"}),
        
        KFlows(new String[]{"last", "timestamp", "proto", "daddr", "dport", "saddr", "sport", "npkts", "nbytes"}), 
        
        //@1252e0caa6eb6d18@<|>c8:bc:c8:c5:31:52<|>-55.352941<|>0<|>18<|>9325<|>
        Links(new String[]{"last", "macaddr", "rssi", "npkts", "nretries", "nbytes"}),
        NFlows(new String[]{"last", "timestamp", "proto", "daddr", "dport", "saddr", "sport", "npkts", "nbytes"}),
        NotificationRequest (null),
        NotificationResponse (null),
        NoxCommand (null),
        NoxResponse (null),
        PolicyRequest (null),
        PolicyResponse (null),
        Sys (null),
        
        //@12532f5eae7713b0@<|>6<|>10.2.0.17<|>54424<|>69.93.144.21<|>80<|>click-management.com<|>/images/bg.gif<|>NULL<|>
        Urls(new String[]{"last", "proto", "daddr", "dport", "saddr", "sport", "hst", "uri", "cnt"}),
        UsersEvents(null);
        
        String[] fields;
        TABLES(){}        
        TABLES(String[] fields)
        {
            this.fields = fields;
        }        
        public String[] getFields()
        {
            return fields;
        }        
    }
    
    
    public void importDataCollection(String path)
    {
        //System.out.println("import data collection..."+path);
        File main = new File(path);
        String[] subfolders = main.list();
        for (String fname: subfolders)
        {
            File f = new File(path+"/"+fname);
            if (f.isDirectory())
            {
                importDirectory(f);
            }
        }
    }
    
    void importDirectory(File file)
    {
        //System.out.println("\nImport directory: "+file);
        File[] files = file.listFiles();
        //System.out.println("number of files: "+ files.length);
        for (File f: files)
        {
            //System.out.println(f);
            
            String table = f.getName();
            table = table.substring(0, table.indexOf("-"));
            //System.out.println(table);
            String[] fields = null;
            for (HWDataToMySQL.TABLES t: HWDataToMySQL.TABLES.values())
            {
                if (t.name().equals(table))
                    fields = t.getFields();
            }
            
            List<String[]> records = getRecordsTableFromDataFile(f.getAbsolutePath(), table, fields);
            //importTableToMySQL(records, table, fields);
            //exportTableToCSV(records, table, fields);
            //System.exit(0);
        }
        //System.exit(0);
    }
    
    public List<String[]> getRecordsTableFromDataFile(String pathFile, String table, String[] fields)
    {
        //System.out.println("get records from data file "+pathFile);
        List<String[]> records = new ArrayList<String[]>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(pathFile));
            String line = null;
            while ((line=br.readLine())!=null){
                //System.out.println("line "+line +" "+line.length());
                String[] items = line.split("<\\|>");
                //System.out.println(items.length+" "+items);
                //if (items.length >0)
                if (items==null) continue;
                if (items.length==0) continue;
                records.add(items);
                
                
                if (records.size() > this.MAX_RECORDS_SIZE)
                {
                    exportTableToCSV(records, table, fields);
                    records.clear();
                }
            }
            
            exportTableToCSV(records, table, fields);
            
            //printRecords(records);
            //if (records.size()>0)
            //    System.exit(0);
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Logger.getLogger(HWDataToMySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(HWDataToMySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
        //printRecords(records);
        //System.out.println("records: "+ records.size());
        return records;
    }
    
    /*public static void printRecords(List<String[]> records)
    {
        for (String[] record: records)
        {
            for (String r: record)
                System.out.print(r+" ");
            System.out.println();
        }
    }*/
    
    public void importTableToMySQL(List<String[]> records, String table, String... fields)
    {
        //System.out.println("import table "+table+" in mysql ");
        //System.out.println("number of records: " + records.size());
        if (fields == null)
        {   
            //System.out.println("empty set!");
            return;
        }
        //System.out.println("fields are: ");
        //for (String f: fields)
        //       System.out.print(f+",");
        //System.out.println();
        
    }
    static final String path = "data/";//London/";
    public void exportTableToCSV(List<String[]> records, String table, String... fields)
    {
        //System.out.println("export table "+table+" as csv ");
        //System.out.println("number of records: " + records.size());
        if ((records.size()==0) || (fields == null))
        {   
            //System.out.println("empty set!");
            return;
        }
        //System.out.println("fields are: ");
        //for (String f: fields)
        //       System.out.print(f+",");
        //System.out.println();                      
        
        int nrRecords = records.size();
        if (nrRecords>MAX_RECORDS_SIZE)
        {
            int nrRecordsSaved = 0;
            while (nrRecordsSaved <= nrRecords)
            {
                StringBuilder sb = new StringBuilder("");
                sb.setLength(65000);

                //add the fields if no records were added
                if (nrRecordsSaved == 0)
                {
                    for (int i=0;i<fields.length-1;i++)
                        sb.append(fields[i]+",");
                    sb.append(fields[fields.length-1]+"\n");
                }            
                int bound = nrRecordsSaved+MAX_RECORDS_SIZE;
                if (bound > nrRecords)
                    bound = nrRecords;
                List<String[]> recordsSet = records.subList(nrRecordsSaved, bound);// [) subset
                for (String[] record: recordsSet)
                {
                    for (int i=0;i<record.length-1;i++)
                        sb.append(record[i]+",");
                    sb.append(record[record.length-1]+"\n");
                }

                //System.out.println("csv text length = "+sb.length());
                
                appendRecordsToCSVTable(path, table, sb.toString()); 
                
                nrRecordsSaved += MAX_RECORDS_SIZE;
            }
        }
    }
    
    void appendRecordsToCSVTable(String path, String table, String text)
    {
        try{
            String file = path+table+".csv";
            //System.out.println(new File(file).getAbsolutePath());
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(text);
            bw.flush();
            bw.close();
        }catch(IOException e){
            e.printStackTrace();
        };
    }
    
    //20-21 august 2012
    //read flows from the flows csv file (data imported from hw in the format <|>field_value<|>)
    /*public TreeMap<Integer,TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Long>>>> getFlowsDeviceSumHours(String deviceIp) {
        
        ArrayList<TreeMap<String, Object>> ipFlows = 
                new ArrayList();//new CSVParser().getRecords("Flows");
        
        TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Long>>>> ipUsage = 
                new TreeMap();
        
        if (ipFlows!=null)
        {    for (TreeMap<String, Object> flow: ipFlows)
            {
                if (flow!=null)
                {
                    if ((flow.containsKey("nbytes")&&(flow.containsKey("last"))))
                    {
                        int value = (Integer)flow.get("nbytes");
                        long nbytes = value;
                        String last = (String)flow.get("last");
                        
                        Timestamp tstamp = HWTimestamp.getTimestampFromUnixString(last);
                        
                        //System.out.println(tstamp.getTime());
                        //System.out.println(tstamp.getHours());
                        //Date date = new Date(tstamp.getTime());
                        
                        //System.out.println("time: " + date);
                        
                        int hour = tstamp.getHours();
                        int day = tstamp.getDate();
                        int month = tstamp.getMonth()+1;//starts with 1
                        int year = 1900+tstamp.getYear();                       
                        
                        TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Long>>> ipUsageYear = new TreeMap();
                        if (ipUsage.containsKey(year))
                            ipUsageYear = ipUsage.get(year);     
                        
                        TreeMap<Integer, TreeMap<Integer, Long>> ipUsageMonth = new TreeMap();                        
                        if (ipUsageYear.containsKey(month))
                            ipUsageMonth = ipUsageYear.get(month);
                        
                        TreeMap<Integer, Long> ipUsageDay = new TreeMap();
                        
                        if (ipUsageMonth.containsKey(day))
                            ipUsageDay = ipUsageMonth.get(day);
                        
                        Long ipUsageHour = 0L;
                        if (ipUsageDay.containsKey(hour))
                            ipUsageHour = ipUsageDay.get(hour);
                        
                        ipUsageHour += nbytes;
                        
                        ipUsageDay.put(hour, ipUsageHour);
                        ipUsageMonth.put(day, ipUsageDay);
                        ipUsageYear.put(month, ipUsageMonth);
                        ipUsage.put(year, ipUsageYear);
                        
                    }
                }
            }
        }
        return ipUsage;
    }*/
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //new HWDataToMySQL().importDataCollection("D:/hwJavaSocket/London_data_txl/hwdatalondonv2 (1).tar/hwdata");
    }
}
