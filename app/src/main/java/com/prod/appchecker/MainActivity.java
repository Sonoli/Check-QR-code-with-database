package com.prod.appchecker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button scanBtn;

    Button btAdd;
    ListView listView;

    DatabaseHelper databaseHelper;
    ArrayList arrayList;
    ArrayAdapter arrayAdapter;

    String QR_CODE;
    int count = 0;
    int FLAG_PROVERKA = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        scanBtn = findViewById(R.id.btn_add);
        scanBtn.setOnClickListener(this);
        btAdd = findViewById(R.id.scanBtn);
        listView = findViewById(R.id.listview);



        databaseHelper = new DatabaseHelper(MainActivity.this);


        arrayList = databaseHelper.getAllText();

        arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1,arrayList);


        listView.setAdapter(arrayAdapter);


        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FLAG_PROVERKA = 1;

                scanCode();

            }
        });


    }

    @Override
    public void onClick(View view) {
        FLAG_PROVERKA = 2;
        scanCode();

    }

    private void scanCode() {

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){


            if (result.getContents() != null){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(result.getContents());
                builder.setTitle("Результат");
                builder.setPositiveButton("Просканировать снова", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        scanCode();
                    }
                }).setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                });


                if (FLAG_PROVERKA == 1){

                    QR_CODE = result.getContents();

                    count = Collections.frequency(arrayList, QR_CODE); // получим результат 2

                    if (count>=1){
                        Toast.makeText(this, "Такой товар есть!", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(this, "Такого товара нет! ", Toast.LENGTH_LONG).show();
                    }

                    count = 0;

                }

                if (FLAG_PROVERKA ==2){

                    QR_CODE = result.getContents();

                    databaseHelper.addText(QR_CODE);

                    arrayList.clear();
                    arrayList.addAll(databaseHelper.getAllText());
                    arrayAdapter.notifyDataSetChanged();
                    listView.invalidateViews();
                    listView.refreshDrawableState();

                }
                AlertDialog dialog = builder.create();
                dialog.show();

            }else{
                Toast.makeText(this, "QR код не отсканировался", Toast.LENGTH_SHORT).show();
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
}
