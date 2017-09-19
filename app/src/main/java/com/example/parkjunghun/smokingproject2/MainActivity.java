package com.example.parkjunghun.smokingproject2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static String address = "98:D3:32:30:F2:76";
    public static final int REQUEST_ENABLE_BT = 0;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();

    Button onBtn, offBtn;
    private ConnectedThread mConnectedThread;
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        Intent intent = getIntent();
        String username = intent.getExtras().getString("nickname");
        actionBar.setTitle(username+"hi");
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTrasaction = fm.beginTransaction();
        fragmentTrasaction.replace(R.id.fragment, new EmptyFragment());
        fragmentTrasaction.commit();

        onBtn = (Button) findViewById(R.id.open);
        offBtn = (Button) findViewById(R.id.close);

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        if(checkBTState()){
            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            try {
                btSocket = createBluetoothSocket(device);
            } catch (Exception e) {
                errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
            }
            btAdapter.cancelDiscovery();

            try {
                btSocket.connect();
            } catch (Exception e) {
                try {
                    btSocket.close();
                } catch (Exception e2) {
                    errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                }
            }

            mConnectedThread = new ConnectedThread(btSocket);
            mConnectedThread.start();
        }

        onBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("1");
                Toast.makeText(getApplicationContext(), "OPEN", Toast.LENGTH_SHORT).show();
            }
        });
        offBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("2");
                Toast.makeText(getApplicationContext(), "CLOSE", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    public void onResume() {
        super.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            btSocket.close();
        } catch (Exception e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private boolean checkBTState() {
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "지원안됌", Toast.LENGTH_SHORT).show();
            //errorExit("Fatal Error", "Bluetooth not support");
            return false;
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            }
            return true;
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getApplicationContext(), title + " - " + message, Toast.LENGTH_LONG).show();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (Exception e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void write(String message) {
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (Exception e) {
            }
        }
    }

    public void sharekakao(View v){
        try{
            final KakaoLink kakaoLink = KakaoLink.getKakaoLink(this);
            final KakaoTalkLinkMessageBuilder kakaoBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

            kakaoBuilder.addText("smokingproject 테스트다!");
            kakaoBuilder.addAppButton("앱실행/앱설치");
            kakaoLink.sendMessage(kakaoBuilder,this);

        }
        catch (KakaoParameterException e){
            e.printStackTrace();
        }
    }

}
