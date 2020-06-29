package singavi.truckinglogistics.in;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class OTPcheck extends AppCompatActivity {


    //Firebase Declarations
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;



    //Code Variables
    boolean verified = false;
    String otpSent;


    // UI Attributes Declaration
    EditText editTextphonenumber;
    Button verfiy,login;
    EditText editTextPin1,editTextPin2;
    EditText Otp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_pcheck);



        // UI Attributes Linked
        editTextphonenumber = findViewById(R.id.input_phone);
        verfiy = findViewById(R.id.verify_button);
        Otp = findViewById(R.id.typed_otp);
        editTextPin1 = findViewById(R.id.pin1);
        editTextPin2 = findViewById(R.id.pin2);
        login = findViewById(R.id.login);


        firebaseAuth = FirebaseAuth.getInstance();

        // On Click Verfiy Button ( To Recieve OTP )
        verfiy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNumber = editTextphonenumber.getText().toString();
                if (phoneNumber.equals("") || phoneNumber.length() != 10)
                {

                    Toast.makeText(OTPcheck.this, "Enter Valid Phone Number", Toast.LENGTH_SHORT).show();
                    editTextphonenumber.requestFocus();

                }

                phoneNumber = "+91"+phoneNumber;
                Toast.makeText(OTPcheck.this, phoneNumber, Toast.LENGTH_SHORT).show();
                numberVerify(phoneNumber);

            }
        });

        // On Click Login ( Otp check and Login With Pin and PhoneNumber saved In DB)
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                if (verified) {

                    String otp = Otp.getText().toString().trim();
                    verifyLogin(otp);
                }
                else

                    Toast.makeText(OTPcheck.this, "First Ask OTP", Toast.LENGTH_SHORT).show();


            }
        });






    }

    private void verifyLogin(String otpTyped) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpSent,otpTyped);

        if (otpSent.equals(otpTyped))

            signInWithPhoneAuthCredential(credential);

        else

            Toast.makeText(this, "Check Otp", Toast.LENGTH_SHORT).show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(OTPcheck.this, "Number Verified", Toast.LENGTH_SHORT).show();

                            //Also Directs To Main Activity if DataSaved
                            savePinAndPhoneNumber();

                        }
                        else
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                                Toast.makeText(OTPcheck.this, "Invalid Otp", Toast.LENGTH_SHORT).show();
                        }
                        }
                    }
                );
    }



    private void numberVerify(String phNumber) {




        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallback

    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {


        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

            super.onCodeSent(s, forceResendingToken);

            otpSent = s;
            verified = true;

        }
    };
    // Saves User Detail in DB (If successful -> Goes to MainActivity and if !successful -> Network Error)
    private void savePinAndPhoneNumber() {

        String p1 = editTextPin1.getText().toString().trim();
        String p2 = editTextPin2.getText().toString().trim();
        String phNumber = editTextphonenumber.getText().toString().trim();

        //Get Date
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("dd/MM/YYYY");
        Date myDate = new Date();
        String currentDate = timeStampFormat.format(myDate);
        // ....

        if (p1.equals(p2))
        {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(phNumber);

            HashMap<String, String> userBase = new HashMap<>();

            userBase.put("Phone Number", phNumber);
            userBase.put("App Pin",p1);
            userBase.put("Registeration Date",currentDate);

            databaseReference.setValue(userBase).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful())
                    {

                        Intent intent = new Intent(OTPcheck.this,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    }

                    else
                    {
                        Toast.makeText(OTPcheck.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();

                    }
                }

            });

        }
        else
        {
            Toast.makeText(this, "Different Pin Entered", Toast.LENGTH_SHORT).show();
        }
    }
}