package francoispog.samp.quiz;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity {

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button button = (Button) findViewById(R.id.play_button);
        editText = (EditText) findViewById(R.id.pseudo);

        button.setOnClickListener(this::startQuiz);
    }

    private void startQuiz(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("pseudo", editText.getText().toString());
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null){
            return;
        }

        Toast.makeText(this, "Score : " + data.getStringExtra("score"), Toast.LENGTH_SHORT).show();
    }
}