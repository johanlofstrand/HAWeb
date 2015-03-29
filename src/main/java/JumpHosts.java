/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/**
 * This program will demonstrate SSH through jump hosts.
 * Suppose that you don't have direct accesses to host2 and host3.
 *   $ CLASSPATH=.:../build javac JumpHosts.java
 *   $ CLASSPATH=.:../build java JumpHosts usr1@host1 usr2@host2 usr3@host3
 * You will be asked passwords for those destinations,
 * and if everything works fine, you will get file lists of your home-directory
 * at host3.
 *
 */

import com.jcraft.jsch.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.*;
 
public class JumpHosts {
  public static void main(String[] arg){
 
    try{
      JSch jsch = new JSch();
 
      Session session = null;
      String host = "78.68.64.95";
      String user = "root";
    
      session = jsch.getSession(user, host, 22);
      session.setPassword("");
      Properties config = new java.util.Properties();
      config.put("StrictHostKeyChecking", "no");
      session.setConfig(config);
      session.connect();
      System.out.println("The session has been established to "+user+"@"+host);
 
      int assinged_port = session.setPortForwardingL(0, "localhost", 2222);
      System.out.println("portforwarding: "+
                         "localhost:"+assinged_port+" -> "+host+":"+2222);
      Session sessionPi =
      jsch.getSession("pi", "127.0.0.1", assinged_port);
      config.put("StrictHostKeyChecking", "no");
      sessionPi.setConfig(config);
      sessionPi.setHostKeyAlias(host);
      sessionPi.connect(20000);
      
      
      Channel channel = session.openChannel("exec");
      
      String command = "ls";
      ((ChannelExec) channel).setCommand(command);

      // this is the key line that sets AgentForwading to true
    //  ((ChannelExec) channel).setAgentForwarding(true);

    // channel.setInputStream(null);
      ((ChannelExec) channel).setErrStream(System.err);

      channel.connect();
      System.out.println("Connected to pi: " + channel.isConnected());
      InputStream is = channel.getInputStream();
      
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String line;
      int index = 0;

      while ((line = reader.readLine()) != null)
      {
          System.out.println(++index + " : " + line);
      }
         
       channel.disconnect();
 
       session.disconnect();
    }
    catch(Exception e){
      System.out.println(e);
    }
  }
}

 
  class MyUserInfo implements UserInfo {
  	String passwd;
  	public MyUserInfo(String passwd){
  		this.passwd = passwd;
  	}
	public String getPassword(){
		return passwd; 
	}
    public boolean promptYesNo(String str){
      return false;
    }
    public String getPassphrase(){ return null; }
    public boolean promptPassphrase(String message){ return true; }
    public boolean promptPassword(String message){
    	return false;
    }
	@Override
	public void showMessage(String message) {
		// TODO Auto-generated method stub
		
	}
}