package ua.kiev.prog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.SQLOutput;
import java.util.Scanner;

class GetThread extends Thread{
    private int n;
    private User user;

    public GetThread(User user) {
        this.user = user;
    }

    @Override
    public void run(){

        try {
            while (!isInterrupted()){
                URL url = new URL("http://localhost:8888/get?from=" + n);
                HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                try (InputStream is = huc.getInputStream()){
                    int s = is.available();
                    if(s>0){
                        Gson gson = new GsonBuilder().create();
                        Message[] list = gson.fromJson(new BufferedReader(new InputStreamReader(is)),Message[].class);
                        for (Message m : list){
                            if(!m.getPrivate()) {
                                System.out.println(m);
                            } else {
                                if(m.getTo().equals(user.getLogin()))
                                    System.out.println(m);
                                }
                            n++;
                        }
                    }
                }
            }
        } catch (Exception ex) {
        ex.printStackTrace();
        return;
        }
    }
}

public class Main{
    static public CookieHandler cookieHandler;
    static CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    static Scanner sc = new Scanner(System.in);
    static User user;

    static User login(){
        while (true){
            try{
                System.out.println("Enter login: ");
                String login = sc.nextLine();
                System.out.println("Enter password: ");
                String password = sc.nextLine();
                User user = new User(login,password);

                int res = user.send("http://localhost:8888/login");
                if(res==400){
                    System.out.println("Wrong login or password");
                } else if(res!=200){
                    System.out.println("HTTP Error:" + res);
                }else {
                    System.out.println("Welcome" + login +"!");
                    return user;
                }
            } catch (IOException ex){
                System.out.println("Error" + ex.getMessage());
            }
        }
    }

    static int sendCommand(String url) throws IOException{
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setRequestMethod("POST");
        return con.getResponseCode();
    }
    static void userLogOut(){
        try{
            int res = sendCommand("http://localhost:8888/logOut");
            if(res !=200){
                System.out.println("Error" + res);
                return;
            }
            user = null;
            System.out.println("You have not login yet");
        } catch (IOException ex){
            System.out.println("Error: " + ex.getMessage());
            return;
        }

    }
    static void userStatus(){
        System.out.println("Enter name of user:");
        String name = sc.nextLine();
        try{
            int res = sendCommand("http://localhost:8888/status" + name);
            if(res ==200){
                System.out.println("User "+name+" is online");
                return;
            }else if(res==202){
                System.out.println("User "+name+"is offline");
                return;
            }else {
                System.out.println("Error" +res);
                return;
            }
        }catch (IOException ex){
            System.out.println("Error: "+ex.getMessage());
            return;
        }
    }

    static void privateMessage(){
        System.out.println("Write private message to: ");
        String pm = sc.nextLine();
        System.out.println("Write text: ");
        String text = sc.nextLine();
        Message m = new Message();
        m.setText(text);
        m.setFrom(user.getLogin());
        m.setTo(pm);
        m.setPrivate(true);
        try{
            int res = m.send("http://localhost:8888/add");
            if(res != 200){
                System.out.println("Error: "+ res);
                return;
            }
        } catch (IOException ex){
            System.out.println("Error: " + ex.getMessage());
            return;
        }
    }

    static void UserList(){
        try{
            URL url = new URL("http://localhost:8888/getUsers");
            HttpURLConnection huc = (HttpURLConnection)url.openConnection();
            try (InputStream is = huc.getInputStream()){
                int s = is.available();
                if(s>0){
                    Gson gson = new GsonBuilder().create();
                    User[] list = gson.fromJson(new BufferedReader(new InputStreamReader(is)), User[].class);
                    for(User user : list){
                        System.out.println(user);
                    }
                }

            }
        } catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Back to menu?");
        String back = sc.nextLine();
    }
    static void chat(String to, String url){
        System.out.println("Write 'MENU' if you want to back");
        System.out.println("Write a message: ");
        while (true){
            String text = sc.nextLine();
            if(text.isEmpty()||text.equals("MENU"))
                break;
            Message m = new Message();
            m.setText(text);
            m.setFrom(user.getLogin());
            m.setTo(to);
            m.setPrivate(false);
            try {
                int res = m.send(url);
                if(res!=200){
                    System.out.println("Error: " + res);
                    return;
                }
            }catch (IOException ex){
                System.out.println("Error: "+ ex.getMessage());
                return;
            }
        }
    }
    static void roomChat(){
        System.out.println("Create new room enter- NEW");
        System.out.println("Enter to exit- ENTER");
        String action = sc.nextLine();
        System.out.println("Write a name of the room");
        String roomName = sc.nextLine();
        switch (action){
            case "NEW":{
                System.out.println("Enter users: ");
                String text = sc.nextLine();
                Message m = new Message();
                m.setText(text);
                m.setFrom(user.getLogin());
                m.setTo("new " + roomName);
                m.setPrivate(false);
                try {
                    int res = m.send("http://localhost:8888/room");
                    if(res!=200){
                        System.out.println("Error :"+res);
                        return;
                    }
                    System.out.println("The chat room was created");
                }catch (IOException ex){
                    System.out.println("Error: "+ex.getMessage());
                    return;
                }
                break;
            }
            case "ENTER": {
                String url = "http://localhost:8888/room";
                chat(roomName,url);
                break;
            }
        }

    }

    public static void main(String[] args) {
        cookieHandler = AccessController.doPrivileged(new PrivilegedAction<CookieHandler>(){
            public CookieHandler run(){
                return CookieHandler.getDefault();
            }
        });
        cookieHandler.setDefault(cookieManager);

        while (true){
            user = login();
            GetThread th = new GetThread(user);
            th.setDaemon(true);
            th.start();
            while (true){
                System.out.println();
                System.out.println("Chat - enter > CHAT");
                System.out.println("Users - enter > USERS");
                System.out.println("Private message - enter > PRIVATE");
                System.out.println("Users online - enter > STATUS");
                System.out.println("Logout - enter > EXIT");
                System.out.println();

                String action = sc.nextLine();
                switch (action){
                    case "CHAT":{
                        chat("all","http://localhost:8888/add");
                        break;
                    }
                    case "USERS":{
                        UserList();
                        break;
                     }
                    case "PRIVATE":{
                        privateMessage();
                        break;
                    }
                    case "ROOM": {
                        roomChat();
                        break;
                    }
                    case "STATUS":{
                        userStatus();
                        break;
                    }
                    case "EXIT":{
                        th.interrupt();
                        userLogOut();
                        break;
                    }
                    default:{
                        System.out.println("Enter command, please!");
                    }
                }
                if(action.equals("EXIT"))
                    break;
            }
        }
    }
}
