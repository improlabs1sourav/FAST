package com.improlabs.auth.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

public class PropertyUtil
{
   private  Properties configProp;
    
   private static PropertyUtil propertyUtil;
   
   private PropertyUtil()
   {
      //Private constructor to restrict new instances
      //InputStream in = this.getClass().getClassLoader().getResourceAsStream("app.properties");
	   
	   
      System.out.println("Read all properties from file");
      try {
    	  InputStream in = new FileInputStream(new File("E:\\Workspace\\FAST\\dist\\app.properties"));
    	  configProp=new Properties();

          configProp.load(in);
          
      } catch (IOException e) {
          e.printStackTrace();
      }
   }
 
 
   public static PropertyUtil getInstance()
   {
     if(propertyUtil==null)
     {
    	 propertyUtil=new PropertyUtil();
     }
     return propertyUtil;
     
   }
    
   public String getProperty(String key){
      return configProp.getProperty(key);
   }
   
   public String getProperty(String group,String key){
	      return configProp.getProperty(group+"."+key);
   }
    
   public Set<String> getAllPropertyNames(){
      return configProp.stringPropertyNames();
   }
    
   public boolean containsKey(String key){
      return configProp.containsKey(key);
   }
}
