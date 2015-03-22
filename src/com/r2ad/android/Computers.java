package com.r2ad.android;

import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.util.Log;


public class Computers {
	
	static final String keystoreFilename = "keystore.android";
    //TODO: Use secure data source
    static final String keystorePassword = "internaluseonly";
    
	static String OCCIEndPoint = "https://dashboard.compatibleone.fr:8086/publication/";
    //static String OCCIEndPoint = "https://demo.compatibleone.fr:8094/compute/";
	static final String urlEncodedAccount = "YWNjb3JkczpwbGF0Zm9ybQ=="; //accords:platform
	static final int portNumber = 8086;	
	
    /**
    static String OCCIEndPoint = "https://egi-cloud.zam.kfa-juelich.de:5000/";
	static final String urlEncodedAccount = "Y2F0Y2hhbGxfcGx1Z2Zlc3Q6ZVplaXllMWU="; //catchall_plugfest:eZeiye1e 
	static final int portNumber = 5000; 
	*/
    
    /**
     * Works, have to also use xauth.
    static String OCCIEndPoint = "https://egi-cloud.zam.kfa-juelich.de:8787/";
    //X-Auth-Token: c2f045010e364d7e862647c23973e3bc 
	static final String urlEncodedAccount = "c2f045010e364d7e862647c23973e3bc"; //catchall_plugfest:eZeiye1e 
	static final int portNumber = 5000;   
    */

	KeyStore trustStore = null;
	SSLSocketFactory socketFactory = null;
	Context context = null;

	public Computers (String gotoURL) {
		Log.d("Computers","input URL: " + OCCIEndPoint);		
	}
	
	// see: http://blog.antoine.li/2010/10/22/android-trusting-ssl-certificates/ 
	// and: http://stackoverflow.com/questions/4115101/apache-httpclient-on-android-producing-certpathvalidatorexception-issuername
    private void createSocketFactory() {
    	if (socketFactory != null) {
	        try {
				trustStore = KeyStore.getInstance(KeyStore.getDefaultType());	
	            // Get the raw resource, which contains the keystore with
	            // your trusted certificates (root and any intermediate certs)
				InputStream in = context.getResources().openRawResource(R.raw.keystore);			
	            try {
	                // Initialize the keystore with the provided trusted certificates
	                // Also provide the password of the keystore
	    			trustStore.load(in, keystorePassword.toCharArray());
	            } finally {
	                in.close();
	            }
	            // Pass the keystore to the SSLSocketFactory. The factory is responsible
	            // for the verification of the server certificate.
	            SSLSocketFactory sf = new SSLSocketFactory(trustStore);
	            // Hostname verification from certificate
	            // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
	            sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER); //ALLOW_ALL_HOSTNAME_VERIFIER);
	        } catch (Exception e) {
	            throw new AssertionError(e);
	        }
    	}
    }    

    public HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }
    
    public ArrayList<String> parseComputers(Context context) {
    	this.context = context;
		ArrayList<String> items = new ArrayList<String>();
		
		//
		// Note: A message is returned in conditions of errors or if nothing is found
		//
		
		// Use these two commands instead for trusted connections that use real certs:
    	// createSocketFactory();
        // DefaultHttpClient httpclient = new DefaultHttpClient();

    	
        HttpClient httpclient = getNewHttpClient(); //DefaultHttpClient();
        HttpGet httpget = new HttpGet(OCCIEndPoint);
		httpget.addHeader("User-Agent", "R2AD");
		httpget.addHeader("Content-Type", "occi/text");
		httpget.addHeader("Accept", "*/*");
		
		httpget.addHeader("Authorization", "Basic " + urlEncodedAccount);
	    //X-Auth-Token: c2f045010e364d7e862647c23973e3bc 
		httpget.addHeader("X-Auth-Token",  urlEncodedAccount);
		HttpResponse response = null;
		int resourceCount = 0;

        try { 
            System.out.println("Executing request to : " + OCCIEndPoint);
            response = httpclient.execute(httpget);      
            

            /**
             * Use this for debug purposes:
             *            
             */

            HttpEntity entity = response.getEntity();
            System.out.println("--------------------------------------------------------------");
            System.out.println("Status Line: " + response.getStatusLine());
            if (entity != null) {
                System.out.println("Response content length: " + entity.getContentLength());
            }
            
  		  //
  		  // Display the Body of the message
  		  //
          BufferedReader in = null;
          try {

              in = new BufferedReader
              (new InputStreamReader(response.getEntity().getContent()));
              StringBuffer sb = new StringBuffer("");
              String line = "";
              String NL = System.getProperty("line.separator");
              while ((line = in.readLine()) != null) {
                  sb.append(line + NL);
                  items.add(line);
                  if ( line.length() > 5) resourceCount++;
              }
              in.close();
              String page = sb.toString();
      		  //System.out.println("================================================================");
              //System.out.println(page);
      		  //System.out.println("================================================================");
              
          } finally {
              if (in != null) {
                  try {
                      in.close();
                      } catch (IOException e) {
                      e.printStackTrace();
                  }
              }
          }  		  
  		                 
        } catch (javax.net.ssl.SSLException ssle) {
            System.out.println("Keystore missing trust values for that site:  " + ssle);
            items.add("Need to trust URL first"); 
        } catch (java.net.UnknownHostException uhe) {
            items.add("Internet not available"); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
            items.add("trust store missing"); 			
		} catch (IOException e) {
			e.printStackTrace();
            items.add("trust store error"); 						
		} finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }		
		
		items.add("Discovered (" + resourceCount + ") resources.");
		return items;	            

	}	
}
