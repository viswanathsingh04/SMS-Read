package biggieconsulting.sms;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // GUI Widget
    Button btnSent, btnInbox, btnDraft;
    TextView lblMsg, lblNo;
    RecyclerView msgrecycler;
    ListView lvMsg;
    List<Bean> beans;
    // Cursor Adapter
    SimpleCursorAdapter adapter;

    private String title;
    private String folder;
    private String pages;
    private SharedPreferences sharedPref;
    private View rootView;

    private HashMap<String, String> metaMap;
    Bean bean;
    Boolean permission = false;
    public static final int MY_PERMISSIONS_REQUEST_READ_SMS = 99;
    SMSAdapter smsAdapter;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beans = new ArrayList<>();

        PreferenceManager.setDefaultValues(this, R.xml.user_settings, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // Init GUI Widget
        btnInbox = (Button) findViewById(R.id.btnInbox);
        btnInbox.setOnClickListener(this);

        btnSent = (Button) findViewById(R.id.btnSentBox);
        msgrecycler = findViewById(R.id.msgrecycler);
        btnSent.setOnClickListener(this);

        btnDraft = (Button) findViewById(R.id.btnDraft);
        btnDraft.setOnClickListener(this);

        lvMsg = (ListView) findViewById(R.id.lvMsg);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPDF();
                Snackbar.make(view, "Created successfully", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v == btnInbox) {
            Uri inboxURI = Uri.parse("content://sms/inbox");
            final Cursor cursor = getContentResolver().query(inboxURI,
                    new String[]{"_id", "address", "date", "body"},
                    null, null, null);
            assert cursor != null;
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String address = cursor.getString(1);
                String date = cursor.getString(2);
                String msg = cursor.getString(3);
                Log.i("msg", msg);
                bean = new Bean();
                bean.set_id(id);
                bean.setAddress(address);
                bean.setBody(msg);
                beans.add(bean);
            }
            Log.w("arraysize", String.valueOf(beans.size()));
            final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            msgrecycler.setLayoutManager(layoutManager);
            smsAdapter = new SMSAdapter(MainActivity.this, beans);
            msgrecycler.setAdapter(smsAdapter);

            /*
            String[] reqCols = new String[]{"_id", "address", "body"};
            // Get Content Resolver object, which will deal with Content
            // Provider
            ContentResolver cr = getContentResolver();
            // Fetch Inbox SMS Message from Built-in Content Provider
            Cursor c = cr.query(inboxURI, reqCols, null, null, null);
            // Attached Cursor with adapter and display in listview
            adapter = new SimpleCursorAdapter(this, R.layout.row, c, new String[]{"body", "address"}, new int[]{
                    R.id.lblMsg, R.id.lblNumber});
            lvMsg.setAdapter(adapter);*/
        }
    }

    private void createPDF() {

        // Output file
        title = sharedPref.getString("title", null);
        folder = sharedPref.getString("folder", "Android/data/biggieconsulting.pdf/");
        String outputPath = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                folder + title);

        // Run conversion
        final boolean result = convertToPdf(outputPath);

        // Notify the UI
        if (result) {
            Snackbar snackbar = Snackbar
                    .make(btnInbox, getString(R.string.toast_successfully), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.toast_open), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            File file = new File(helper_pdf.actualPath(MainActivity.this));
                            helper_main.openFile(MainActivity.this, file, "application/pdf", btnInbox);
                        }
                    });
            snackbar.show();
        } else
            Toast.makeText(this, getString(R.string.toast_successfully_not), Toast.LENGTH_SHORT).show();

    }

    private boolean convertToPdf(String outputPdfPath) {
        try {

            String paragraph = "This is testing message";
            //String paragraph = edit.getText().toString().trim();

            // Create output file if needed
            File outputFile = new File(outputPdfPath);
            if (!outputFile.exists()) outputFile.createNewFile();

            Document document;
            if (sharedPref.getString("rotateString", "portrait").equals("portrait")) {
                document = new Document(PageSize.A4);
            } else {
                document = new Document(PageSize.A4.rotate());
            }

            PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();
            document.add(new Paragraph(paragraph));
            //document.add(new List(paragraph));

            document.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean checkSmsPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_SMS)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_SMS},
                        MY_PERMISSIONS_REQUEST_READ_SMS);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_SMS},
                        MY_PERMISSIONS_REQUEST_READ_SMS);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.
                        PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.READ_SMS)
                            == PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "permission denied",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    /*@Override
    public void onItemClick(View v, int position) {
        Toast.makeText(this, "Clicked Item: "+position,Toast.LENGTH_SHORT).show();
    }*/
}


/*access data from sent and draft*/

/*
        if (v == btnSent) {

            // Create Sent box URI
            Uri sentURI = Uri.parse("content://sms/sent");

            // List required columns
            String[] reqCols = new String[]{"_id", "address", "body"};

            // Get Content Resolver object, which will deal with Content
            // Provider
            ContentResolver cr = getContentResolver();

            // Fetch Sent SMS Message from Built-in Content Provider
            Cursor c = cr.query(sentURI, reqCols, null, null, null);

            // Attached Cursor with adapter and display in listview
            adapter = new SimpleCursorAdapter(this, R.layout.row, c, new String[]{"body", "address"}, new int[]{
                    R.id.lblMsg, R.id.lblNumber});
            lvMsg.setAdapter(adapter);

        }

        if (v == btnDraft) {
            // Create Draft box URI
            Uri draftURI = Uri.parse("content://sms/draft");

            // List required columns
            String[] reqCols = new String[]{"_id", "address", "body"};

            // Get Content Resolver object, which will deal with Content
            // Provider
            ContentResolver cr = getContentResolver();

            // Fetch Sent SMS Message from Built-in Content Provider
            Cursor c = cr.query(draftURI, reqCols, null, null, null);

            // Attached Cursor with adapter and display in listview
            adapter = new SimpleCursorAdapter(this, R.layout.row, c,
                    new String[]{"body", "address"}, new int[]{
                    R.id.lblMsg, R.id.lblNumber});
            lvMsg.setAdapter(adapter);

        }*/