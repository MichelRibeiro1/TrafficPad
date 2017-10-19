package com.clubederobotica.trafficpad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class TelaInicialActivity extends AppCompatActivity {

    private static final int SOLICITA_BT = 1;
    private static final int SOLICITA_CONEXAO = 2;
    public static ConnectedThread connectedThread;

    public static boolean conexaoBT = false;
    BluetoothAdapter myBtAdapter = null;
    BluetoothDevice myBtDevice = null;
    BluetoothSocket myBTSocket = null;
    private static  String MAC_ADDRESS2 = null;
    UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_inicial);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //config BT

        myBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if(myBtAdapter == null){
            Toast.makeText(getApplicationContext(), "Sem support para BT", Toast.LENGTH_LONG).show();
        }else if(!myBtAdapter.isEnabled()){
            Intent intent_enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent_enableBT, SOLICITA_BT);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(conexaoBT){
                //desconectar BT
                try {
                    myBTSocket.close();
                    conexaoBT = false;
                    Toast.makeText(getApplicationContext(), "Desconectado do  MAC " + MAC_ADDRESS2, Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    conexaoBT = false;
                    Toast.makeText(getApplicationContext(), "Desconectar erro: " + e, Toast.LENGTH_LONG).show();
                }

            }else{
                //conectar BT
                Intent openDevicesList = new Intent(TelaInicialActivity.this, DevicesList.class);
                startActivityForResult(openDevicesList, SOLICITA_CONEXAO);
            }
            }
        });

        Button btn_abrir_app = (Button) findViewById(R.id.botao_iniciar_app);

        btn_abrir_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TelaInicialActivity.this, TelaFuncionalActivity.class);
//                finish();
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case SOLICITA_BT:
                if(resultCode == Activity.RESULT_OK){
                    Toast.makeText(getApplicationContext(), "BT ativado", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "BT não foi ativado, fechando app", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case SOLICITA_CONEXAO:
                if(resultCode == Activity.RESULT_OK){
                    MAC_ADDRESS2 = data.getExtras().getString(DevicesList.MAC_ADDRESS);
                    myBtDevice = myBtAdapter.getRemoteDevice(MAC_ADDRESS2);
                    try{
                        myBTSocket = myBtDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                        myBTSocket.connect();
                        conexaoBT = true;
                        connectedThread = new ConnectedThread(myBTSocket);
                        connectedThread.start();
                        Toast.makeText(getApplicationContext(), "Conectado ao MAC " + MAC_ADDRESS2, Toast.LENGTH_LONG).show();

                    }catch (IOException erro){
                        conexaoBT = false;
                        Toast.makeText(getApplicationContext(), "Erro ao conectar: " + erro, Toast.LENGTH_LONG).show();

                    }

                }else{
                    Toast.makeText(getApplicationContext(), "Falha ao obter o MAC", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tela_inicial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_close:
                finish();
                break;
            case R.id.action_sobre:
                Toast.makeText(this, "@TrafficPad, versão 2.0. Disponha!", Toast.LENGTH_LONG).show();
                break;
        }

        if (id == R.id.action_sobre) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void sendAlerta(String alertaMsg) {
        connectedThread.write(alertaMsg);
    }

    public static int getSolicitaBt() {
        return SOLICITA_BT;
    }

    public static boolean isConexaoBT() {
        return conexaoBT;
    }

    public static class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            //myBTSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                    //      .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String comando) {
            byte[] comandoBuffer = comando.getBytes();
            try {
                mmOutStream.write(comandoBuffer);
            } catch (IOException e) { }
        }
    }
}
