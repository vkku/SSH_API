package com.example.sshconnection;

import com.example.sshconnection.service.SSHConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class SshConnectionApplicationTests {

    @Autowired
    SSHConnection sshConnection;

    @Test
    void contextLoads() {
    }

    @Test
    public void testSendCommand()
    {
        System.out.println("sendCommand");

        /**
         * YOU MUST CHANGE THE FOLLOWING
         * FILE_NAME: A FILE IN THE DIRECTORY
         * USER: LOGIN USER NAME
         * PASSWORD: PASSWORD FOR THAT USER
         * HOST: IP ADDRESS OF THE SSH SERVER
         **/
        String command = "cat /etc/passwd";
        String userName = "USER_HERE";
        String password = "PASSWORD_HERE";
        String connectionIP = "IP_HERE";
        sshConnection.doCommonConstructorActions(userName, password, connectionIP, "");
        String errorMessage = sshConnection.connect();

        if(errorMessage != null)
        {
            System.out.println(errorMessage);
            fail();
        }

        Set<String> uniqueUsers = new HashSet<>();
        String expResult = "FILE_NAME\n";
        // call sendCommand for each command and the output
        //(without prompts) is returned
        String result = sshConnection.sendCommand(command);
        // close only after all commands are sent
        String userCapturePattern = "([^\\/])+$";
        Pattern p = Pattern.compile(userCapturePattern, Pattern.MULTILINE);
        Matcher m = p.matcher(result);
        while(m.find()) {
        	uniqueUsers.add(m.group());
        }
        
        uniqueUsers.forEach(user -> {
        	System.out.println(user);
        });

        sshConnection.close();
        //assertEquals(expResult, result);
    }
    
}
