package sadappp.myapplication.model3D.util;

import android.content.Context;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mende on 10/30/2017.
 */

public class AmazonS3Helper {

    //ALL REQUIRED FOR DOWNLOADING FILES IN S3
    private CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider;
    private AmazonS3 s3Client;
    private static final String BUCKET_NAME = "noni1995";
    private static TransferUtility transferUtility;
    private static TransferObserver observer;
    FileOutputStream fos;

    public static TransferUtility getTransferUtility() {
        return transferUtility;
    }

    public void download(Context context, String OBJECT_KEY, File MY_FILE) {

        System.out.println("🎬 Iniating download for (requestedFile) on menu");

        s3credentialsProvider(context);
        downloadS3(BUCKET_NAME,OBJECT_KEY,MY_FILE);
        //or downloadFileFromS3(final String myKey, final String file)
    }

    /**
     * Gets an instance of CognitoCachingCredentialsProvider which is
     * constructed using the given Context.
     *
     * @param context An Context instance.
     * @return A default credential provider.
     */
    public void s3credentialsProvider(Context context){
            // Initialize the Amazon Cognito credentials provider
            this.cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    "us-east-1:0114edd2-0c3a-4f9b-ba39-8e1b32a9071b", // Identity pool ID
                    Regions.US_EAST_1 // Region
            );
            createAmazonS3Client(cognitoCachingCredentialsProvider, context);
        }

        /**
         * Create a AmazonS3Client constructor and pass the credentialsProvider.
         * @param credentialsProvider
         */
        public void createAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider, Context context) {
         // Create an S3 client
            this.s3Client = new AmazonS3Client(credentialsProvider);

          // Set the region of your S3 bucket
            this.s3Client.setRegion(Region.getRegion(Regions.US_EAST_1));

            this.transferUtility = new TransferUtility(s3Client, context.getApplicationContext());
    }

    //Hopefully i'll be able to use this
    public void downloadS3(String MY_BUCKET, String OBJECT_KEY, File MY_FILE) {
            System.out.println("File Path : " + MY_FILE);
        this.observer = transferUtility.download(
                MY_BUCKET,     /* The bucket to download from */
                OBJECT_KEY,    /* The key for the object to download */
                MY_FILE        /* The file to download the object to */
        );

        observer.setTransferListener(new TransferListener(){

            @Override
            public void onStateChanged(int id, TransferState state) {
                // do something
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
               // int percentage = (int) (bytesCurrent/bytesTotal * 100);
                //Display percentage transfered to user
            }

            @Override
            public void onError(int id, Exception ex) {
                // do something
            }

        });
    }


    //WE CALL THIS TO DOWNLOAD FILES INTO INTERNAL STORAGE AT DATA/DATA/SADAPPP.MYAPPLICATION/FILES
    public void downloadFileFromS3(final String myKey, final String file) {

        S3Object o = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, myKey));

        try {
            //         fos = openFileOutput(file, Context.MODE_PRIVATE);
            InputStream s3is = o.getObjectContent();
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    //cancel ALL current downloads
    public void cancel(){
        transferUtility.cancelAllWithType(TransferType.DOWNLOAD);
    }
}
