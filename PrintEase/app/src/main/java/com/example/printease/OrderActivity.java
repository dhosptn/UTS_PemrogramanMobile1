package com.example.printease;



import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.itextpdf.text.pdf.PdfReader;

public class OrderActivity extends AppCompatActivity {
    private static final int PICK_PDF_FILE = 1;
    private static final String TAG = "OrderActivity";

    private Uri selectedFileUri;
    private TextView fileNameTextView;
    private TextView pageCountTextView;
    private TextView totalPriceTextView;
    private EditText nameInput;
    private RadioGroup printTypeGroup;
    private Spinner paperSizeSpinner;
    private EditText copiesEditText;
    private Spinner paymentMethodSpinner;
    private CardView uploadArea;
    private Button orderButton;

    private int pageCount = 0;
    private int totalPrice = 0;

    // Firebase Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        initializeViews();
        setupSpinners();
        setupListeners();
    }

    private void initializeViews() {
        uploadArea = findViewById(R.id.uploadArea);
        fileNameTextView = findViewById(R.id.fileNameTextView);
        pageCountTextView = findViewById(R.id.pageCountTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        nameInput = findViewById(R.id.nameInput);
        printTypeGroup = findViewById(R.id.printTypeGroup);
        paperSizeSpinner = findViewById(R.id.paperSize);
        copiesEditText = findViewById(R.id.copies);
        paymentMethodSpinner = findViewById(R.id.paymentMethod);
        orderButton = findViewById(R.id.orderButton);

        uploadArea.setOnClickListener(v -> openFilePicker());
        orderButton.setOnClickListener(v -> handleSubmit());
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> paperSizeAdapter = ArrayAdapter.createFromResource(this,
                R.array.paper_sizes, android.R.layout.simple_spinner_item);
        paperSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paperSizeSpinner.setAdapter(paperSizeAdapter);

        ArrayAdapter<CharSequence> paymentAdapter = ArrayAdapter.createFromResource(this,
                R.array.payment_methods, android.R.layout.simple_spinner_item);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodSpinner.setAdapter(paymentAdapter);
    }

    private void setupListeners() {
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculatePrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        paperSizeSpinner.setOnItemSelectedListener(spinnerListener);
        printTypeGroup.setOnCheckedChangeListener((group, checkedId) -> calculatePrice());

        copiesEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculatePrice();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_FILE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            handleSelectedFile();
        }
    }

    private void handleSelectedFile() {
        if (selectedFileUri != null) {
            String fileName = getFileName(selectedFileUri);
            fileNameTextView.setText(fileName);
            countPages();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void countPages() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
            PdfReader reader = new PdfReader(inputStream);
            pageCount = reader.getNumberOfPages();
            reader.close();
            pageCountTextView.setText("Jumlah Halaman: " + pageCount);
            calculatePrice();
        } catch (Exception e) {
            Log.e(TAG, "Error counting pages: " + e.getMessage());
            showError("Error membaca file PDF. Silakan coba file lain.");
        }
    }

    private void calculatePrice() {
        if (pageCount == 0) return;

        boolean isColor = printTypeGroup.getCheckedRadioButtonId() == R.id.color;
        String paperSize = paperSizeSpinner.getSelectedItem().toString();
        int copies = 1;

        try {
            copies = Integer.parseInt(copiesEditText.getText().toString());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing copies: " + e.getMessage());
        }

        int pricePerPage;
        if ("A4".equals(paperSize)) {
            pricePerPage = isColor ? 1000 : 500;
        } else { // A3
            pricePerPage = isColor ? 2000 : 1000;
        }

        totalPrice = pageCount * pricePerPage * copies;
        totalPriceTextView.setText(String.format(Locale.getDefault(), "Total Harga: Rp %,d", totalPrice));
    }

    private void handleSubmit() {
        if (!validateInput()) {
            return;
        }


        // Start OrderSummaryActivity
        Intent intent = new Intent(this, OrderSummaryActivity.class);
        intent.putExtra("orderSummary", generateOrderSummary());
        startActivity(intent);
    }

    private String generateOrderSummary() {
        RadioButton selectedPrintType = findViewById(printTypeGroup.getCheckedRadioButtonId());
        return String.format(Locale.getDefault(),
                "Detail Pesanan:\nNama: %s\nFile: %s\nHalaman: %d\n" +
                        "Jenis Cetakan: %s\nUkuran: %s\nJumlah: %s\n" +
                        "Pembayaran: %s\nTotal: Rp %,d",
                nameInput.getText(),
                fileNameTextView.getText(),
                pageCount,
                selectedPrintType.getText(),
                paperSizeSpinner.getSelectedItem().toString(),
                copiesEditText.getText().toString(),
                paymentMethodSpinner.getSelectedItem().toString(),
                totalPrice);
    }



    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
    private boolean validateInput() {
        if (nameInput.getText().toString().trim().isEmpty()) {
            showError("Nama tidak boleh kosong");
            return false;
        }

        if (selectedFileUri == null) {
            showError("Silakan unggah file PDF");
            return false;
        }

        if (pageCount == 0) {
            showError("File PDF tidak memiliki halaman");
            return false;
        }

        if (copiesEditText.getText().toString().trim().isEmpty()) {
            showError("Jumlah kopi tidak boleh kosong");
            return false;
        }

        try {
            int copies = Integer.parseInt(copiesEditText.getText().toString());
            if (copies <= 0) {
                showError("Jumlah kopi harus lebih dari 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Jumlah kopi tidak valid");
            return false;
        }

        return true;
    }
}

