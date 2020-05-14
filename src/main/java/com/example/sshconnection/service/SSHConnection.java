package com.example.sshconnection.service;


import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SSHConnection {
	
	@Autowired
	SSHConnection sshConnection;

        public static final Logger LOGGER =
                Logger.getLogger(SSHConnection.class.getName());
        public JSch jschSSHChannel;
        public String strUserName;
        public String strConnectionIP;
        public int intConnectionPort;
        public String strPassword;
        public Session sesConnection;
        public int intTimeOut;


        public void doCommonConstructorActions(String userName,
                                                String password, String connectionIP, String knownHostsFileName)
        {
            jschSSHChannel = new JSch();

            try
            {
                jschSSHChannel.setKnownHosts(knownHostsFileName);
            }
            catch(JSchException jschX)
            {
                logError(jschX.getMessage());
            }

            this.strUserName = userName;
            this.strPassword = password;
            this.strConnectionIP = connectionIP;
            this.intConnectionPort = 22;
            this.intTimeOut = 60000;
        }

        public String connect()
        {
            String errorMessage = null;

            try
            {
                sesConnection = jschSSHChannel.getSession(strUserName,
                        strConnectionIP, intConnectionPort);
                sesConnection.setPassword(strPassword);
                // UNCOMMENT THIS FOR TESTING PURPOSES, BUT DO NOT USE IN PRODUCTION
                sesConnection.setConfig("StrictHostKeyChecking", "no");
                sesConnection.connect(intTimeOut);
            }
            catch(JSchException jschX)
            {
                errorMessage = jschX.getMessage();
            }

            return errorMessage;
        }

        private String logError(String errorMessage)
        {
            if(errorMessage != null)
            {
                LOGGER.log(Level.SEVERE, "{0}:{1} - {2}",
                        new Object[]{strConnectionIP, intConnectionPort, errorMessage});
            }

            return errorMessage;
        }

        private String logWarning(String warnMessage)
        {
            if(warnMessage != null)
            {
                LOGGER.log(Level.WARNING, "{0}:{1} - {2}",
                        new Object[]{strConnectionIP, intConnectionPort, warnMessage});
            }

            return warnMessage;
        }

        public String sendCommand(String command)
        {
            StringBuilder outputBuffer = new StringBuilder();

            try
            {
                Channel channel = sesConnection.openChannel("exec");
                ((ChannelExec)channel).setCommand(command);
                InputStream commandOutput = channel.getInputStream();
                channel.connect();
                int readByte = commandOutput.read();

                while(readByte != 0xffffffff)
                {
                    outputBuffer.append((char)readByte);
                    readByte = commandOutput.read();
                }

                channel.disconnect();
            }
            catch(IOException ioX)
            {
                logWarning(ioX.getMessage());
                return null;
            }
            catch(JSchException jschX)
            {
                logWarning(jschX.getMessage());
                return null;
            }

            return outputBuffer.toString();
        }

        public void close()
        {
            sesConnection.disconnect();
        }
        
        public List<String> getUniqueUsers(String userName,
                String password, String connectionIP, String knownHostsFileName) {
        	
        	sshConnection.doCommonConstructorActions(userName, password, connectionIP, knownHostsFileName);
        	sshConnection.connect();
        	String result = sshConnection.sendCommand("cat /etc/passwd");
        	sshConnection.close();
        	
        	List<String> uniqueUserList = collectUniqueUsers(Optional.ofNullable(result).orElse(""));
        	
        	
        	return uniqueUserList;
        }
        
        public List<String> collectUniqueUsers(String commandExecutionResult) {

        	List<String> uniqueUserList = new ArrayList<>();
        	Set<String> uniqueUsers = new HashSet<>();
            String userCapturePattern = "([^\\/\n])+$";
            Pattern p = Pattern.compile(userCapturePattern, Pattern.MULTILINE);
            Matcher m = p.matcher(commandExecutionResult);
            while(m.find()) {
            	uniqueUsers.add(m.group());
            }
            
            uniqueUserList.addAll(uniqueUsers);
            return uniqueUserList;
            
        }

    }

