        package com.example.jenniferdai.cookyourrice;

        import android.content.Intent;
        import android.os.Bundle;
        import android.widget.Toast;

        import com.example.jenniferdai.cookyourrice.MainActivity;
        import com.example.jenniferdai.cookyourrice.R;

        /**
 * Created by aneh on 10/8/2014.
 */
public class NotificationDetailsActivity extends MainActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent get = getIntent();
        String data = get.getStringExtra("msg");
        Toast.makeText(getBaseContext(), "Message: "+ data, Toast.LENGTH_LONG).show();
    }
}