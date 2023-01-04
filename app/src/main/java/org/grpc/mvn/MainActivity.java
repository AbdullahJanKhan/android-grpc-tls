package org.grpc.mvn;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;

import org.grpc.mvn.databinding.ActivityMainBinding;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.tls.HandshakeCertificates;
import okhttp3.tls.HeldCertificate;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        try{
            Resources res = getResources();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream instream = res.openRawResource(R.raw.ca_cert);
            Certificate ca = cf.generateCertificate(instream);
            KeyStore kStore = KeyStore.getInstance(KeyStore.getDefaultType());
            kStore.load(null, null);
            kStore.setCertificateEntry("ca", ca);
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(kStore);
            TrustManager[]  trustManagers = tmf.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            SSLSocketFactory sslSocketFactory = context.getSocketFactory();
            final ManagedChannel channel = OkHttpChannelBuilder
                    .forAddress("IP",PORT)
                    .useTransportSecurity()
                    .overrideAuthority("IP:PORT")
                    .sslSocketFactory(sslSocketFactory)
                    .build();
            BookGrpc.BookBlockingStub stub = BookGrpc.newBlockingStub(channel);
            BookItemReq requestData = BookItemReq
                    .newBuilder()
                    .setName("Ok HTTP TLS")
                    .setAuthor("Abdullah Jan Khan")
                    .setDescription("Abdullah Jan Khan OK Http TLS test. Test Round 5. Final")
                    .build();
            BookItem response = stub.createBook(requestData);
            System.out.println(response);
            System.out.println("=========================");
            channel.shutdown();
        } catch (Exception e){
            e.printStackTrace();
        }
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

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}