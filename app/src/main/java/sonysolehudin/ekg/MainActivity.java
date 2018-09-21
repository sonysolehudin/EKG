package sonysolehudin.ekg;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    LineChart grafik;
    Button lihat_sinyal_a;
    Spinner daftar;
    String alamat = "";
    ArrayList<Entry> antrian1 = new ArrayList<>();
    boolean pisah = true;
    File sd_card;
    OutputStream simpan;
    String tampung1,data_pasien;
    int total=0,count=0,hitung,eks=0;
    float tampung=0;
    boolean hasil = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        grafik = (LineChart) findViewById(R.id.tampil);
        lihat_sinyal_a = (Button) findViewById(R.id.lihatsinyal);
        daftar = (Spinner) findViewById(R.id.daftar_id);

        if (Build.VERSION.SDK_INT >= 15){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED&&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                /*if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)&&
                        ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {}
                else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }*/}
        }

        lihat_sinyal_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                if (pisah) {
                    ///Download
                    data_pasien = daftar.getSelectedItem().toString();
                    if(((data_pasien.equals("bs10089601")) || (data_pasien.equals("ss25029501"))) ||
                            ((data_pasien.equals("bs10089601-anonim")) || (data_pasien.equals("ss25029501-anonim")))) {hitung = 4;}
                    if(((data_pasien.equals("bs10089602")) || (data_pasien.equals("ss25029502"))) ||
                            ((data_pasien.equals("bs10089602-anonim")) || (data_pasien.equals("ss25029502-anonim")))) {hitung = 2;}
                    if(((data_pasien.equals("bs10089603")) || (data_pasien.equals("ss25029503"))) ||
                            ((data_pasien.equals("bs10089603-anonim")) || (data_pasien.equals("ss25029503-anonim")))) {hitung = 1;}
                    alamat = "http://192.168.137.1/EKG/" + data_pasien + ".txt";
                    new unduh().execute(alamat);
                    if(!antrian1.isEmpty()) {
                        grafik.clear();
                        antrian1.clear();
                    }
                    lihat_sinyal_a.setText("Display Signal");
                    pisah = false;
                    /*if(hasil) {
                        lihat_sinyal_a.setText("Lihat Sinyal");
                        pisah = false;
                    }*/
                }
                else {
                    ///Tampilkan
                    try{
                        LineDataSet siaptampil;
                        eks = 0;
                        BufferedReader scan = new BufferedReader(new FileReader(sd_card));
                        while((tampung1 = scan.readLine())!= null) {
                            eks = eks + hitung;
                            tampung = Float.parseFloat(tampung1);
                            antrian1.add(new Entry(eks,tampung));
                        }
                        siaptampil = new LineDataSet(antrian1, data_pasien);
                        siaptampil.setDrawCircles(false);
                        siaptampil.setColor(Color.BLUE);
                        LineData plotdata = new LineData(siaptampil);
                        grafik.setData(plotdata);
                        grafik.invalidate();
                        Log.e("EKG","Berhasil tampil");
                        lihat_sinyal_a.setText("Download");
                        pisah = true;
                    }
                    catch(FileNotFoundException i){Log.e("EKG",i.getMessage());}
                    catch(IOException f) {
                        Log.e("EKG",f.getMessage());
                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();}
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private class unduh extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();}
        @Override
        protected String doInBackground(String... f_url) {
            String tes = f_url[0];
            HttpURLConnection koneksi;
            try {
                URL link = new URL(tes);
                koneksi = (HttpURLConnection) link.openConnection();
                koneksi.setRequestMethod("GET");
                koneksi.setReadTimeout(1000);
                koneksi.setConnectTimeout(2000);
                koneksi.connect();
                sd_card = new File (Environment.getExternalStorageDirectory(), data_pasien+".txt");
                InputStream baca = new BufferedInputStream(link.openStream(),8192);
                byte[] data = new byte [koneksi.getContentLength()];
                simpan = new FileOutputStream(sd_card);
                while((count = baca.read(data))!=-1) {
                    total = total+count;
                    //Log.e("EKG",String.valueOf(total));
                    simpan.write(data,0,count);
                }
                hasil = true;
                Log.e("EKG","Berhasil download");
                baca.close();
                simpan.close();
            }
            catch(Exception e){
                Log.e("EKG", e.getMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(String zzz) {
            super.onPostExecute(zzz);
            if(hasil) {
                Toast.makeText(getApplicationContext(), "Download Complete", Toast.LENGTH_LONG).show();}
            else {
                Toast.makeText(getApplicationContext(), "Download Failed", Toast.LENGTH_LONG).show();}
        }
    }
}

