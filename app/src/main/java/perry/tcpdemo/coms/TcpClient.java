package perry.tcpdemo.coms;

import android.content.Intent;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

import perry.tcpdemo.funcs.FuncTcpClient;

/**
 * Created by perry Zhu on 2017-04-25.
 * Email: cloud_happy@163.com
 */

public class TcpClient implements Runnable{
    private String TAG = "TcpClient";
    private String  serverIP = "192.168.88.141";
    private int serverPort = 1234;
    private PrintWriter pw;
    private InputStream is;
    private DataInputStream dis;
    private boolean isRun = true;
    private Socket socket = null;
    byte buff[]  = new byte[4096];
    private String rcvMsg;
    private int rcvLen;



    public TcpClient(String ip , int port){
        this.serverIP = ip;
        this.serverPort = port;
    }

    public void closeSelf(){
        isRun = false;
    }

    public void send(String msg){
        pw.println(msg);
        pw.flush();
    }

    @Override
    public void run() {
        try {
            socket = new Socket(serverIP,serverPort);
            socket.setSoTimeout(5000);
            pw = new PrintWriter(socket.getOutputStream(),true);
            is = socket.getInputStream();
            dis = new DataInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
            Intent intent =new Intent();
            intent.setAction("error");
            intent.putExtra("error",e.toString());
            FuncTcpClient.context.sendBroadcast(intent);//将消息发送给主界面
            return;
        }
        while (isRun){
            try {
                rcvLen = dis.read(buff);
                if(rcvLen<=0){
                    Log.e(TAG, "run: 读到的信息长度是零" );
                    Intent intent =new Intent();
                    intent.setAction("error");
                    intent.putExtra("error","读到的信息长度是："+rcvLen+"，与服务器的连接异常了");
                    FuncTcpClient.context.sendBroadcast(intent);//将消息发送给主界面
                    return;
                }
                rcvMsg = new String(buff,0,rcvLen,"utf-8");
                Log.i(TAG, "run: 收到消息:"+ rcvMsg);
                Intent intent =new Intent();
                intent.setAction("tcpClientReceiver");
                intent.putExtra("tcpClientReceiver",rcvMsg);
                FuncTcpClient.context.sendBroadcast(intent);//将消息发送给主界面
                if (rcvMsg.equals("QuitClient")){   //服务器要求客户端结束
                    isRun = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }catch (StringIndexOutOfBoundsException e){

            }

        }
        try {
            pw.close();
            is.close();
            dis.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
